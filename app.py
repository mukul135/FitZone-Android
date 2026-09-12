from flask import Flask, request, render_template, redirect, session, url_for
import mysql.connector
from werkzeug.security import generate_password_hash, check_password_hash

import config
from utils.responses import success_response, error_response
from utils.auth import generate_token, token_required


app = Flask(__name__)
app.secret_key = config.SECRET_KEY


# ===============================
# DATABASE CONNECTION
# ===============================

db = None
cursor = None

try:
    db = mysql.connector.connect(
        host=config.DB_HOST,
        user=config.DB_USER,
        password=config.DB_PASSWORD,
        database=config.DB_NAME
    )

    cursor = db.cursor()
    print("Database connected successfully!")

except Exception as e:
    print("Database connection failed!")
    print("Error:", e)
# ===============================
# ROUTES
# ===============================

@app.route("/")
def home():
    return render_template("home.html")


@app.route("/about")
def about():
    return render_template("about.html")


@app.route("/plans")
def plans():
    return render_template("plans.html")


@app.route("/programs")
def programs():
    return render_template("programs.html")


@app.route("/gallery")
def gallery():
    return render_template("gallery.html")


# ===============================
# CONTACT
# ===============================

@app.route("/contact", methods=["GET", "POST"])
def contact():
    if request.method == "POST":
        name = request.form["name"]
        email = request.form["email"]
        phone = request.form["phone"]
        message = request.form["message"]

        sql = "INSERT INTO contact (name, email, phone, message) VALUES (%s, %s, %s, %s)"
        values = (name, email, phone, message)
        cursor.execute(sql, values)
        db.commit()

        return "Message Sent Successfully!"

    return render_template("contact.html")


# ===============================
# REGISTER
# ===============================

@app.route("/register", methods=["GET", "POST"])
def register():
    if request.method == "POST":
        fullname = request.form['fullname']
        email = request.form['email']
        mobile = request.form['mobile']
        password = request.form['password']
        dob = request.form['dob']
        gender = request.form['gender']
        height = request.form['height']
        weight = request.form['weight']
        goal = request.form['goal']
        plan = request.form['plan']
        medical_info = request.form['medical_info']
        emergency_name = request.form['emergency_name']
        emergency_number = request.form['emergency_number']

        hashed_password = generate_password_hash(password)
        sql = """
        INSERT INTO members 
        (fullname, email, mobile, password, dob, gender, height, weight, goal, plan, medical_info, emergency_name, emergency_number)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """

        values = (fullname, email, mobile, hashed_password, dob, gender,
                  height, weight, goal, plan,
                  medical_info, emergency_name, emergency_number)

        cursor.execute(sql, values)
        db.commit()

        return redirect(url_for("login"))

    return render_template("register.html")


@app.route("/register/<program_name>")
def register_program(program_name):
    return render_template("register.html", program=program_name)


# ===============================
# LOGIN
# ===============================

@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        email = request.form["email"]
        password = request.form["password"]

        sql = "SELECT * FROM members WHERE email=%s" 
        values = (email, )

        cursor.execute(sql, values)
        user = cursor.fetchone()

        if user and check_password_hash(user[4], password):
            session["user"] = user[1]   # fullname
            return redirect(url_for("dashboard"))
        else:
            return "Invalid Email or Password"

    return render_template("login.html")


# ===============================
# API: REGISTER (for Android)
# ===============================
# Same members table, same required fields as the web /register route.
# Difference: reads JSON instead of a form, returns JSON instead of a redirect,
# and hashes the password instead of storing it as plain text.

