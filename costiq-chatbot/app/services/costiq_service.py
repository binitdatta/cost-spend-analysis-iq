"""
CostIQ Spring Boot REST API service.
Fetches data from all CostIQ endpoints using the Keycloak JWT access token.
Every call is authenticated — no endpoint is called unauthenticated.
"""
import os
import logging
import requests
from typing import Any

log = logging.getLogger(__name__)

COSTIQ_API_BASE = os.getenv("COSTIQ_API_BASE", "http://localhost:8085/costiq/api")


def _get(token: str, path: str) -> list[dict] | dict:
    """Authenticated GET against CostIQ Spring Boot API."""
    url = f"{COSTIQ_API_BASE}{path}"
    # Log token prefix only — never log the full token
    token_preview = f"{token[:12]}..." if token and len(token) > 12 else "MISSING"
    log.debug("→ GET %s  (token: %s)", url, token_preview)

    try:
        resp = requests.get(
            url,
            headers={"Authorization": f"Bearer {token}"},
            timeout=15
        )
    except requests.exceptions.ConnectionError as e:
        log.error("CONNECTION FAILED for %s — is CostIQ running on %s? Error: %s",
                  path, COSTIQ_API_BASE, e)
        raise RuntimeError(f"Cannot connect to CostIQ API at {url}. "
                           f"Is Spring Boot running?") from e
    except requests.exceptions.Timeout:
        log.error("TIMEOUT calling %s (15s limit exceeded)", url)
        raise RuntimeError(f"CostIQ API timed out: {url}")

    log.debug("← %s %s → HTTP %d (%d bytes)",
              "GET", url, resp.status_code, len(resp.content))

    if resp.status_code == 200:
        data = resp.json()
        # Unwrap Spring Page wrapper if present
        if isinstance(data, dict) and "content" in data:
            count = len(data["content"])
            log.info("  ✓ %s → %d records (paged, total=%s)",
                     path, count, data.get("totalElements", "?"))
            return data["content"]
        count = len(data) if isinstance(data, list) else 1
        log.info("  ✓ %s → %d records", path, count)
        return data

    if resp.status_code == 401:
        log.error("  ✗ %s → 401 UNAUTHORIZED — token expired or invalid", path)
        raise RuntimeError(f"401 Unauthorized calling {path}. Token may be expired.")

    if resp.status_code == 403:
        log.error("  ✗ %s → 403 FORBIDDEN — user lacks permission for this endpoint", path)
        raise RuntimeError(f"403 Forbidden calling {path}. Check Keycloak roles.")

    if resp.status_code == 404:
        log.error("  ✗ %s → 404 NOT FOUND — endpoint missing. "
                  "Is ApiController.java deployed in CostIQ Spring Boot?", path)
        raise RuntimeError(f"404 Not Found: {url}. Add ApiController.java to CostIQ.")

    log.error("  ✗ %s → HTTP %d: %s", path, resp.status_code, resp.text[:300])
    raise RuntimeError(f"CostIQ API {path} → {resp.status_code}: {resp.text[:300]}")


# ── Individual fetch functions ─────────────────────────────────────────────

def get_food_costs(token: str)       -> list: return _get(token, "/food-costs")
def get_packaging_costs(token: str)  -> list: return _get(token, "/packaging-costs")
def get_toy_allocations(token: str)  -> list: return _get(token, "/toy-allocations")
def get_marketing_costs(token: str)  -> list: return _get(token, "/marketing-costs")
def get_campaigns(token: str)        -> list: return _get(token, "/campaigns")
def get_suppliers(token: str)        -> list: return _get(token, "/suppliers")
def get_food_items(token: str)       -> list: return _get(token, "/food-items")
def get_packaging_items(token: str)  -> list: return _get(token, "/packaging-items")
def get_toy_items(token: str)        -> list: return _get(token, "/toy-items")
def get_countries(token: str)        -> list: return _get(token, "/countries")
def get_fiscal_periods(token: str)   -> list: return _get(token, "/fiscal-periods")
def get_cost_centers(token: str)     -> list: return _get(token, "/cost-centers")


# ── Aggregate fetch — used by the AI context builder ──────────────────────

