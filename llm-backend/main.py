from llm.model import get_model
from llm.prompts import (
    build_extraction_prompt,
    build_followup_prompt,
    build_final_response_prompt,
    build_help_prompt,
)
from llm.state import (
    MAX_HISTORY_LENGTH,
    CONFIDENCE_THRESHOLD,
    reset_conversation_for_user,
    user_conversations,
)
from llm.utils import (
    get_recent_history,
    merge_slots,
    extract_json_from_response,
    get_user_context,
    find_missing_slots,
)
from llm.intent_handler import handle_next_bus
from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional, Dict, Any
import joblib
import numpy as np


app = FastAPI()
model = get_model()

CLASSIFIER = "tf-idf"

# Load embedder and classifier as a tuple
embedder, intent_classifier = joblib.load("intent-classifier/intent_classifier_embedding.joblib")

# Load the TF-IDF model
# vectorizer, intent_classifier = joblib.load("intent-classifier/intent_classifier_tfidf.joblib")

class ChatRequest(BaseModel):
    userInput: str
    currentLocation: Optional[Dict[str, float]] = None
    currentTimestamp: Optional[int] = None


class BotResponseDTO(BaseModel):
    type: str
    message: str
    intent: Optional[str] = None
    slots: Optional[Dict[str, Any]] = None


def predict_intent(user_input: str) -> Optional[str]:
    input_vector = None
    input_vector = embedder.encode([user_input])
    # input_vector = vectorizer.transform([user_input])
    probs = intent_classifier.predict_proba(input_vector)[0]
    print(f"Prediction probs: {probs}")
    max_index = np.argmax(probs)
    max_prob = probs[max_index]
    return (
        intent_classifier.classes_[max_index]
        if max_prob >= CONFIDENCE_THRESHOLD
        else None
    )
    

@app.post("/chat", response_model=BotResponseDTO)
def chat_endpoint(request: ChatRequest):
    user_input = request.userInput.strip()
    current_location = request.currentLocation
    user_id = 1  # Replace with session or actual user ID

    ctx = get_user_context(user_id, user_conversations)
    ctx["current_location"] = current_location
    print(f"Intent before prediction: {ctx['state']['intent']}")

    predicted_intent = predict_intent(user_input)
    print(f"Intent after prediction: {predicted_intent}")

    # --- LLM SESSION START ---
    with model.chat_session():
        if predicted_intent and predicted_intent != ctx["state"]["intent"]:
            ctx["history"].clear()
            ctx["state"]["intent"] = predicted_intent
            
        active_intent = ctx["state"]["intent"]
        ctx["history"].append({"role": "user", "content": user_input})
            
        # --- Reset conversation ---
        if active_intent == "reset":
            reset_conversation_for_user(user_id)
            return BotResponseDTO(
                type="message", message="Starting fresh. What would you like to do?"
            )

        # --- Help and Fallback ---
        elif active_intent in ["help", None]:
            help_prompt = build_help_prompt(ctx["history"])
            print(f"Help prompt executed: {help_prompt}")
            help_response = model.generate(help_prompt, max_tokens=300)
            return BotResponseDTO(type="message", message=help_response)

        # Only update intent if it changed and is confident
        else:
            recent_history = get_recent_history(ctx["history"], MAX_HISTORY_LENGTH)
            current_slots = ctx["state"]["slots"]
            missing_slots = find_missing_slots(active_intent, current_slots)

            extraction_prompt = build_extraction_prompt(
                active_intent, missing_slots, recent_history, ctx["current_location"]
            )
            print(f"Slot extraction prompt: {extraction_prompt}")
            response = model.generate(extraction_prompt, max_tokens=100)
            extracted = extract_json_from_response(response)

            if not extracted:
                return BotResponseDTO(
                    type="message", message="Sorry, I couldn't identify your intent."
                )

            # --- Merge extracted intent/slots ---
            print(f"Slots before update: {ctx}")
            new_slots = extracted.get("slots", {})
            merge_slots(ctx["state"]["slots"], new_slots)
            print(f"Slots after update: {ctx}")

            # --- Handle main intents ---
            current_slots = ctx["state"]["slots"]
            missing_slots = find_missing_slots(active_intent, current_slots)

            if not missing_slots:
                if active_intent == "next_bus":
                    backend_result = handle_next_bus(current_slots)
                else:
                    backend_result = f"Intent '{active_intent}' is recognized, but no handler implemented."

                final_prompt = build_final_response_prompt(
                    active_intent, current_slots, backend_result
                )
                print(f"Final Prompt: {final_prompt}")
                reply = model.generate(final_prompt, max_tokens=200)
                ctx["history"].append({"role": "assistant", "content": reply})

                return BotResponseDTO(
                    type="message",
                    message=reply,
                    intent=active_intent,
                    slots=current_slots,
                )

            # --- Follow-up prompt if there are missing slots ---
            followup_prompt = build_followup_prompt(
                active_intent, current_slots, recent_history, missing_slots, ctx["current_location"]
            )
            reply = model.generate(followup_prompt, max_tokens=300)
            ctx["history"].append({"role": "assistant", "content": reply})

            return BotResponseDTO(
                type="message", message=reply, intent=active_intent, slots=current_slots
            )