@app.route("/api/auth/register", methods=["POST"])
def api_register():
    data = request.get_json(silent=True)

    if not data:
        return error_response("Request body must be JSON", "INVALID_JSON", 400)

    required_fields = [
        "fullname", "email", "mobile", "password", "confirm_password",
        "dob", "gender", "height", "weight", "goal", "plan",
        "medical_info", "emergency_name", "emergency_number"
    ]
    missing = [f for f in required_fields if not data.get(f)]
    if missing:
        return error_response(
            f"Missing required field(s): {', '.join(missing)}",
            "MISSING_FIELDS", 400
        )

    if data["password"] != data["confirm_password"]:
        return error_response("Password and confirm password do not match",
                               "PASSWORD_MISMATCH", 400)

    if db is None:
        return error_response("Database unavailable, please try again later",
                               "DB_UNAVAILABLE", 500)

    try:
        cursor.execute("SELECT id FROM members WHERE email=%s", (data["email"],))
        if cursor.fetchone():
            return error_response("An account with this email already exists",
                                   "DUPLICATE_EMAIL", 409)

        hashed_password = generate_password_hash(data["password"])

        sql = """
        INSERT INTO members
        (fullname, email, mobile, password, dob, gender, height, weight, goal, plan, medical_info, emergency_name, emergency_number)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        values = (
            data["fullname"], data["email"], data["mobile"], hashed_password,
            data["dob"], data["gender"], data["height"], data["weight"],
            data["goal"], data["plan"], data["medical_info"],
            data["emergency_name"], data["emergency_number"]
        )
        cursor.execute(sql, values)
        db.commit()

        return success_response(message="Registration successful", status_code=201)

    except Exception:
        return error_response("Unable to complete registration", "SERVER_ERROR", 500)


# ===============================
# API: LOGIN (for Android)
# ===============================
# Same members table as the web /login route.
# Difference: reads JSON, checks a hashed password, and returns a token
# instead of setting a cookie session.

@app.route("/api/auth/login", methods=["POST"])
def api_login():
    data = request.get_json(silent=True)

    if not data or not data.get("email") or not data.get("password"):
        return error_response("Email and password are required",
                               "MISSING_FIELDS", 400)

    if db is None:
        return error_response("Database unavailable, please try again later",
                               "DB_UNAVAILABLE", 500)

    try:
        cursor.execute(
            "SELECT id, fullname, email, password FROM members WHERE email=%s",
            (data["email"],)
        )
        user = cursor.fetchone()

        if not user or not check_password_hash(user[3], data["password"]):
            return error_response("Invalid email or password",
                                   "INVALID_CREDENTIALS", 401)

        member_id, fullname, email = user[0], user[1], user[2]
        token = generate_token(member_id)

        return success_response(
            data={
                "token": token,
                "member": {
                    "id": member_id,
                    "fullname": fullname,
                    "email": email
                }
            },
            message="Login successful"
        )

    except Exception:
        return error_response("Unable to process login", "SERVER_ERROR", 500)

# ===============================
# API: PROFILE (for Android)
# ===============================
# Protected route — requires a valid token from /api/auth/login.
# Returns the logged-in member's own data. Password is never selected/returned.

@app.route("/api/profile", methods=["GET"])
@token_required
def api_profile(member_id):
    if db is None:
        return error_response("Database unavailable, please try again later",
                               "DB_UNAVAILABLE", 500)

    try:
        cursor.execute(
            """
            SELECT id, fullname, email, mobile, dob, gender, height, weight,
                   goal, plan, medical_info, emergency_name, emergency_number
            FROM members WHERE id=%s
            """,
            (member_id,)
        )
        user = cursor.fetchone()

        if not user:
            return error_response("Member not found", "NOT_FOUND", 404)

        profile = {
            "id": user[0],
            "fullname": user[1],
            "email": user[2],
            "mobile": user[3],
            "dob": str(user[4]) if user[4] else None,
            "gender": user[5],
            "height": user[6],
            "weight": user[7],
            "goal": user[8],
            "plan": user[9],
            "medical_info": user[10],
            "emergency_name": user[11],
            "emergency_number": user[12]
        }

        return success_response(data=profile, message="Profile fetched successfully")

    except Exception:
        return error_response("Unable to fetch profile", "SERVER_ERROR", 500)

# ===============================
# API: MEMBERSHIP (for Android)
# ===============================
# Protected route — returns only what the original app actually tracks:
# the member's selected plan string. No expiry/payment/renewal fields exist
# in the database, so none are invented here (Phase 1 §8, Phase 2 Decision 1).

@app.route("/api/membership", methods=["GET"])
@token_required
def api_membership(member_id):
    if db is None:
        return error_response("Database unavailable, please try again later",
                               "DB_UNAVAILABLE", 500)

    try:
        cursor.execute(
            "SELECT id, fullname, plan FROM members WHERE id=%s",
            (member_id,)
        )
        user = cursor.fetchone()

        if not user:
            return error_response("Member not found", "NOT_FOUND", 404)

        membership = {
            "member_id": user[0],
            "fullname": user[1],
            "plan": user[2]
        }

        return success_response(data=membership, message="Membership fetched successfully")

    except Exception:
        return error_response("Unable to fetch membership", "SERVER_ERROR", 500)

    # ===============================
# API: UPDATE MEMBERSHIP (for Android)
# ===============================
# Protected route — lets the authenticated member change their plan.
# This is the ONLY membership mutation that exists; there is still no
# payment step, no expiry, no renewal/cancellation logic (Phase 9 Decision).

VALID_PLAN_DURATIONS = ["1 Month", "3 Months", "6 Months", "12 Months"]

@app.route("/api/membership/update", methods=["POST"])
@token_required
def api_membership_update(member_id):
    if db is None:
        return error_response("Database unavailable, please try again later",
                               "DB_UNAVAILABLE", 500)

    data = request.get_json(silent=True)
    if not data or "plan" not in data:
        return error_response("Missing 'plan' field", "MISSING_FIELDS", 400)

    new_plan = data["plan"]

    if new_plan not in VALID_PLAN_DURATIONS:
        return error_response(
            f"Invalid plan. Must be one of: {', '.join(VALID_PLAN_DURATIONS)}",
            "INVALID_PLAN", 400
        )

    try:
        cursor.execute(
            "UPDATE members SET plan=%s WHERE id=%s",
            (new_plan, member_id)
        )
        db.commit()

        return success_response(
            data={"member_id": member_id, "plan": new_plan},
            message="Membership updated successfully"
        )

    except Exception:
        db.rollback()
        return error_response("Unable to update membership", "SERVER_ERROR", 500)

# ===============================
# API: PLANS (for Android)
# ===============================
# No authentication needed — same static plan data currently hardcoded
# in plans.html (Phase 1 §5, §8). Not stored in the database.

@app.route("/api/plans", methods=["GET"])
def api_plans():
    plans = [
        {
            "id": "monthly",
            "name": "Monthly",
            "price": 1500,
            "duration": "1 Month",
            "best_value": False
        },
        {
            "id": "quarterly",
            "name": "Quarterly",
            "price": 4000,
            "duration": "3 Months",
            "best_value": False
        },
        {
            "id": "semi_annual",
            "name": "Semi-Annual",
            "price": 7500,
            "duration": "6 Months",
            "best_value": False
        },
        {
            "id": "yearly",
            "name": "Yearly",
            "price": 14000,
            "duration": "12 Months",
            "best_value": True
        }
    ]

    return success_response(data={"plans": plans}, message="Plans fetched successfully")

# ===============================
# API: PROGRAMS (for Android)
# ===============================
# No authentication needed — matches the 7 programs and exact descriptions
# hardcoded in programs.html (Phase 1 §5, §9). Not stored in the database.
# "id" matches the exact slug used in programs.html's /register/<slug> links,
# so Android can pass it straight through to program-specific registration.

@app.route("/api/programs", methods=["GET"])
def api_programs():
    programs = [
        {"id": "Weight-Training", "name": "Weight Training",
         "description": "Build muscle and increase strength with structured weight lifting programs."},
        {"id": "Cardio-Training", "name": "Cardio Training",
         "description": "Improve stamina and heart health with treadmill, cycling, and HIIT workouts."},
        {"id": "Yoga-Flexibility", "name": "Yoga & Flexibility",
         "description": "Enhance flexibility, reduce stress, and improve mental focus."},
        {"id": "Personal-Training", "name": "Personal Training",
         "description": "One-on-one coaching tailored to your fitness goals."},
        {"id": "Muscle-Gain-Program", "name": "Muscle Gain Program",
         "description": "12-week structured strength training program focused on hypertrophy."},
        {"id": "Fat-Loss-Program", "name": "Fat Loss Program",
         "description": "8-week transformation plan combining HIIT, cardio, and diet guidance."},
        {"id": "Endurance-Training", "name": "Endurance Training",
         "description": "10-week advanced conditioning program to improve stamina and performance."}
    ]

    return success_response(data={"programs": programs}, message="Programs fetched successfully")

# ===============================
# API: PROGRAM-SPECIFIC REGISTRATION (for Android)
# ===============================
# Fixes the original 405 bug: /register/<program_name> was GET-only,
# so submitting a registration from a program-specific entry point
# always failed. This does the same job as /api/auth/register, but also
# stores which program the member registered from (Phase 2 Decision 2).

VALID_PROGRAM_IDS = {
    "Weight-Training", "Cardio-Training", "Yoga-Flexibility",
    "Personal-Training", "Muscle-Gain-Program", "Fat-Loss-Program",
    "Endurance-Training"
}

@app.route("/api/register/<program_name>", methods=["POST"])
def api_register_program(program_name):
    if program_name not in VALID_PROGRAM_IDS:
        return error_response("Unknown program", "INVALID_PROGRAM", 400)

    data = request.get_json(silent=True)

    if not data:
        return error_response("Request body must be JSON", "INVALID_JSON", 400)

    required_fields = [
        "fullname", "email", "mobile", "password", "confirm_password",
        "dob", "gender", "height", "weight", "goal", "plan",
        "medical_info", "emergency_name", "emergency_number"
    ]
    missing = [f for f in required_fields if not data.get(f)]
    if missing:
        return error_response(
            f"Missing required field(s): {', '.join(missing)}",
            "MISSING_FIELDS", 400
        )

    if data["password"] != data["confirm_password"]:
        return error_response("Password and confirm password do not match",
                               "PASSWORD_MISMATCH", 400)

    if db is None:
        return error_response("Database unavailable, please try again later",
                               "DB_UNAVAILABLE", 500)

    try:
        cursor.execute("SELECT id FROM members WHERE email=%s", (data["email"],))
        if cursor.fetchone():
            return error_response("An account with this email already exists",
                                   "DUPLICATE_EMAIL", 409)

        hashed_password = generate_password_hash(data["password"])

        sql = """
        INSERT INTO members
        (fullname, email, mobile, password, dob, gender, height, weight, goal, plan,
         medical_info, emergency_name, emergency_number, program_interest)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        values = (
            data["fullname"], data["email"], data["mobile"], hashed_password,
            data["dob"], data["gender"], data["height"], data["weight"],
            data["goal"], data["plan"], data["medical_info"],
            data["emergency_name"], data["emergency_number"], program_name
        )
        cursor.execute(sql, values)
        db.commit()

        return success_response(message="Registration successful", status_code=201)

    except Exception:
        return error_response("Unable to complete registration", "SERVER_ERROR", 500)

