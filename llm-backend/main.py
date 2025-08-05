from llm.model import get_model
from llm.prompts import build_extraction_prompt, build_followup_prompt, build_final_response_prompt
from llm.state import INTENTS, REQUIRED_SLOTS, SLOT_TYPES, MAX_HISTORY_LENGTH, reset_conversation_for_user, user_conversations
from llm.utils import get_recent_history, merge_slots, extract_json_from_response, show_help, get_user_context, find_missing_slots
from llm.intent_handler import handle_next_bus
from fastapi import FastAPI
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional, Dict, Any


app = FastAPI()
model = get_model()

class ChatRequest(BaseModel):
    userInput: Optional[str] = None
    currentLocation: Optional[Dict[str, float]] = None
    currentTimestamp: Optional[int] = None

class BotResponseDTO(BaseModel):
    type: str
    message: str
    intent: Optional[str] = None
    slots: Optional[Dict[str, Any]] = None
    
    
@app.post("/chat", response_model=BotResponseDTO)
def chat_endpoint(request: ChatRequest):
    print("DEBUG - Raw Body:", request)
    user_input = request.userInput.strip()
    user_id = 1  # Can be replaced with session or actual user ID
    print(f"User_input: {user_input}")
    
    ctx = get_user_context(user_id, user_conversations)
    print(f"Initial Context: {ctx}")
    ctx["history"].append({"role": "user", "content": user_input})

    with model.chat_session():
        # Extract intent and slots
        recent_history = get_recent_history(ctx["history"], MAX_HISTORY_LENGTH)
        known_slots = list(SLOT_TYPES.keys())
        extraction_prompt = build_extraction_prompt(INTENTS, known_slots, recent_history)
        print(f"Prompt: {extraction_prompt}")
        response = model.generate(extraction_prompt, max_tokens=100)
        print(f"Response: {response}")
        extracted = extract_json_from_response(response)

        if extracted:
            intent = extracted.get("intent")
            new_slots = extracted.get("slots", {})
            print(f"Context: {ctx}")

            # If intent changes, clear history and update intent
            if intent and intent != ctx["state"].get("intent"):
                ctx["history"].clear()
                ctx["state"]["intent"] = intent

            merge_slots(ctx["state"]["slots"], new_slots)
        else:
            print(f"Context: {ctx}")
            return BotResponseDTO(
                type="message",
                message="Sorry, I couldn't identify your intent."
            )

        # Handle special intents
        print(f"Context: {ctx}")
        if ctx["state"]["intent"] == "help":
            return BotResponseDTO(
                type="message",
                message=show_help()
            )

        if ctx["state"]["intent"] == "reset":
            reset_conversation_for_user(user_id)
            return BotResponseDTO(
                type="message",
                message="Starting fresh. What would you like to do?"
            )

        required_slots = REQUIRED_SLOTS.get(intent, [])
        current_slots = ctx["state"]["slots"]
        recent_history = get_recent_history(ctx["history"], MAX_HISTORY_LENGTH)
        
        # Find missing slot values
        missing_slots = find_missing_slots(intent, current_slots)
        
        if not missing_slots:
            # Call backend and build final prompt
            if intent == "next_bus":
                print(f"Current slots before passing to handler: {current_slots}")
                backend_result = handle_next_bus(current_slots)
            else:
                backend_result = f"Intent '{intent}' is recognized, but no handler implemented."

            # Build and send final prompt
            final_prompt = build_final_response_prompt(intent, current_slots, backend_result)
            print(f"Final Prompt: {final_prompt}")
            reply = model.generate(final_prompt, max_tokens=200)

            ctx["history"].append({"role": "assistant", "content": reply})

            return BotResponseDTO(
                type="message",
                message=reply,
                intent=intent,
                slots=current_slots
            )

        else:
            # Build and send follow-up prompt
            followup_prompt = build_followup_prompt(
                intent,
                required_slots,
                current_slots,
                recent_history,
                missing_slots
            )
            print(f"Follow-up Prompt: {followup_prompt}")
            reply = model.generate(followup_prompt, max_tokens=300)
            print(f"Reply: {reply}")
            ctx["history"].append({"role": "assistant", "content": reply})
            print(f"Context after appending reply: {ctx}")
            
            return BotResponseDTO(
                type="message",
                message=reply,
                intent=intent,
                slots=current_slots
            )
