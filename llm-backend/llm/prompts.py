import json

def build_extraction_prompt(intents, slots, history):
    system_prompt = f"""
You are a helpful assistant that extracts the user's intent and any relevant information from a multi-turn conversation.

Possible intents:
{json.dumps(intents, indent=2)}

Extract these slot fields if available:
{json.dumps(slots, indent=2)}

Return a JSON with 'intent' and 'slots'.
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
            "Some required information is still missing. Please ask a follow-up question to collect:\n"
            f"{json.dumps(missing_slots, indent=2)}"
            "Your task is to prompt the user **only** for these missing values."
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
{json.dumps(current_slots, indent=2)}

{status_message}

Do not include explanations or labels. Just write the next message from the assistant.
"""

    dialogue = "\n".join(
        f"{'User' if turn['role'] == 'user' else 'Assistant'}: {turn['content']}"
        for turn in history
    )

    return f"{system_prompt}\n\nConversation:\n{dialogue}\n\nAssistant:"