# ===============================
# API: CONTACT (for Android)
# ===============================
# No authentication needed — same contact table as the web /contact route
# (Phase 1 §7). Difference: reads JSON, returns JSON instead of raw text.

@app.route("/api/contact", methods=["POST"])
def api_contact():
    data = request.get_json(silent=True)

    if not data:
        return error_response("Request body must be JSON", "INVALID_JSON", 400)

    required_fields = ["name", "email", "message"]
    missing = [f for f in required_fields if not data.get(f)]
    if missing:
        return error_response(
            f"Missing required field(s): {', '.join(missing)}",
            "MISSING_FIELDS", 400
        )

    name = data["name"]
    email = data["email"]
    phone = data.get("phone", "")
    message = data["message"]

    if db is None:
        return error_response("Database unavailable, please try again later",
                               "DB_UNAVAILABLE", 500)

    try:
        sql = "INSERT INTO contact (name, email, phone, message) VALUES (%s, %s, %s, %s)"
        values = (name, email, phone, message)
        cursor.execute(sql, values)
        db.commit()

        return success_response(message="Message sent successfully", status_code=201)

    except Exception:
        return error_response("Unable to send message", "SERVER_ERROR", 500)


# ===============================
# DASHBOARD (Protected Page)
# ===============================

@app.route("/dashboard")
def dashboard():
    if "user" not in session:
        return redirect(url_for("login"))

    return render_template("dashboard.html", user=session["user"])


