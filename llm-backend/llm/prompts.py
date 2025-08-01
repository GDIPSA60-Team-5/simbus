import json
from llm.utils import current_datetime, serialize_for_json

def build_extraction_prompt(intents, slots, history):
    system_prompt = f"""
You are a helpful assistant that extracts the user's intent and any relevant information from a multi-turn conversation.

Possible intents:
{json.dumps(intents, indent=2)}

Extract these slot fields if available:
{json.dumps(slots, indent=2)}

Special rules:
- The current date and time is {current_datetime()}
- If the user specifies a time (e.g., "at 9 AM" or "in the evening") but not a date, use today's date with the given time.
- Format all date and time values as ISO 8601 strings (e.g., "2025-08-01T09:00:00").
- If the user gives a date and time separately, combine them into a full datetime string.

Return only a valid JSON object with fields intent and slots.
Do not wrap the output in quotes or code blocks. Do not omit closing braces.
"""
    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\n\nConversation:\n{dialogue}\n\nJSON:"


def build_followup_prompt(intent, required_slots, current_slots, history):
    missing_slots = [slot for slot in required_slots if not current_slots.get(slot)]

    if missing_slots:
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
    else:
        status_message = (
            "All required slot values have been provided. Please send a confirmation message to the user.\n"
            "Do not ask for any extra information or question."
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
