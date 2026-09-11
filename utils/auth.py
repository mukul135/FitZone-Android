# ===============================
# utils/auth.py
# ===============================
# WHAT THIS FILE DOES:
# The original app.py protects pages like this:
#
#     if "user" not in session:
#         return redirect(url_for("login"))
#
# That works for a browser, because the browser automatically stores a
# session cookie. Android has no cookie jar like that, so we replace the
# cookie-based session with a TOKEN:
#
#   1. Android sends email+password to /api/auth/login
#   2. Flask checks the password, then calls generate_token(member_id)
#   3. Flask sends that token back to Android in the JSON response
#   4. Android saves the token (e.g. in SharedPreferences)
#   5. On every future request to a protected endpoint, Android sends:
#         Authorization: Bearer <token>
#   6. Flask calls verify_token(token) to check it's valid before
#      running the route
#
# We use "itsdangerous" for this — it's already installed because Flask
# itself depends on it (see requirements.txt), so no new library is needed.
# It creates a signed token: nobody can fake or edit it without knowing
# your SECRET_KEY, and it can auto-expire after a set time.

from itsdangerous import URLSafeTimedSerializer, BadSignature, SignatureExpired
from functools import wraps
from flask import request
import config

from utils.responses import error_response

# How long a token stays valid, in seconds. 7 days here.
TOKEN_MAX_AGE_SECONDS = 60 * 60 * 24 * 7

# The serializer is the object that actually creates/reads tokens.
# It uses SECRET_KEY from config.py to "sign" the token so it can't be
# tampered with.
_serializer = URLSafeTimedSerializer(config.SECRET_KEY)


def generate_token(member_id):
    """
    Creates a token that represents "this is member #<member_id>, logged in".
    Called once, right after a successful login.
    """
    return _serializer.dumps({"member_id": member_id})


def verify_token(token):
    """
    Checks a token sent by Android.
    Returns the member_id if the token is valid and not expired.
    Returns None if the token is invalid, tampered with, or expired.
    """
    try:
        data = _serializer.loads(token, max_age=TOKEN_MAX_AGE_SECONDS)
        return data.get("member_id")
    except (BadSignature, SignatureExpired):
        return None


def token_required(route_function):
    """
    This is a "decorator" — you put @token_required directly above any
    route function that should require login, the same way the original
    app.py manually checked "if 'user' not in session" at the top of
    every protected route.

    Example usage (we'll use this in Step 2):

        @app.route("/api/user/profile")
        @token_required
        def profile(member_id):
            ...  # member_id is automatically provided here

    HOW IT WORKS:
    1. It looks for a header called "Authorization" that looks like:
           Authorization: Bearer abc123.....
    2. It pulls out the "abc123....." part and checks it with verify_token()
    3. If valid, it calls your real route function and passes in member_id
    4. If missing/invalid, it returns a 401 error WITHOUT running your route
    """
    @wraps(route_function)
    def wrapper(*args, **kwargs):
        auth_header = request.headers.get("Authorization", "")

        if not auth_header.startswith("Bearer "):
            return error_response(
                "Missing or invalid Authorization header",
                "UNAUTHORIZED",
                401
            )

        token = auth_header.split("Bearer ", 1)[1].strip()
        member_id = verify_token(token)

        if member_id is None:
            return error_response(
                "Invalid or expired token, please log in again",
                "INVALID_TOKEN",
                401
            )

        # Pass member_id into the actual route function
        return route_function(member_id, *args, **kwargs)

    return wrapper