# ===============================
# BMI CALCULATOR
# ===============================

@app.route("/bmi", methods=["GET", "POST"])
def bmi():
    if "user" not in session:
        return redirect(url_for("login"))

    result = None

    if request.method == "POST":
        try:
            height = float(request.form["height"])
            weight = float(request.form["weight"])

            # Convert cm to meters
            height = height / 100  

            bmi_value = weight / (height * height)
            result = round(bmi_value, 2)

        except:
            result = "Invalid Input"

    return render_template("bmi.html", result=result)


# ===============================
# SMART CALORIE CALCULATOR
# ===============================


@app.route("/calorie", methods=["GET", "POST"])
def calorie():
    if "user" not in session:
        return redirect(url_for("login"))

    result = None

    if request.method == "POST":
        weight = float(request.form["weight"])
        height = float(request.form["height"])
        age = int(request.form["age"])
        gender = request.form["gender"]
        activity = float(request.form["activity"])
        goal = request.form["goal"]

        # BMR Calculation
        if gender == "Male":
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5
        else:
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161

        # TDEE
        calories = bmr * activity

        # Goal Adjustment
        if goal == "Weight Loss":
            calories -= 500
        elif goal == "Muscle Gain":
            calories += 400

        result = round(calories)

    return render_template("calorie.html", result=result)


