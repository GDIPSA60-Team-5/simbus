import json
import re
import ast
import sys
import time as timing
import requests
from datetime import datetime, time
import pytz
from typing import List, Dict
from llm.state import SLOT_TYPES, REQUIRED_SLOTS, user_conversations, BACKEND_URL


def typewriter_print(text, delay=0.015):
    for char in text:
        sys.stdout.write(char)
        sys.stdout.flush()
        timing.sleep(delay)
    print()


def merge_slots(current_slots, new_slots):
    for key, value in new_slots.items():
        if value is not None:
            converted_value = convert_slot_value(key, value)
            if converted_value is not None:
                if key in ["notification_start_time", "arrival_time"]:
                    if not validate_future_datetime(converted_value):
                        current_slots[key] = (
                            None  # invalidate slot to trigger re-prompt
                        )
                        continue
                current_slots[key] = converted_value
            else:
                current_slots[key] = None  # also invalidate


def extract_json_from_response(text):
    try:
        # Extract all JSON-like substrings and try parsing the first valid one
        json_candidates = re.findall(r"\{.*?\}", text, re.DOTALL)

        for candidate in json_candidates:
            try:
                cleaned = candidate.strip().lstrip("\ufeff")

                # Fix unbalanced braces
                open_braces = cleaned.count("{")
                close_braces = cleaned.count("}")
                if open_braces > close_braces:
                    cleaned += "}" * (open_braces - close_braces)

                # Unescape if needed
                if (cleaned.startswith('"') and cleaned.endswith('"')) or (
                    cleaned.startswith("'") and cleaned.endswith("'")
                ):
                    cleaned = ast.literal_eval(cleaned)

                return json.loads(cleaned)

            except json.JSONDecodeError:
                continue  # Skip and try the next one

        raise ValueError("No valid JSON object found.")

    except Exception as e:
        print(f"[ERROR extracting JSON]: {e}")
        print("LLM returned:\n", repr(text))
        return None


def show_help(user_name):
    return f"""
Hi {user_name}, here’s what I can help you with:

1. Get directions (e.g., “How do I get from Clementi Mall to Changi Airport?”)
2. Plan your trip to arrive on time (e.g., “Remind me to leave Bukit Panjang to NUS at 08:00 every Monday, Wednesday, and Friday.”)
3. Check bus arrival times (e.g., “When is bus D1 arriving at University Town?”)
4. Restart the conversation if needed (e.g., “Reset.”)

What would you like to do?
"""


def get_recent_history(conversation_history, MAX_HISTORY_LENGTH):
    return conversation_history[-MAX_HISTORY_LENGTH:]


def get_user_context(user_name):
    if user_name not in user_conversations:
        user_conversations[user_name] = {
            "state": {"intent": None, "slots": {slot: None for slot in SLOT_TYPES}},
            "history": [],
            "current_location": {},
        }
    return user_conversations[user_name]


def reset_conversation_for_user(user_name):
    if user_name in user_conversations:
        user_conversations[user_name]["state"] = {
            "intent": None,
            "slots": {slot: None for slot in SLOT_TYPES},
        }
        user_conversations[user_name]["history"] = []


def convert_slot_value(slot, value):
    expected_type = SLOT_TYPES.get(slot)

    if expected_type is time:
        if not isinstance(value, str):
            return None

        value = value.strip().lower()
        # Try parsing time in multiple formats
        time_formats = ["%H:%M", "%I:%M %p", "%I %p"]
        for fmt in time_formats:
            try:
                return datetime.strptime(value, fmt).time()
            except ValueError:
                continue
        return None

    elif expected_type is str:
        return str(value).strip()

    elif expected_type == List[str]:
        if isinstance(value, str):
            # Assume comma-separated string: "mon, wed, fri"
            return [v.strip().lower() for v in value.split(",") if v.strip()]
        elif isinstance(value, list):
            return [str(v).strip().lower() for v in value]
        else:
            return []

    return value


def validate_future_datetime(dt_value):
    if isinstance(dt_value, datetime):
        return dt_value > datetime.now()
    return True  # Non-datetime values are assumed valid


def current_datetime():
    sgt = pytz.timezone("Asia/Singapore")
    return datetime.now(sgt)  # .isoformat(timespec='seconds')


def serialize_for_json(obj):
    if isinstance(obj, dict):
        return {k: serialize_for_json(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [serialize_for_json(item) for item in obj]
    elif isinstance(obj, datetime):
        return obj.isoformat()
    elif isinstance(obj, time):
        return obj.strftime("%H:%M")  # Convert time to "HH:mm" string
    return obj


def find_missing_slots(intent, current_slots):
    required = REQUIRED_SLOTS.get(intent, [])
    missing = []

    for slot in required:
        if isinstance(slot, list):
            # slot group means at least one must be present
            if not any(current_slots.get(s) for s in slot):
                missing.extend(slot)  # all alternatives missing, so ask for all
        else:
            if not current_slots.get(slot):
                missing.append(slot)
    return missing


def flatten_slots(required_slots):
    flat = []
    for slot in required_slots:
        if isinstance(slot, list):
            flat.extend(slot)
        else:
            flat.append(slot)
    return flat


def get_user_saved_locations(jwt_token: str) -> Dict[str, str]:
    """
    Fetch user's saved locations and return a mapping from name -> id.
    """
    url = f"{BACKEND_URL}/api/locations"
    headers = {"Authorization": jwt_token}

    try:
        response = requests.get(url, headers=headers, timeout=5)
        response.raise_for_status()
        locations = response.json()  # expecting a list of location objects
        # Map name to id
        location_map = {loc["locationName"]: loc["id"] for loc in locations if "locationName" in loc and "id" in loc}
        return location_map
    except requests.RequestException as e:
        print(f"Failed to fetch user locations: {e}")
        return {}
