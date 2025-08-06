from datetime import datetime


MAX_HISTORY_LENGTH = 7

# ---- INTENT DETECTION GUARD ----
CONFIDENCE_THRESHOLD = 0.3
MIN_WORDS_FOR_INTENT = 7

INTENTS = ["route_info", "schedule_commute", "next_bus", "help", "reset"]

INTENT_DESCRIPTIONS = {
    "route_info": "Help users find directions from a start location to a destination.",
    "schedule_commute": "Plan when to leave based on start, destination, arrival time, and notification timing.",
    "next_bus": "Provide arrival time for the next bus given a bus service number and optionally boarding stop info.",
    "help": "Provide guidance on how to use the assistant.",
    "reset": "Clear the current conversation context to start fresh."
}

REQUIRED_SLOTS = {
    "route_info": ["start_location", "end_location"],
    "schedule_commute": ["start_location", "end_location", ["notification_start_time", "arrival_time"]],
    "next_bus": ["bus_service_number", ["boarding_bus_stop_name", "boarding_bus_stop_code"]],
    "help": [],
    "reset": []
}

SLOT_TYPES = {
    "start_location": str,
    "end_location": str,
    "notification_start_time": datetime,
    "arrival_time": datetime,
    "bus_service_number": str,
    "boarding_bus_stop_name": str,
    "boarding_bus_stop_code": str
}

user_conversations = {}

def reset_conversation_for_user(user_id):
    if user_id in user_conversations:
        user_conversations[user_id]["state"] = {
            "intent": None,
            "slots": {slot: None for slot in SLOT_TYPES}
        }
        user_conversations[user_id]["history"] = []
