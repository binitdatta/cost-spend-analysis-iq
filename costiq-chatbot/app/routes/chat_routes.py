"""
Chat routes — main UI page and SSE streaming endpoint.
Streams Claude responses token-by-token using Anthropic's streaming API.
"""
import os
import json
import time
import uuid
import logging
import anthropic
from datetime import datetime, timezone
from typing import Any
from flask import (
    Blueprint, Response, render_template, request,
    session, redirect, url_for, stream_with_context
)
from app.services.keycloak_service import get_valid_access_token
from app.services.costiq_service import fetch_all_data, build_ai_context

chat_bp = Blueprint("chat", __name__)

# Server-side data cache keyed by username (avoids bloating the session cookie)
_data_cache: dict = {}

ANTHROPIC_CLIENT = anthropic.Anthropic(api_key=os.getenv("ANTHROPIC_API_KEY"))
MODEL            = "claude-opus-4-5"
MAX_TOKENS       = 2048


# ── Audit logging ─────────────────────────────────────────────────────────────
# Mirrors the pattern in bomiq SDM handlers.py — every Anthropic call gets a
# unique request ID, full request/response payload logged, and cost calculated.

# Pricing per million tokens (claude-opus-4-5 as of 2025)
_COST_PER_M_INPUT  = 3.00    # USD
_COST_PER_M_OUTPUT = 15.00   # USD


def _make_request_id() -> str:
    ts  = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%S%f")
    uid = uuid.uuid4().hex[:6]
    return f"costiq:{ts}:{uid}"


def _serialize(obj: Any) -> Any:
    if hasattr(obj, "model_dump"):
        return obj.model_dump()
    if hasattr(obj, "__dict__"):
        return vars(obj)
    return str(obj)


def _calculate_cost(usage: dict) -> tuple[float, str]:
    input_tokens  = usage.get("input_tokens", 0)
    output_tokens = usage.get("output_tokens", 0)
    cache_read    = usage.get("cache_read_input_tokens", 0)
    cost = (
        ((input_tokens - cache_read) * _COST_PER_M_INPUT  / 1_000_000) +
        (cache_read                  * (_COST_PER_M_INPUT * 0.1) / 1_000_000) +
        (output_tokens               * _COST_PER_M_OUTPUT / 1_000_000)
    )
    summary = (
        f"input={input_tokens} output={output_tokens} "
        f"cache_read={cache_read} → ${cost:.6f} USD"
    )
    return cost, summary


def _log_request(request_id: str, model: str, system: str, messages: list) -> None:
    payload = {
        "model":      model,
        "max_tokens": MAX_TOKENS,
        "system":     system[:300] + "..." if len(system) > 300 else system,
        "messages":   [
            {"role": m["role"], "content": str(m["content"])[:200]}
            for m in messages
        ],
    }
    logging.getLogger("anthropic.audit").info(
        "\n╔══ ANTHROPIC REQUEST  [%s] ══\n%s\n╚═══════════════════════════════════════",
        request_id,
        json.dumps(payload, indent=2, default=_serialize),
    )


def _log_response(request_id: str, final_msg: Any, elapsed: float) -> None:
    serialized  = _serialize(final_msg)
    usage       = serialized.get("usage", {}) if isinstance(serialized, dict) else {}
    _, cost_str = _calculate_cost(usage)
    logging.getLogger("anthropic.audit").info(
        "\n╔══ ANTHROPIC RESPONSE [%s]  (%.3fs) ══\n"
        "    COST  : %s\n"
        "    USAGE : %s\n"
        "╚═══════════════════════════════════════",
        request_id,
        elapsed,
        cost_str,
        json.dumps(usage, default=_serialize),
    )


def _log_error(request_id: str, exc: Exception, elapsed: float) -> None:
    logging.getLogger("anthropic.audit").error(
        "\n╔══ ANTHROPIC ERROR    [%s]  (%.3fs) ══\n%s\n╚═══════════════════════════════════════",
        request_id,
        elapsed,
        str(exc),
    )


# ── Auth guard ────────────────────────────────────────────────────────────────

def _require_auth():
    """Return redirect if not authenticated, else None."""
    if not session.get("authenticated"):
        return redirect(url_for("auth.login"))
    return None


# ── Routes ────────────────────────────────────────────────────────────────────

@chat_bp.route("/")
def home():
    """Public home page — no authentication required."""
    return render_template("home.html")


