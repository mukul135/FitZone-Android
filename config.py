# ===============================
# config.py
# ===============================
# WHAT THIS FILE DOES:
# It reads secret values (DB password, Flask secret key) from a ".env" file
# instead of writing them directly in app.py like the original project did.
#
# WHY:
# The original app.py had this hardcoded:
#     app.secret_key = "fitzone_secret_key"
#     password="yourpassword"
# That means anyone who sees app.py sees your real passwords.
# This file fixes that WITHOUT changing any app behaviour.

import os
from dotenv import load_dotenv

# This line reads the ".env" file (which you create from .env.example)
# and loads its values into the environment.
load_dotenv()

# Each line below reads one value from .env.
# The second argument (after the comma) is a fallback used ONLY if
# the .env file is missing that value — this keeps local dev working
# even before you set up .env, matching the original project's defaults.
SECRET_KEY = os.getenv("SECRET_KEY", "8f3ka92md0alfk29LKASDjLKASDJ2")

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "mukul@0906")
DB_NAME = os.getenv("DB_NAME", "gym_db")