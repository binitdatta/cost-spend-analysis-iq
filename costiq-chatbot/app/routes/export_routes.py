"""
Export routes — proxies the Excel download from the CostIQ Spring Boot app.

Flow:
  1. Get a valid Keycloak access token from the Flask session
  2. Call GET /costiq/api/export/excel on the Spring Boot app (Bearer token)
  3. Spring Boot builds the augmented Apache POI workbook and streams it back
  4. Flask proxies the bytes directly to the browser as a download

This means the Excel is always built by Spring Boot (Apache POI, live DB data)
not by the Python service. The Python excel_service.py is kept as a fallback.
"""
import os
import logging
import requests
from datetime import datetime
from flask import Blueprint, Response, session, redirect, url_for

from app.services.keycloak_service import get_valid_access_token
from app.services.excel_service import build_excel_report
from app.routes.chat_routes import _data_cache

log = logging.getLogger(__name__)

export_bp = Blueprint("export", __name__)

COSTIQ_API_BASE = os.getenv("COSTIQ_API_BASE", "http://localhost:8085/costiq/api")


@export_bp.route("/export/excel")
def export_excel():
    if not session.get("authenticated"):
        return redirect(url_for("auth.login"))

    token = get_valid_access_token()
    if not token:
        return redirect(url_for("auth.login"))

    filename = f"CostIQ_Report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xlsx"

    # ── Primary: fetch workbook from Spring Boot Apache POI endpoint ──────────
    try:
        log.info("Requesting Excel workbook from Spring Boot: %s/export/excel",
                 COSTIQ_API_BASE)
        resp = requests.get(
            f"{COSTIQ_API_BASE}/export/excel",
            headers={"Authorization": f"Bearer {token}"},
            timeout=60,   # POI build can take a few seconds on large datasets
            stream=True
        )
        if resp.status_code == 200:
            log.info("Spring Boot Excel download: %d bytes", len(resp.content))
            return Response(
                resp.content,
                mimetype="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                headers={"Content-Disposition": f"attachment; filename={filename}"}
            )
        else:
            log.warning("Spring Boot Excel endpoint returned %d — falling back to Python",
                        resp.status_code)
    except Exception as e:
        log.warning("Spring Boot Excel endpoint failed (%s) — falling back to Python", e)

    # ── Fallback: build workbook in Python if Spring Boot call fails ──────────
    log.info("Building Excel workbook in Python (fallback)")
    from app.services.costiq_service import fetch_all_data
    username = session.get("username", "anon")
    data = _data_cache.get(username) or fetch_all_data(token)
    excel_bytes = build_excel_report(data)

    return Response(
        excel_bytes,
        mimetype="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        headers={"Content-Disposition": f"attachment; filename={filename}"}
    )