@chat_bp.route("/chat")
def index():
    guard = _require_auth()
    if guard:
        return guard
    return render_template("chat.html",
                           username=session.get("display_name", "User"),
                           email=session.get("email", ""))


@chat_bp.route("/chat/stream", methods=["POST"])
def stream():
    """
    SSE endpoint — receives the full conversation history from the browser,
    fetches live CostIQ data, injects it into the system prompt, and streams
    Claude's response token by token back to the browser.
    """
    guard = _require_auth()
    if guard:
        return Response("data: [AUTH_REQUIRED]\n\n", mimetype="text/event-stream")

    payload  = request.get_json(silent=True) or {}
    messages = payload.get("messages", [])

    if not messages:
        return Response("data: [NO_MESSAGE]\n\n", mimetype="text/event-stream")

    token = get_valid_access_token()
    if not token:
        return Response("data: [SESSION_EXPIRED]\n\n", mimetype="text/event-stream")

    try:
        data    = fetch_all_data(token)
        context = build_ai_context(data)
        _data_cache[session.get("username", "anon")] = data
    except Exception as e:
        logging.getLogger(__name__).warning("CostIQ data fetch failed: %s", e)
        context = "Live data unavailable. Answer based on general CostIQ knowledge."
        data    = {}

    system_prompt = f"""
You are the CostIQ AI Assistant — an intelligent cost spend analysis assistant for
GlobalBite Foods Inc. You have real-time access to the company's cost data.

{context}

RESPONSE STYLE:
- Be concise and professional
- Use bullet points and tables in markdown where helpful
- Always cite specific figures when answering cost questions
- Do NOT fabricate data — only use figures from the context above

EXCEL REPORT CAPABILITY:
When the user asks to generate, export, download, or create an Excel report,
respond with a brief confirmation of what the report will contain, then end
your response with exactly this token on its own line: [EXCEL_READY]

The Excel report contains 8 sheets:
  📊 Executive Summary   — KPIs: grand total spend, entry counts, active campaigns/suppliers
  🥩 Food Costs          — 28 columns including supplier tier, cost vs baseline delta, % of total
  📦 Packaging Costs     — 26 columns including recyclability, order vs min qty, cost vs baseline
  🎁 Toy Allocations     — 26 columns including campaign budget utilisation, licensed IP, age range
  📢 Marketing Costs     — 19 columns including campaign budget remaining, cost type breakdown
  🏆 Supplier Scorecard  — Cross-category spend per supplier with tier and country (fully computed)
  🌍 Regional Summary    — Spend by region × category matrix (fully computed)
  🎯 Campaign Summary    — Budget vs actual spend + toy/marketing ratio per campaign (fully computed)
""".strip()

    # Capture request_id and start time outside generate() so they're
    # available for both the request log and the response log
    request_id = _make_request_id()
    _log_request(request_id, MODEL, system_prompt, messages)

    def generate():
        audit = logging.getLogger("anthropic.audit")
        start  = time.perf_counter()
        chunks = []

        try:
            audit.info(
                "╔══ ANTHROPIC STREAM START [%s] ══  user=%s  model=%s",
                request_id,
                session.get("username", "anon"),
                MODEL,
            )

            with ANTHROPIC_CLIENT.messages.stream(
                model=MODEL,
                max_tokens=MAX_TOKENS,
                system=system_prompt,
                messages=messages
            ) as stream_obj:

                for text in stream_obj.text_stream:
                    chunks.append(text)
                    safe = text.replace("\n", "\\n")
                    yield f"data: {safe}\n\n"

                # Final message — contains usage/cost data
                final   = stream_obj.get_final_message()
                elapsed = time.perf_counter() - start
                _log_response(request_id, final, elapsed)

                audit.info(
                    "╔══ ANTHROPIC STREAM END   [%s]  (%.3fs | %d chunks | ~%d chars) ══",
                    request_id,
                    elapsed,
                    len(chunks),
                    sum(len(c) for c in chunks),
                )

            yield "data: [DONE]\n\n"

        except anthropic.APIError as e:
            _log_error(request_id, e, time.perf_counter() - start)
            yield f"data: [ERROR] Anthropic API error: {e}\n\n"
        except Exception as e:
            _log_error(request_id, e, time.perf_counter() - start)
            yield f"data: [ERROR] {e}\n\n"

    return Response(
        stream_with_context(generate()),
        mimetype="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
        }
    )