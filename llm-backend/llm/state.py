import os
from dotenv import load_dotenv
from datetime import time
from typing import List


# ---- CONVERSATION HISTORY LENGTH TO KEEP ----
MAX_HISTORY_LENGTH = 4

# ---- INTENT DETECTION GUARD ----
CONFIDENCE_THRESHOLD = 0.38
MIN_WORDS_FOR_INTENT = 7

# ---- LOAD BACKEND URL ----
load_dotenv()
BACKEND_URL = os.getenv("BACKEND_URL")

# ---- INTENTS AND DESCRIPTIONS ----
INTENTS = ["route_info", "schedule_commute", "next_bus", "help", "reset"]

INTENT_DESCRIPTIONS = {
    "route_info": "Help users find directions from a start location to a destination.",
    "schedule_commute": "Plan a recurring commute with start and destination locations and a notification time.",
    "next_bus": "Provide arrival time for the next bus given a bus service number and optionally boarding stop info.",
    "help": "Provide guidance on how to use the assistant.",
    "reset": "Clear the current conversation context to start fresh.",
}

REQUIRED_SLOTS = {
    "route_info": ["start_location", "end_location"],
    "schedule_commute": [
        "start_location",
        "end_location",
        "notification_start_time",
        "recurrence_days"   #eg. ["mon", "tue", "wed", "thu", "fri"]
    ],
    "next_bus": [
        "bus_service_number",
        ["boarding_bus_stop_name", "boarding_bus_stop_code"],
    ],
    "help": [],
    "reset": [],
}

SLOT_TYPES = {
    "start_location": str,
    "end_location": str,
    "notification_start_time": time,
    "recurrence_days": List[str],
    "bus_service_number": str,
    "boarding_bus_stop_name": str,
    "boarding_bus_stop_code": str,
}

user_conversations = {}
