from datetime import datetime

INTENTS = ["route_info", "schedule_commute", "next_bus", "help", "reset"]

REQUIRED_SLOTS = {
    "route_info": ["start_location", "destination"],
    "schedule_commute": ["start_location", "destination", "notification_start_time", "arrival_time"],
    "next_bus": ["bus_service_number", ["boarding_bus_stop_name", "boarding_bus_stop_code"]],
    "help": [],
    "reset": []
}

SLOT_TYPES = {
    "start_location": str,
    "destination": str,
    "notification_start_time": datetime,
    "arrival_time": datetime,
    "bus_service_number": str,
    "boarding_bus_stop_name": str,
    "boarding_bus_stop_code": str
}

user_conversations = {}

MAX_HISTORY_LENGTH = 7

def reset_conversation_for_user(user_id):
    if user_id in user_conversations:
        user_conversations[user_id]["state"] = {
            "intent": None,
            "slots": {slot: None for slot in SLOT_TYPES}
        }
        user_conversations[user_id]["history"] = []
