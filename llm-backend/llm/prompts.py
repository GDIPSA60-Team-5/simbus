import json
from llm.utils import serialize_for_json


def build_extraction_prompt(predicted_intent, required_slots, history, allowed_locations=None):
    
    # Show allowed locations if intent is schedule_commute
    locations_text = ""
    if predicted_intent == "schedule_commute" and allowed_locations:
        locations_text = (
            "\n- Only select locations from the allowed list below. "
            "Do not use or infer any other location.\n"
            f"Allowed locations: {', '.join(allowed_locations)}\n"
        )
        
    general_rules = f"""
Rules:
- Locations are expressed as names (e.g., "NUS", "Orchard MRT", "Current Location").{locations_text}
- Return all time values in 24-hour HH:mm format (e.g., "08:30").
- For recurring days, extract any mention of days of the week or phrases like 'every weekday' or 'weekends', and map them to short day codes.
- Do not include a slot in the output if its value cannot be extracted.
""".strip()

    # Define intent-specific examples
    examples = {
        "schedule_commute": """  
Example:

User: "Remind me to leave home to school at 8 am every weekday"
JSON:
{
    "slots": {
        "start_location": "home",
        "end_location": "school",
        "notification_start_time": "08:00",
        "recurrence_days": ["mon", "tue", "wed", "thu", "fri"]
    }
}
""",
        "route_info": """
Example:

User: "How can I get from Clementi to Changi Airport?"
JSON:
{
    "slots": {
        "start_location": "Clementi",
        "end_location": "Changi Airport"
    }
}
""",
        "next_bus": """
Example:

User: "When is bus D1 arriving at university town?"
JSON:
{
    "slots": {
        "bus_service_number": "D1",
        "boarding_bus_stop_name": "University Town"
    }
}
""",
    }

    example_text = examples.get(predicted_intent, "")

    system_prompt = f"""
You are a helpful assistant extracting information ("slots") from a multi-turn conversation.

Current intent: "{predicted_intent}"

Extract these slots if available:
{json.dumps(required_slots, indent=2)}

{general_rules}

{example_text}

Respond with a JSON object containing only a "slots" field.
""".strip()

    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\n\nConversation:\n{dialogue}\n\nJSON:"


def build_followup_prompt(intent, current_slots, history, missing_slots, allowed_locations=None):
    
    # Show allowed locations if intent is schedule_commute
    locations_text = ""
    if intent == "schedule_commute" and allowed_locations:
        locations_text = (
            "\n- Only select locations from the allowed list below. "
            "Do not use or infer any other location.\n"
            f"Allowed locations: {', '.join(allowed_locations)}"
        )
    
    # Base detailed extraction rules & missing slots info (always included)
    base_message = f"""
Ask the user for the missing slot(s):
{json.dumps(missing_slots, indent=2)}

Rules:
- Locations are expressed as names (e.g., "NUS", "Orchard MRT", "Current Location").{locations_text}
- Return all time values in 24-hour HH:mm format (e.g., "08:30").
- For recurring days, extract any mention of days of the week or phrases like 'every weekday' or 'weekends', and map them to short day codes (eg. ["sat", "sun", "mon"]).
- Do not include a slot in the output if its value cannot be extracted.- Do not include a slot in the output if its value cannot be extracted.
    """.strip()

    # Build a custom status message
    if set(missing_slots) == {"boarding_bus_stop_name", "boarding_bus_stop_code"}:
        status_message = (
            "Ask the user for either the boarding bus stop name or code.\n\n"
            + base_message
        )
    else:
        status_message = base_message

    system_prompt = f"""
You are an assistant helping with the intent "{intent}".

Current slot values:
{json.dumps(serialize_for_json(current_slots), indent=2)}

{status_message}

Only respond with the assistant's next message.
""".strip()

    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\n\nConversation:\n{dialogue}\n\nAssistant:"
