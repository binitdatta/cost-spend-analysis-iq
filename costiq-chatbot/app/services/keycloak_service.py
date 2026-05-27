"""
Keycloak PKCE authentication service.
Implements Authorization Code + PKCE flow against Keycloak 26.
Tokens are stored server-side in auth_routes._token_store — NOT in the
session cookie — to avoid ERR_TOO_MANY_REDIRECTS caused by oversized cookies.
"""
import os
import base64
import hashlib
import secrets
import requests
from flask import session

KEYCLOAK_BASE     = os.getenv("KEYCLOAK_BASE_URL", "http://localhost:8080")
REALM             = os.getenv("KEYCLOAK_REALM", "costiq-realm")
CLIENT_ID         = os.getenv("KEYCLOAK_CLIENT_ID", "costiq-app")
REDIRECT_URI      = os.getenv("KEYCLOAK_REDIRECT_URI", "http://localhost:5001/auth/callback")

BASE_URL          = f"{KEYCLOAK_BASE}/realms/{REALM}/protocol/openid-connect"
AUTH_ENDPOINT     = f"{BASE_URL}/auth"
TOKEN_ENDPOINT    = f"{BASE_URL}/token"
LOGOUT_ENDPOINT   = f"{BASE_URL}/logout"
USERINFO_ENDPOINT = f"{BASE_URL}/userinfo"


# ── PKCE helpers ──────────────────────────────────────────────────────────────

def generate_pkce_pair() -> tuple[str, str]:
    verifier  = secrets.token_urlsafe(64)
    digest    = hashlib.sha256(verifier.encode()).digest()
    challenge = base64.urlsafe_b64encode(digest).rstrip(b"=").decode()
    return verifier, challenge


def build_auth_url() -> str:
    verifier, challenge = generate_pkce_pair()
    state = secrets.token_urlsafe(16)
    session["pkce_verifier"] = verifier
    session["oauth_state"]   = state
    params = (
        f"?client_id={CLIENT_ID}"
        f"&redirect_uri={REDIRECT_URI}"
        f"&response_type=code"
        f"&scope=openid+profile+email"
        f"&code_challenge={challenge}"
        f"&code_challenge_method=S256"
        f"&state={state}"
    )
    return AUTH_ENDPOINT + params


def exchange_code_for_tokens(code: str, state: str) -> dict:
    if state != session.get("oauth_state"):
        raise ValueError("OAuth state mismatch — possible CSRF")
    verifier = session.pop("pkce_verifier", None)
    if not verifier:
        raise ValueError("PKCE verifier missing from session")
    resp = requests.post(TOKEN_ENDPOINT, data={
        "grant_type":    "authorization_code",
        "client_id":     CLIENT_ID,
        "redirect_uri":  REDIRECT_URI,
        "code":          code,
        "code_verifier": verifier,
    }, timeout=10)
    if resp.status_code != 200:
        raise RuntimeError(f"Token exchange failed: {resp.status_code} {resp.text}")
    return resp.json()


def get_userinfo(access_token: str) -> dict:
    resp = requests.get(USERINFO_ENDPOINT,
                        headers={"Authorization": f"Bearer {access_token}"},
                        timeout=10)
    return resp.json() if resp.status_code == 200 else {}


def build_logout_url(redirect_to: str = "http://localhost:5001/") -> str:
    return (
        f"{LOGOUT_ENDPOINT}"
        f"?client_id={CLIENT_ID}"
        f"&post_logout_redirect_uri={redirect_to}"
    )


def get_valid_access_token() -> str | None:
    """
    Return a valid access token for the current user.
    Reads from the server-side _token_store in auth_routes.
    Refreshes automatically if the token has expired.
    """
    username = session.get("username")
    if not username:
        return None

    # Import here to avoid circular import at module load time
    from app.routes.auth_routes import _token_store
    tokens = _token_store.get(username)
    if not tokens:
        return None

    access_token = tokens.get("access_token")
    if not access_token:
        return None

    # Verify token is still valid
    resp = requests.get(USERINFO_ENDPOINT,
                        headers={"Authorization": f"Bearer {access_token}"},
                        timeout=5)
    if resp.status_code == 200:
        return access_token

    # Token expired — try refresh
    refresh_token = tokens.get("refresh_token")
    if not refresh_token:
        return None

    resp = requests.post(TOKEN_ENDPOINT, data={
        "grant_type":    "refresh_token",
        "client_id":     CLIENT_ID,
        "refresh_token": refresh_token,
    }, timeout=10)

    if resp.status_code != 200:
        return None

    data = resp.json()
    _token_store[username] = {
        "access_token":  data["access_token"],
        "refresh_token": data.get("refresh_token", refresh_token),
        "id_token":      data.get("id_token", tokens.get("id_token")),
    }
    return data["access_token"]