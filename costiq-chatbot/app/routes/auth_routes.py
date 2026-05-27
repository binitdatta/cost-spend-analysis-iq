"""Auth routes — Keycloak PKCE login, callback, logout."""
from flask import Blueprint, redirect, request, session, url_for
from app.services.keycloak_service import (
    build_auth_url, exchange_code_for_tokens,
    get_userinfo, build_logout_url
)

auth_bp = Blueprint("auth", __name__, url_prefix="/auth")

# Server-side token store keyed by username.
# Keeps JWT tokens OUT of the session cookie which has a 4093-byte browser limit.
# The Keycloak access token alone is ~2KB — storing it in the cookie causes
# ERR_TOO_MANY_REDIRECTS because the browser silently drops the oversized cookie,
# the session appears empty on every request, and Flask loops back to login.
_token_store: dict = {}


@auth_bp.route("/login")
def login():
    return redirect(build_auth_url())


@auth_bp.route("/callback")
def callback():
    error = request.args.get("error")
    if error:
        return f"Authentication error: {error} — {request.args.get('error_description')}", 400

    code  = request.args.get("code")
    state = request.args.get("state")

    if not code:
        return "Missing authorization code", 400

    try:
        tokens = exchange_code_for_tokens(code, state)
    except (ValueError, RuntimeError) as e:
        return f"Token exchange failed: {e}", 400

    userinfo = get_userinfo(tokens["access_token"])
    username = userinfo.get("preferred_username", "user")

    # Store tokens server-side — NOT in the session cookie
    _token_store[username] = {
        "access_token":  tokens["access_token"],
        "refresh_token": tokens.get("refresh_token"),
        "id_token":      tokens.get("id_token"),
    }

    # Session cookie holds only small fields — well under 4093 bytes
    session["username"]     = username
    session["display_name"] = userinfo.get("name", username)
    session["email"]        = userinfo.get("email", "")
    session["authenticated"] = True

    return redirect(url_for("chat.index"))


@auth_bp.route("/logout")
def logout():
    username  = session.get("username", "")
    id_token  = _token_store.get(username, {}).get("id_token", "")
    _token_store.pop(username, None)
    session.clear()
    return redirect(build_logout_url())