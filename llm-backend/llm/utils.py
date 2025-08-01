import json
import re
import ast
import sys
import time as timing
from datetime import datetime, date
from llm.state import SLOT_TYPES

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
                        current_slots[key] = None  # invalidate slot to trigger re-prompt
                        continue
                current_slots[key] = converted_value
            else:
                current_slots[key] = None   # also invalidate


def extract_json_from_response(text):
    try:
        # Extract the first {...} JSON-looking substring (non-greedy)
        match = re.search(r'\{.*\}', text, re.DOTALL)
        if not match:
            raise ValueError("No JSON object found in response.")
        
        json_str = match.group(0).strip().lstrip("\ufeff")

        # Check if it ends correctly, otherwise try to fix
        open_braces = json_str.count("{")
        close_braces = json_str.count("}")
        if open_braces > close_braces:
            json_str += "}" * (open_braces - close_braces)

        # Unescape if string-literal wrapped
        if (json_str.startswith('"') and json_str.endswith('"')) or \
            (json_str.startswith("'") and json_str.endswith("'")):
            try:
                json_str = ast.literal_eval(json_str)
            except Exception as e:
                print(f"[ERROR evaluating string literal]: {e}")
                print("LLM returned:\n", repr(json_str))
                return None

        return json.loads(json_str)

    except json.JSONDecodeError as e:
        print(f"[ERROR decoding JSON]: {e}")
        print("LLM returned:\n", repr(text))
    except Exception as e:
        print(f"[ERROR extracting JSON]: {e}")
        print("LLM returned:\n", repr(text))

    return None


def show_help():
    typewriter_print("""
🧭 Here's what I can help you with:

1. 🚍 Route Info (`route_info`)
    - Ask me how to get from one place to another.
    - Example: "How do I get from downtown to the airport?"

2. 🗓️ Schedule a Commute (`schedule_commute`)
    - Let me know when you need to arrive, and I’ll plan the timing.
    - Example: "I want to reach work by 9 AM. Notify me when to leave."

3. 🚌 Next Bus Timing (`next_bus`)
    - Just give me the bus number and I’ll tell you when the next one arrives.
    - Example: "When is the next A2 bus?"

Just ask a question and I’ll guide you step by step!
""")


def get_recent_history(conversation_history, MAX_HISTORY_LENGTH):
    return conversation_history[-MAX_HISTORY_LENGTH:]


def get_user_context(user_id, user_conversations):
    if user_id not in user_conversations:
        user_conversations[user_id] = {
            "state": {
                "intent": None,
                "slots": {slot: None for slot in SLOT_TYPES}
            },
            "history": []
        }
    return user_conversations[user_id]


def convert_slot_value(slot, value):
    expected_type = SLOT_TYPES.get(slot)

    if expected_type is datetime:
        if not isinstance(value, str):
            return None

        value = value.strip().lower()
        # Try parsing ISO 8601 directly
        try:
            return datetime.fromisoformat(value)
        except ValueError:
            pass

        # Handle only-time formats and assume today's date
        time_formats = ["%H:%M", "%I:%M %p", "%I %p"]
        for fmt in time_formats:
            try:
                parsed_time = datetime.strptime(value, fmt).time()
                return datetime.combine(date.today(), parsed_time)
            except ValueError:
                continue

        return None

    elif expected_type is str:
        return str(value).strip()

    return value


def validate_future_datetime(dt_value):
    if isinstance(dt_value, datetime):
        return dt_value > datetime.now()
    return True  # Non-datetime values are assumed valid


def current_datetime():
    return datetime.now().isoformat(timespec='seconds')


def serialize_for_json(obj):
    if isinstance(obj, dict):
        return {k: serialize_for_json(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [serialize_for_json(item) for item in obj]
    elif isinstance(obj, datetime):
        return obj.isoformat()
    return obj
