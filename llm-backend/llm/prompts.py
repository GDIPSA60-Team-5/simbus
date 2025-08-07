import json
from llm.utils import current_datetime, serialize_for_json
from llm.state import INTENT_DESCRIPTIONS, REQUIRED_SLOTS
from typing import Dict, Any


def build_extraction_prompt(predicted_intent, required_slots, history, current_location):
    general_rules = """
Rules:
- Locations are expressed as names (e.g., "NUS-ISS", "Orchard MRT", "current location").
- Return all datetime values in ISO 8601 (e.g., "2025-08-01T09:00:00").
- Do not include a slot in the output if its value cannot be extracted.
""".strip()

    system_prompt = f"""
You are a helpful assistant extracting information ("slots") from a multi-turn conversation.

Current intent: "{predicted_intent}"

Extract these slots if available:
{json.dumps(required_slots, indent=2)}

Context:
- Today's date: {current_datetime()}

{general_rules}

Respond with a JSON object containing only a "slots" field.
""".strip()

    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\n\nConversation:\n{dialogue}\n\nJSON:"


def build_followup_prompt(
    intent, current_slots, history, missing_slots, current_location
):
    if set(missing_slots) == {"boarding_bus_stop_name", "boarding_bus_stop_code"}:
        status_message = "Ask the user for either the boarding bus stop name or code."
    elif set(missing_slots) == {"notification_start_time", "arrival_time"}:
        status_message = "Ask the user for either when they want to be notified or their desired arrival time."
    else:
        status_message = f"""
Ask the user for the missing slot(s):
{json.dumps(missing_slots, indent=2)}

Context:
- Current datetime: {current_datetime()}
- Current location: {current_location}

Rules:
- Locations are expressed as names (e.g., "NUS-ISS", "Orchard MRT", "current location").
- Return all datetime values in ISO 8601 (e.g., "2025-08-01T09:00:00").
- Do not include a slot in the output if its value cannot be extracted.
    """.strip()

    system_prompt = f"""
You are an assistant helping with the intent "{intent}".

Current values:
{json.dumps(serialize_for_json(current_slots), indent=2)}

{status_message}

Only respond with the assistant's next message.
""".strip()

    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\n\nConversation:\n{dialogue}\n\nAssistant:"


def build_final_response_prompt(
    intent: str, slots: Dict[str, Any], backend_result: str
) -> str:
    return f"""
You are an assistant helping a user with the intent "{intent}".

The backend system has successfully returned the two nearest arrival times for the requested bus service. These times represent upcoming bus arrivals.

Backend result:
"{backend_result}"

Here are the slot values that were used for this intent:
{json.dumps(serialize_for_json(slots), indent=2)}

If the backend result is successful, it will return two nearest bus arrival timings.
Your task is to turn the backend result into a natural, helpful response to the user.
Do not ask any further questions or include labels.

If the backend result explicitly indicates that the feature isn't implemented yet, clearly let the user know that this feature 
for user intent: "{intent}" is still under development. Do not offer suggestions, predictions, or additional help.

Write the assistant's message below:
Assistant:"""


def build_help_prompt(history):
    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"""
The user asked for help or gave no recognizable intent. Provide a short and friendly list of what you can help with, based on this list of intent descrptions:

{json.dumps(INTENT_DESCRIPTIONS, indent=2)}

Conversation history:
{dialogue}

Guidelines:
- Use 2–3 items only if possible, or keep each item brief.
- Include just one short example for each item.
- Avoid technical labels like "intent".
- Use a warm and casual tone.
- Do NOT over-explain.
- End with a short line like: "What would you like to do?"

Reply in 80 words or less.
"""
