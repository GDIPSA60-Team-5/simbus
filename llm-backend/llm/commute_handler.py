from typing import Dict, Any, Union
from datetime import time
import requests
from llm.state import BACKEND_URL
from llm.dto import CommutePlanDTO, CommutePlanResponseDTO, ErrorResponseDTO
from llm.utils import get_user_saved_locations


def handle_schedule_commute(
    slots: Dict[str, Any], jwt_token: str
) -> Union[CommutePlanResponseDTO, ErrorResponseDTO]:
    """
    Handles the 'schedule_commute' intent:
    - Calls backend to create a new commute plan
    - Returns CommutePlanResponseDTO on success or ErrorResponseDTO on failure
    """
    url = f"{BACKEND_URL}/api/user/commute-plans"

    notif_time = slots.get("notification_start_time")
    if isinstance(notif_time, time):
        notif_time_str = notif_time.strftime("%H:%M")
    elif isinstance(notif_time, str):
        notif_time_str = notif_time
    else:
        notif_time_str = None

    # Get user saved locations mapping name -> id
    location_map = get_user_saved_locations(jwt_token)

    start_name = slots.get("start_location")
    end_name = slots.get("end_location")

    start_location_id = location_map.get(str(start_name)) if start_name else None
    end_location_id = location_map.get(str(end_name)) if end_name else None

    if not start_location_id or not end_location_id:
        return ErrorResponseDTO(
            message="Invalid start or end location. Please choose from your saved locations."
        )

    payload = {
        "commutePlanName": slots.get("commute_plan_name", "My Commute Plan"),
        "notifyAt": notif_time_str,
        "startLocationId": start_location_id,
        "endLocationId": end_location_id,
        "recurrence": bool(slots.get("recurrence_days")),
        "commuteRecurrenceDayIds": slots.get("recurrence_days", []),
    }

    headers = {"Content-Type": "application/json"}
    if jwt_token:
        headers["Authorization"] = jwt_token

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=5)
        response.raise_for_status()
        data = response.json()

        commute_plan = CommutePlanDTO(
            id=data.get("id"),
            commutePlanName=data.get("commutePlanName"),
            notifyAt=time.fromisoformat(data.get("notifyAt"))
            if data.get("notifyAt")
            else None,
            startLocationId=data.get("startLocationId"),
            endLocationId=data.get("endLocationId"),
            recurrence=data.get("recurrence"),
            commuteRecurrenceDayIds=data.get("commuteRecurrenceDayIds", []),
            savedTripRouteId=data.get("savedTripRouteId")
        )

        return CommutePlanResponseDTO(creationSuccess=True, commutePlan=commute_plan)

    except requests.RequestException as e:
        return ErrorResponseDTO(message=f"Failed to create commute plan: {e}")
