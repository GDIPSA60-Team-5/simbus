import json
import re
import sys
import time

def typewriter_print(text, delay=0.015):
    for char in text:
        sys.stdout.write(char)
        sys.stdout.flush()
        time.sleep(delay)
    print()


def merge_slots(current_slots, new_slots):
    for key, value in new_slots.items():
        if value:
            current_slots[key] = value


def extract_json_from_response(text):
    try:
        json_str = re.search(r'\{.*\}', text, re.DOTALL).group(0)
        return json.loads(json_str)
    except Exception as e:
        print(f"[ERROR extracting JSON]: {e}")
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


def get_user_context(user_id, user_conversations, REQUIRED_SLOTS):
    if user_id not in user_conversations:
        user_conversations[user_id] = {
            "state": {
                "intent": None,
                "slots": {slot: None for slot in set(s for lst in REQUIRED_SLOTS.values() for s in lst)}
            },
            "history": []
        }
    return user_conversations[user_id]
