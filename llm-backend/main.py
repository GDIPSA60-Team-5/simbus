from llm.model import get_model
from llm.prompts import build_extraction_prompt, build_followup_prompt
from llm.state import INTENTS, REQUIRED_SLOTS, SLOT_TYPES, MAX_HISTORY_LENGTH, reset_conversation_for_user, user_conversations
from llm.utils import get_recent_history, merge_slots, extract_json_from_response, show_help, get_user_context
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional, Dict, Any


app = FastAPI()
model = get_model()

class ChatRequest(BaseModel):
    input: Optional[str] = None
    currentLocation: Optional[Dict[str, float]] = None
    currentTimestamp: Optional[int] = None

class BotResponseDTO(BaseModel):
    type: str
    message: str
    intent: Optional[str] = None
    slots: Optional[Dict[str, Any]] = None
    
    
@app.post("/chat", response_model=BotResponseDTO)
async def chat_endpoint(request: ChatRequest):
    # user_input = request.userInput.strip()
    # user_id = 1  # Can be replaced with session or actual user ID
    
    # ctx = get_user_context(user_id, user_conversations)
    # ctx["history"].append({"role": "user", "content": user_input})

    # # Extract intent and slots
    # recent_history = get_recent_history(ctx["history"], MAX_HISTORY_LENGTH)
    # known_slots = list(SLOT_TYPES.keys())
    # extraction_prompt = build_extraction_prompt(INTENTS, known_slots, recent_history)
    # response = model.generate(extraction_prompt, max_tokens=100)
    # extracted = extract_json_from_response(response)

    # if extracted:
    #     intent = extracted.get("intent")
    #     new_slots = extracted.get("slots", {})

    #     # If intent changes, clear history and update intent
    #     if intent and intent != ctx["state"].get("intent"):
    #         ctx["history"].clear()
    #         ctx["state"]["intent"] = intent

    #     merge_slots(ctx["state"]["slots"], new_slots)
    # else:
    #     return BotResponseDTO(message="🤖 Sorry, I couldn't identify your intent.")

    # # Handle special intents
    # if ctx["state"]["intent"] == "help":
    #     return BotResponseDTO(message=show_help())

    # if ctx["state"]["intent"] == "reset":
    #     reset_conversation_for_user(user_id)
    #     return BotResponseDTO(message="Starting fresh. What would you like to do?")

    # # Build and send follow-up prompt
    # followup_prompt = build_followup_prompt(
    #     ctx["state"]["intent"],
    #     REQUIRED_SLOTS.get(ctx["state"]["intent"], []),
    #     ctx["state"]["slots"],
    #     recent_history
    # )
    # reply = model.generate(followup_prompt, max_tokens=300)
    # ctx["history"].append({"role": "assistant", "content": reply})
    
    # return BotResponseDTO(
    #     message=reply,
    #     intent=ctx["state"].get("intent"),
    #     slots=ctx["state"]["slots"]
    # )
    return JSONResponse(content={
        "type": "message",
        "message": "Received your request."
    })
