# ===============================
# utils/responses.py
# ===============================
# WHAT THIS FILE DOES:
# Every API endpoint we build must return JSON in the SAME shape, so the
# Android app can always expect the same structure, whether the request
# succeeded or failed. Instead of typing that shape out by hand in every
# route, we call these two small helper functions.
#
# WHY THIS MATTERS FOR ANDROID:
# The Android developer (later, that's also you) will write ONE Java class
# that knows how to read {"success":..., "message":..., "data":...}.
# If every endpoint follows this same shape, that one class works everywhere.

from flask import jsonify


def success_response(data=None, message="Success", status_code=200):
    """
    Use this for anything that worked.

    Example:
        return success_response(data={"id": 1}, message="Login successful")

    Produces:
        {
            "success": true,
            "message": "Login successful",
            "data": {"id": 1}
        }
    """
    response = {
        "success": True,
        "message": message,
        "data": data if data is not None else {}
    }
    return jsonify(response), status_code


def error_response(message="Something went wrong", error_code="ERROR", status_code=400):
    """
    Use this for anything that failed (bad input, wrong password, etc).

    Example:
        return error_response("Invalid email or password", "INVALID_CREDENTIALS", 401)

    Produces:
        {
            "success": false,
            "message": "Invalid email or password",
            "error": "INVALID_CREDENTIALS"
        }
    """
    response = {
        "success": False,
        "message": message,
        "error": error_code
    }
    return jsonify(response), status_code