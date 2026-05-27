"""CostIQ ChatBot — entry point."""
import os
import logging
from logging.handlers import RotatingFileHandler
from pathlib import Path
from dotenv import load_dotenv
load_dotenv()

# ── Log directory ─────────────────────────────────────────────────────────────
LOG_DIR = Path("logs")
LOG_DIR.mkdir(exist_ok=True)

# ── Formatters ────────────────────────────────────────────────────────────────
CONSOLE_FMT = logging.Formatter(
    "%(asctime)s [%(levelname)-8s] %(name)s — %(message)s",
    datefmt="%H:%M:%S"
)
FILE_FMT = logging.Formatter(
    "%(asctime)s [%(levelname)-8s] %(name)s — %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)

# ── Handlers ──────────────────────────────────────────────────────────────────
console_handler = logging.StreamHandler()
console_handler.setFormatter(CONSOLE_FMT)
console_handler.setLevel(logging.DEBUG)

# Main app log — all INFO+ messages, rotates at 10MB, keeps 5 backups
app_file_handler = RotatingFileHandler(
    LOG_DIR / "costiq_chatbot.log",
    maxBytes=10 * 1024 * 1024,
    backupCount=5,
    encoding="utf-8"
)
app_file_handler.setFormatter(FILE_FMT)
app_file_handler.setLevel(logging.INFO)

# Anthropic audit log — dedicated file, every request/response/cost entry
audit_file_handler = RotatingFileHandler(
    LOG_DIR / "anthropic_audit.log",
    maxBytes=20 * 1024 * 1024,
    backupCount=10,
    encoding="utf-8"
)
audit_file_handler.setFormatter(FILE_FMT)
audit_file_handler.setLevel(logging.INFO)

# ── Root logger — console + main file ────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    handlers=[console_handler, app_file_handler]
)

# ── Per-logger overrides ──────────────────────────────────────────────────────

# CostIQ service — DEBUG shows per-endpoint record counts
costiq_log = logging.getLogger("app.services.costiq_service")
costiq_log.setLevel(logging.DEBUG)

# Anthropic audit — console + dedicated audit file
audit_log = logging.getLogger("anthropic.audit")
audit_log.setLevel(logging.INFO)
audit_log.addHandler(audit_file_handler)
audit_log.propagate = True   # also goes to console + main file via root

# Suppress noisy libraries
logging.getLogger("urllib3").setLevel(logging.WARNING)
logging.getLogger("werkzeug").setLevel(logging.WARNING)
logging.getLogger("httpx").setLevel(logging.WARNING)

# ── Start app ─────────────────────────────────────────────────────────────────
log = logging.getLogger(__name__)

from app import create_app
app = create_app()

if __name__ == "__main__":
    port  = int(os.getenv("FLASK_PORT", 5001))
    debug = os.getenv("FLASK_DEBUG", "true").lower() == "true"
    log.info("=" * 55)
    log.info("  CostIQ AI ChatBot  →  http://localhost:%d", port)
    log.info("  CostIQ API Base    →  %s", os.getenv("COSTIQ_API_BASE", "http://localhost:8085/costiq/api"))
    log.info("  Keycloak Realm     →  %s", os.getenv("KEYCLOAK_REALM", "costiq-realm"))
    log.info("  Anthropic Model    →  claude-opus-4-5")
    log.info("  Log files          →  %s/", LOG_DIR.absolute())
    log.info("=" * 55)
    app.run(host="0.0.0.0", port=port, debug=debug, use_reloader=False)