from typing import Union
from fastapi import FastAPI, Header, HTTPException
from llm.model import get_model, predict_intent
from llm.prompts import (
    build_extraction_prompt,
    build_followup_prompt,
)
from llm.state import MAX_HISTORY_LENGTH, REQUIRED_SLOTS
from llm.utils import (
    get_recent_history,
    merge_slots,
    extract_json_from_response,
    get_user_context,
    reset_conversation_for_user,
    find_missing_slots,
    show_help,
    flatten_slots,
)
from llm.next_bus_handler import handle_next_bus
from llm.routing_handler import handle_routing
from llm.dto import DirectionsResponseDTO, MessageResponseDTO, ErrorResponseDTO, ChatRequest


app = FastAPI()
model = get_model()


@app.post("/chat", response_model=Union[MessageResponseDTO, DirectionsResponseDTO])
def chat_endpoint(request: ChatRequest, authorization: str = Header(None)):
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=401, detail="Missing or invalid Authorization header"
        )

    jwt_token = authorization
    print(f"Incoming JWT_Token: {jwt_token}")
    user_input = request.userInput.strip()
    current_location = request.currentLocation
    user_name = "Aung"  # Replace with session or actual user name

    ctx = get_user_context(user_name)
    ctx["current_location"] = current_location
    print(f"\nIntent before prediction: {ctx['state']['intent']}\n")

    predicted_intent = predict_intent(user_input)
    print(f"\nIntent after prediction: {predicted_intent}\n")

    # --- LLM SESSION START ---
    with model.chat_session():
        ctx["history"] = get_recent_history(ctx["history"], MAX_HISTORY_LENGTH)

        # Step 1: If intent changed → reset history
        if predicted_intent and predicted_intent != ctx["state"]["intent"]:
            ctx["state"]["intent"] = predicted_intent
            ctx["state"]["slots"] = {}
            ctx["history"].clear()

        active_intent = ctx["state"]["intent"]

        # --- Reset conversation ---
        if active_intent == "reset":
            reset_conversation_for_user(user_name)
            return MessageResponseDTO(
                message="Starting fresh. What would you like to do?"
            )

        # --- Help and Fallback ---
        elif active_intent in ["help", None]:
            return MessageResponseDTO(message=show_help(user_name))

        # EARLY MISSING-SLOT CHECK
        previous_slots = ctx["state"]["slots"]
        required_slots = flatten_slots(REQUIRED_SLOTS.get(active_intent, []))
        missing_before_extraction = find_missing_slots(active_intent, previous_slots)

        if not missing_before_extraction:
            # All slots already present — check if this is a fresh set
            ctx["history"].clear()
            # ctx["state"]["slots"] = {}
            
        ctx["history"].append({"role": "user", "content": user_input})

        # Step 2: Slot extraction
        recent_history = get_recent_history(ctx["history"], MAX_HISTORY_LENGTH)
        extraction_prompt = build_extraction_prompt(
            active_intent, required_slots, recent_history
        )
        print(f"\nSlot extraction prompt: {extraction_prompt}\n")
        response = model.generate(extraction_prompt, max_tokens=100)
        extracted = extract_json_from_response(response)

        if not extracted:
            return MessageResponseDTO(message="Sorry, I couldn't identify your intent.")

        # Step 3: Merge extracted slots
        print(f"Slots before update: {ctx}")
        new_slots = extracted.get("slots", {})
        merge_slots(ctx["state"]["slots"], new_slots)
        print(f"Slots after update: {ctx}")

        # Step 4: Check for missing slots again
        current_slots = ctx["state"]["slots"]
        missing_after_extraction = find_missing_slots(active_intent, current_slots)

        if not missing_after_extraction:
            # --- Handle main intents ---
            if active_intent == "next_bus":
                backend_result = handle_next_bus(current_slots, jwt_token)
                print(f"Next bus result: {backend_result}")

                reply = (
                    "\n".join(backend_result.get("messages", []))
                    if isinstance(backend_result, dict)
                    else str(backend_result)
                )

                ctx["history"].append({"role": "assistant", "content": reply})
                return MessageResponseDTO(
                    message=reply,
                    intent=active_intent,
                    slots=current_slots,
                )

            elif active_intent == "route_info":
                backend_result = handle_routing(current_slots, jwt_token, user_name)
                reply = "\n".join(backend_result.get("messages", []))

                if not backend_result.get("suggestedRoutes"):
                    ctx["history"].append({"role": "assistant", "content": reply})
                    return MessageResponseDTO(
                        message=reply, intent=active_intent, slots=current_slots
                    )

                ctx["history"].append({"role": "assistant", "content": reply})
                return DirectionsResponseDTO(
                    startLocation=backend_result["startLocation"],
                    endLocation=backend_result["endLocation"],
                    startCoordinates=backend_result["startCoordinates"],
                    endCoordinates=backend_result["endCoordinates"],
                    suggestedRoutes=backend_result["suggestedRoutes"]
                )

            else:
                backend_result = f"Intent '{active_intent}' is recognized, but no handler implemented."
                ctx["history"].append({"role": "assistant", "content": backend_result})
                return ErrorResponseDTO(
                    message=backend_result,
                )

        # Step 5: If still missing slots → follow-up prompt
        followup_prompt = build_followup_prompt(
            active_intent, current_slots, recent_history, missing_after_extraction
        )
        print(f"Followup-prompt: {followup_prompt}")
        reply = model.generate(followup_prompt, max_tokens=300)
        ctx["history"].append({"role": "assistant", "content": reply})
        print(f"Context history: {ctx['history']}")

        return MessageResponseDTO(
            message=reply, intent=active_intent, slots=current_slots
        )
