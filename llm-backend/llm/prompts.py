import json
from llm.utils import current_datetime, serialize_for_json


def build_extraction_prompt(predicted_intent, required_slots, history):
    general_rules = """
Rules:
- Locations are expressed as names (e.g., "NUS-ISS", "Orchard MRT", "Current Location").
- Return all datetime values in ISO 8601 (e.g., "2025-08-01T09:00:00").
- Do not include a slot in the output if its value cannot be extracted.
""".strip()

    # Define intent-specific examples
    examples = {
        "schedule_commute": """
Example:

User: "Notify me when I should leave to arrive at YIH by 10 AM tomorrow"
JSON:
{
    "slots": {
        "start_location": "Current Location",
        "end_location": "YIH",
        "arrival_time": "yyyy-mm-ddT10:00:00"
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

Context:
- Current datetime: {current_datetime()}

{general_rules}

{example_text}

Respond with a JSON object containing only a "slots" field.
""".strip()

    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\n\nConversation:\n{dialogue}\n\nJSON:"


def build_followup_prompt(intent, current_slots, history, missing_slots):
    # Base detailed extraction rules & missing slots info (always included)
    base_message = f"""
Ask the user for the missing slot(s):
{json.dumps(missing_slots, indent=2)}

Context:
- Current datetime: {current_datetime()}

Rules:
- Locations are expressed as names (e.g., "NUS-ISS", "Orchard MRT", "Current Location").
- Return all datetime values in ISO 8601 (e.g., "2025-08-01T09:00:00").
- Do not include a slot in the output if its value cannot be extracted.
    """.strip()

    # Build a custom status message
    if set(missing_slots) == {"boarding_bus_stop_name", "boarding_bus_stop_code"}:
        status_message = (
            "Ask the user for either the boarding bus stop name or code.\n\n"
            + base_message
        )
    elif set(missing_slots) == {"notification_start_time", "arrival_time"}:
        status_message = (
            "Please ask the user to specify either their desired arrival time or when they want to be notified.\n\n"
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