def fetch_all_data(token: str) -> dict[str, Any]:
    """
    Fetch all CostIQ data in one call. Used to build the AI system prompt context
    and to populate the Excel export. Errors on individual endpoints are caught
    so a partial result is still useful.
    """
    log.info("=" * 55)
    log.info("fetch_all_data: calling CostIQ API at %s", COSTIQ_API_BASE)
    log.info("=" * 55)

    result  = {}
    errors  = []
    endpoints = {
        "food_costs":      get_food_costs,
        "packaging_costs": get_packaging_costs,
        "toy_allocations": get_toy_allocations,
        "marketing_costs": get_marketing_costs,
        "campaigns":       get_campaigns,
        "suppliers":       get_suppliers,
        "food_items":      get_food_items,
        "packaging_items": get_packaging_items,
        "countries":       get_countries,
        "fiscal_periods":  get_fiscal_periods,
        "cost_centers":    get_cost_centers,
    }

    for key, fn in endpoints.items():
        try:
            result[key] = fn(token)
        except Exception as e:
            log.warning("  SKIPPED %-20s — %s", key, e)
            result[key] = []
            result[f"{key}_error"] = str(e)
            errors.append(key)

    ok_count = len(endpoints) - len(errors)
    log.info("fetch_all_data complete: %d/%d endpoints OK", ok_count, len(endpoints))
    if errors:
        log.warning("Failed endpoints: %s", ", ".join(errors))
        log.warning("► If all 404: ApiController.java not deployed in CostIQ Spring Boot.")
        log.warning("► If all 401: access token is invalid or expired.")
    for key in endpoints:
        records = result.get(key) or []
        err     = result.get(f"{key}_error")
        if err:
            log.warning("  %-22s → ERROR: %s", key, err)
        else:
            log.info("  %-22s → %d records", key, len(records))

    return result


# ── Context summary for AI prompt ─────────────────────────────────────────

def build_ai_context(data: dict) -> str:
    """
    Build a concise text summary of live CostIQ data to inject into the
    Claude system prompt. Keeps the context window manageable.
    """
    log.info("build_ai_context: building system prompt from fetched data")
    def safe_len(key): return len(data.get(key) or [])
    def total_cost(key, field):
        items = data.get(key) or []
        try:
            return sum(float(i.get(field, 0) or 0) for i in items)
        except: return 0

    food_total = total_cost("food_costs", "totalCostUsd")
    pkg_total  = total_cost("packaging_costs", "totalCostUsd")
    toy_total  = total_cost("toy_allocations", "totalCostUsd")
    mkt_total  = total_cost("marketing_costs", "amountUsd")
    grand      = food_total + pkg_total + toy_total + mkt_total

    # Supplier names
    suppliers = [s.get("name", "") for s in (data.get("suppliers") or [])[:10]]

    # Campaign names
    campaigns = [c.get("name", "") for c in (data.get("campaigns") or [])[:8]]

    # Fiscal periods
    periods = [p.get("periodName", "") for p in (data.get("fiscal_periods") or [])[:8]]

    # Country/region breakdown
    countries = data.get("countries") or []
    regions   = list({c.get("region", {}).get("name", "") for c in countries if c.get("region")})

    context = f"""
You are the CostIQ AI Assistant for GlobalBite Foods Inc., an enterprise cost spend 
analysis platform. You have LIVE access to the following real data:

FINANCIAL SUMMARY (all figures in USD):
  Grand Total Spend : ${grand:,.0f}
  Food Costs        : ${food_total:,.0f}  ({safe_len('food_costs')} entries)
  Packaging Costs   : ${pkg_total:,.0f}  ({safe_len('packaging_costs')} entries)
  Toy Allocations   : ${toy_total:,.0f}  ({safe_len('toy_allocations')} allocations)
  Marketing Costs   : ${mkt_total:,.0f}  ({safe_len('marketing_costs')} entries)

OPERATIONS:
  Active Campaigns  : {safe_len('campaigns')} campaigns — {', '.join(campaigns)}
  Active Suppliers  : {safe_len('suppliers')} suppliers — {', '.join(suppliers)}
  Regions           : {', '.join(regions)}
  Fiscal Periods    : {', '.join(periods)}

CAPABILITIES:
  - Answer detailed questions about cost entries, campaigns, suppliers, regions
  - Compare spend across fiscal periods, regions, or categories  
  - Identify top suppliers by spend, top campaigns by cost
  - Generate an Excel report combining all cost data — user must ask explicitly
  - Flag anomalies or unusual cost patterns if asked

Always answer with specific figures from the live data above. 
If a question requires data not in your summary, say so clearly.
When generating Excel, confirm what sheets will be included before generating.
""".strip()

    return context