import json
from llm.utils import current_datetime, serialize_for_json
from llm.state import INTENTS
from typing import Dict, Any


def build_extraction_prompt(intents, slots, history):
    system_prompt = f"""
You are a helpful assistant that extracts the user's intent and any relevant information from a multi-turn conversation.

Possible intents:
{json.dumps(intents, indent=2)}

Extract these slot fields if available:
{json.dumps(slots, indent=2)}

Rules for extracting datetime-related information:
- Current datetime: {current_datetime()}
- If only time is given, assume today.
- Use ISO 8601 format (e.g. "2025-08-01T09:00:00")

Return a JSON with 'intent' and 'slots'. Do not generate anything else.
"""
    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\nConversation:\n{dialogue}\n\nJSON:"


def build_followup_prompt(intent, required_slots, current_slots, history, missing_slots):
    if set(missing_slots) == {"boarding_bus_stop_name", "boarding_bus_stop_code"}:
        status_message = (
            "To proceed, please provide either the boarding bus stop name or the boarding bus stop code."
        )
    else:
        status_message = (
            f"""
            Some required information is still missing. Please ask a follow-up question to collect:
            {json.dumps(missing_slots, indent=2)}
            Your task is to prompt the user **only** for these missing values.
            
            Special rules:
            - The current date and time is {current_datetime()}
            - If the user specifies a time (e.g., "at 9 AM" or "in the evening") but not a date, use today's date with the given time.
            - Format all date and time values as ISO 8601 strings (e.g., "2025-08-01T09:00:00").
            - If the user gives a date and time separately, combine them into a full datetime string.
            """
        )

    system_prompt = f"""
You are an assistant helping a user with the intent "{intent}".

Here are the expected slot values for this intent:
{json.dumps(required_slots, indent=2)}

Here are the values provided so far:
{json.dumps(serialize_for_json(current_slots), indent=2)}

{status_message}

Do not include explanations or labels. Just write the next message from the assistant.
"""

    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\n\nConversation:\n{dialogue}\n\nAssistant:"


def build_final_response_prompt(intent: str, slots: Dict[str, Any], backend_result: str) -> str:
    return f"""
You are an assistant helping a user with the intent "{intent}".

The backend system has successfully processed the user's request and returned this result:
"{backend_result}"

Here are the slot values that were used for this intent:
{json.dumps(serialize_for_json(slots), indent=2)}

Your task is to turn the backend result into a natural, helpful response to the user.
Do not ask any further questions or include labels.

If the backend result indicates that the feature isn't implemented yet, clearly let the user know that this feature 
for user intent: "{intent}" is still under development. Do not offer suggestions, predictions, or additional help.

Write the assistant's message below:
Assistant:"""


def build_help_prompt(history):
    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )
    
    return f"""
The user asked for help or gave no recognizable intent. Provide a short and friendly list of what you can help with, based on this list of intents:

{json.dumps(INTENTS, indent=2)}

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