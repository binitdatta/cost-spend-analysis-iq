"""
login_required decorator — drop this into app/utils.py (or any utils file).

Usage in chat_routes.py:
    from app.utils import login_required

    @chat_bp.route("/chat")
    @login_required
    def index():
        ...
"""
from functools import wraps
from flask import session, redirect, url_for


def login_required(f):
    """Redirect to login if the user is not authenticated."""
    @wraps(f)
    def decorated(*args, **kwargs):
        if not session.get("authenticated"):
            return redirect(url_for("auth.login"))
        return f(*args, **kwargs)
    return decorated