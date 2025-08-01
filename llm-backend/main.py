from llm.model import get_model
from llm.prompts import build_extraction_prompt, build_followup_prompt
from llm.state import INTENTS, REQUIRED_SLOTS, MAX_HISTORY_LENGTH, reset_conversation_for_user, user_conversations
from llm.utils import get_recent_history, typewriter_print, merge_slots, extract_json_from_response, show_help, get_user_context

model = get_model()

with model.chat_session():
    while True:
        user_input = input("You: ").strip()
        if user_input.lower() in ["exit", "quit"]:
            break

        user_id = 1  # Can be replaced with session or actual user ID
        ctx = get_user_context(user_id, user_conversations, REQUIRED_SLOTS)
        ctx["history"].append({"role": "user", "content": user_input})

        print("🤖 Thinking...", end="", flush=True)

        # Extract intent and slots
        recent_history = get_recent_history(ctx["history"], MAX_HISTORY_LENGTH)
        known_slots = list(ctx["state"]["slots"].keys())
        extraction_prompt = build_extraction_prompt(INTENTS, known_slots, recent_history)
        response = model.generate(extraction_prompt, max_tokens=100)
        extracted = extract_json_from_response(response)

        if extracted:
            intent = extracted.get("intent")
            new_slots = extracted.get("slots", {})

            # If intent changes, clear history and update intent
            if intent and intent != ctx["state"].get("intent"):
                ctx["history"].clear()
                ctx["state"]["intent"] = intent

            merge_slots(ctx["state"]["slots"], new_slots)
        else:
            print("\r", end="")
            typewriter_print("🤖 Sorry, I couldn't identify your intent.")
            continue

        print("\r", end="")

        # Handle special intents
        if ctx["state"]["intent"] == "help":
            show_help()
            continue

        if ctx["state"]["intent"] == "reset":
            reset_conversation_for_user(user_id)
            typewriter_print("🔄 Starting fresh. What would you like to do?")
            continue

        # Build and send follow-up prompt
        followup_prompt = build_followup_prompt(
            ctx["state"]["intent"],
            REQUIRED_SLOTS.get(ctx["state"]["intent"], []),
            ctx["state"]["slots"],
            recent_history
        )
        reply = model.generate(followup_prompt, max_tokens=300)
        ctx["history"].append({"role": "assistant", "content": reply})
        typewriter_print(reply)
        print(f"\n{user_conversations}\n") # to visualize the current state of extracted information