# ===============================
# DIET PLANNER
# ===============================

@app.route("/diet", methods=["GET", "POST"])
def diet():
    if "user" not in session:
        return redirect(url_for("login"))

    plan = None

    if request.method == "POST":
        calories = int(request.form["calories"])
        goal = request.form["goal"]

        if goal == "Weight Loss":
            protein_percent = 0.40
            carb_percent = 0.35
            fat_percent = 0.25
        elif goal == "Muscle Gain":
            protein_percent = 0.35
            carb_percent = 0.45
            fat_percent = 0.20
        else:
            protein_percent = 0.30
            carb_percent = 0.40
            fat_percent = 0.30

        protein = round((calories * protein_percent) / 4)
        carbs = round((calories * carb_percent) / 4)
        fats = round((calories * fat_percent) / 9)

        plan = {
            "calories": calories,
            "protein": protein,
            "carbs": carbs,
            "fats": fats,
            "goal": goal
        }

    return render_template("diet.html", plan=plan)


# ===============================
# AI WORKOUT GENERATOR
# ===============================
@app.route("/ai-workout", methods=["GET", "POST"])
def ai_workout():
    if "user" not in session:
        return redirect(url_for("login"))

    workout_plan = None

    if request.method == "POST":
        goal = request.form["goal"]
        days = int(request.form["days"])
        level = request.form["level"]

        workout_plan = generate_workout(goal, days, level)

    return render_template("ai_workout.html", workout_plan=workout_plan)


