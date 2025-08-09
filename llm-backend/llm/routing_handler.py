from typing import Optional, Dict, Any, Tuple
import requests
from llm.utils import get_user_context
from llm.state import BACKEND_URL


def get_coordinates(
    location_name: str, jwt_token: Optional[str] = None
) -> Tuple[Optional[float], Optional[float], Optional[str], Optional[str]]:
    """
    Call Geocode API and return (lat, lon, displayName, error_message).
    """
    geocode_url = f"{BACKEND_URL}/api/geocode"
    params = {"locationName": location_name}
    headers = {}
    if jwt_token:
        headers["Authorization"] = jwt_token

    try:
        resp = requests.get(geocode_url, params=params, headers=headers, timeout=5)
        resp.raise_for_status()
    except requests.Timeout:
        return (
            None,
            None,
            None,
            f"Unable to reach the location service in time for '{location_name}'.",
        )
    except requests.RequestException as e:
        print(f"[DEBUG] RequestException: {e}")
        return (
            None,
            None,
            None,
            f"Unable to contact the location service for '{location_name}'.",
        )

    try:
        data = resp.json()
    except ValueError:
        return (
            None,
            None,
            None,
            f"Received an unexpected response when searching for '{location_name}'.",
        )

    results = data.get("results", [])
    if not results:
        return None, None, None, f"No matching location found for '{location_name}'."

    first = results[0]
    lat = first.get("latitude")
    lon = first.get("longitude")
    name = first.get("displayName")

    if lat is None or lon is None:
        return (
            None,
            None,
            None,
            f"Could not determine coordinates for '{location_name}'.",
        )

    return float(lat), float(lon), name, None


def handle_routing(
    slots: Dict[str, Any], jwt_token: Optional[str], user_name: str
) -> Dict[str, Any]:
    ctx = get_user_context(user_name)
    current_location = ctx.get("current_location")

    def resolve_location(loc_name: str):
        if loc_name and loc_name.lower() == "current location":
            if (
                current_location
                and "latitude" in current_location
                and "longitude" in current_location
            ):
                return (
                    float(current_location["latitude"]),
                    float(current_location["longitude"]),
                    "Current Location",
                    None,
                )
            else:
                return None, None, None, "Current location not available."
        else:
            return get_coordinates(loc_name, jwt_token)

    start_name = slots.get("start_location")
    end_name = slots.get("end_location")

    if not start_name or not end_name:
        return {
            "messages": ["Please provide both start and end location names."],
            "suggestedRoutes": [],
        }

    start_lat, start_lon, start_display, start_err = resolve_location(start_name)
    end_lat, end_lon, end_display, end_err = resolve_location(end_name)

    if start_err or end_err:
        return {
            "messages": [msg for msg in [start_err, end_err] if msg],
            "suggestedRoutes": [],
        }

    routing_url = f"{BACKEND_URL}/api/routing"
    payload = {
        "startCoordinates": f"{start_lat},{start_lon}",
        "endCoordinates": f"{end_lat},{end_lon}",
    }
    headers = {"Content-Type": "application/json"}
    if jwt_token:
        headers["Authorization"] = jwt_token

    try:
        response = requests.post(routing_url, json=payload, headers=headers, timeout=10)
        response.raise_for_status()
        data = response.json()

        suggested_routes = data.get("suggestedRoutes", [])
        if not suggested_routes:
            return {
                "messages": ["No routes found between the specified locations."],
                "suggestedRoutes": [],
            }

        return {
            "startLocation": start_display,
            "endLocation": end_display,
            "startCoordinates": {"latitude": start_lat, "longitude": start_lon},
            "endCoordinates": {"latitude": end_lat, "longitude": end_lon},
            "suggestedRoutes": suggested_routes,
        }

    except requests.RequestException as e:
        return {
            "messages": [f"Failed to fetch routing data: {str(e)}"],
            "suggestedRoutes": [],
        }
