INTENTS = ["route_info", "schedule_commute", "next_bus", "help", "reset"]

REQUIRED_SLOTS = {
    "route_info": ["start_location", "destination"],
    "schedule_commute": ["start_location", "destination", "notification_start_time", "arrival_time"],
    "next_bus": ["bus_number"],
    "help": [],
    "reset": []
}

conversation_state = {
    "intent": None,
    "slots": {slot: None for slot in set(s for lst in REQUIRED_SLOTS.values() for s in lst)}
}

user_conversations = {}

MAX_HISTORY_LENGTH = 7

def reset_conversation_for_user(user_id):
    if user_id in user_conversations:
        user_conversations[user_id]["state"] = {"intent": None, "slots": {}}
        user_conversations[user_id]["history"] = []