# ===============================
# WORKOUT GENERATOR FUNCTION
# ===============================
def generate_workout(goal, days, level):

    plan = {}

    # ==============================
    # MUSCLE GAIN
    # ==============================
    if goal == "Muscle Gain":

        if days >= 6:
            split = ["Push", "Pull", "Legs", "Push", "Pull", "Legs"]
        elif days == 5:
            split = ["Chest", "Back", "Legs", "Shoulders", "Arms"]
        else:
            split = ["Upper Body", "Lower Body", "Rest", "Upper Body"]

        exercises = {
            "Push": "Flat Bench Press 3x10,\n"
                    "Incline Bench Press 3x10,"
                    "Chest Flies 3x10,"
                    "Shoulder Press 3x12,"
                    "Lateral Raises 3x10,"
                    "Tricep Pushdowns 3x12,"
                    "French Curls 3x10",
            "Pull": "Pull-ups 2x8, Barbell Rows 3x10,Lat Pulldown 3x10 , Bicep Curls 3x12, Hammer Curls 3x12",
            "Legs": "Squats 4x8, Leg Press 3x12, Hamstring Curls 3x12,Leg Extensions 3x12, Calf Raises 2x15",
            "Chest": "Incline Press 3x10, Chest Fly 3x12,Pushups 3x15",
            "Back": "Lat Pulldown 4x10, Seated Row 3x12,Deadlifts 3x8",
            "Shoulders": "Overhead Press 4x8, Lateral Raises 3x15,Rear Delt Fly 3x12",
            "Arms": "Barbell Curl 3x12, Tricep Pushdown 3x12,Preacher Curl 3x10, Skull Crushers 3x10",
            "Upper Body": "Bench Press, Rows, Shoulder Press (3x10 each)",
            "Lower Body": "Squats, Lunges, Leg Curl (3x12 each)"
        }

    # ==============================
    # WEIGHT LOSS
    # ==============================
    elif goal == "Weight Loss":

        split = ["Cardio & Core", "Full Body Strength"] * (days // 2)
        if days % 2 != 0:
            split.append("Cardio & Core")

        exercises = {
            "Cardio & Core": "30 mins Treadmill / Cycling + 15 mins Ab Circuit (Planks, Crunches)",
            "Full Body Strength": "Dumbbell Squats 3x15, Push-ups 3x15, Dumbbell Rows 3x15, Core Workout"
        }

    # ==============================
    # STRENGTH
    # ==============================
    elif goal == "Strength":

        split = ["Heavy Upper", "Heavy Lower"] * (days // 2)

        exercises = {
            "Heavy Upper":
            "Bench Press 5x5, Pull-ups 5x5, Overhead Press 5x5",

            "Heavy Lower":
            "Squats 5x5, Deadlift 5x5, Leg Press 4x8"
        }

    # ==============================
    # EXPERIENCE LEVEL ADJUSTMENT
    # ==============================

    volume_note = ""

    if level == "Beginner":
        volume_note = "Focus on form. Moderate weight."
    elif level == "Intermediate":
        volume_note = "Progressive overload recommended."
    elif level == "Advanced":
        volume_note = "High intensity. Add drop sets & supersets."

    # ==============================
    # BUILD FINAL PLAN
    # ==============================

    for i in range(len(split)):
        day_name = f"Day {i+1}"
        workout_type = split[i]

        if workout_type in exercises:
            plan[day_name] = exercises[workout_type] + " | " + volume_note
        else:
            plan[day_name] = "Rest or Active Recovery"

    return plan



# ===============================
# LOGOUT
# ===============================

@app.route("/logout")
def logout():
    session.pop("user", None)
    return redirect(url_for("home"))


# ===============================
# GLOBAL API ERROR HANDLERS
# ===============================
# Flask's default 404/405 pages are HTML, which breaks Android's JSON
# parsing. These convert them to the same JSON shape every other API
# error already uses, via the existing error_response() helper.
# Web routes (non-/api/) are untouched — they still get Flask's normal
# HTML error pages, which is fine since only humans hit those in a browser.

@app.errorhandler(404)
def handle_404(e):
    if request.path.startswith("/api/"):
        return error_response("Endpoint not found", "NOT_FOUND", 404)
    return e

@app.errorhandler(405)
def handle_405(e):
    if request.path.startswith("/api/"):
        return error_response("Method not allowed for this endpoint", "METHOD_NOT_ALLOWED", 405)
    return e


# ===============================
# RUN APP
# ===============================

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True)