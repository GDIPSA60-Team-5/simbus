import os
from typing import Optional, Dict, Any, Tuple
from dateutil import parser as date_parser
from dotenv import load_dotenv
import requests
from llm.utils import current_datetime
from llm.utils import get_user_context


load_dotenv()
backend_url = os.getenv("BACKEND_URL")


def get_coordinates(
    location_name: str, jwt_token: Optional[str] = None
) -> Tuple[Optional[str], Optional[str]]:
    """
    Call Geocode API and return (coordinates, error_message).
    Coordinates: 'latitude,longitude' if successful, else None.
    Error_message: None if successful, else user-friendly error.
    Technical details are logged to terminal for debugging.
    """
    geocode_url = f"{backend_url}/api/geocode"
    params = {"locationName": location_name}
    headers = {}
    if jwt_token:
        headers["Authorization"] = jwt_token

    try:
        resp = requests.get(geocode_url, params=params, headers=headers, timeout=5)
    except requests.Timeout:
        print(f"[DEBUG] Geocode request for '{location_name}' timed out.")
        return None, f"Unable to reach the location service in time for '{location_name}'. Please try again."
    except requests.RequestException as e:
        print(f"[DEBUG] Geocode request error for '{location_name}': {e}")
        return None, f"Unable to contact the location service for '{location_name}'."

    try:
        resp.raise_for_status()
    except requests.HTTPError as e:
        print(f"[DEBUG] Geocode request failed with HTTP {resp.status_code} for '{location_name}': {e}")
        return None, f"Location service returned an error for '{location_name}'. Please try again."

    try:
        data = resp.json()
    except ValueError:
        print(f"[DEBUG] Invalid JSON response while geocoding '{location_name}'. Raw: {resp.text[:200]}")
        return None, f"Received an unexpected response when searching for '{location_name}'."

    results = data.get("results", [])
    if not results:
        print(f"[DEBUG] No results found in geocode response for '{location_name}'. Response: {data}")
        return None, f"No matching location found for '{location_name}'."

    first = results[0]
    lat = first.get("latitude")
    lon = first.get("longitude")
    if lat is None or lon is None:
        print(f"[DEBUG] Missing latitude/longitude in geocode result for '{location_name}': {first}")
        return None, f"Could not determine coordinates for '{location_name}'."

    return f"{lat},{lon}", None


def handle_next_bus(slots: Dict[str, Any], jwt_token: str) -> Dict[str, Any]:
    base_url = f"{backend_url}/api/bus/arrivals"
    params = {}

    # Pick either bus stop code or name
    if slots.get("boarding_bus_stop_code"):
        params["busStopQuery"] = slots["boarding_bus_stop_code"]
        stop_name = slots.get("boarding_bus_stop_code")
    elif slots.get("boarding_bus_stop_name"):
        params["busStopQuery"] = slots["boarding_bus_stop_name"]
        stop_name = slots.get("boarding_bus_stop_name")
    else:
        return {
            "messages": [
                "Please provide a bus stop code or name to check the next bus."
            ],
            "arrivals": {},
        }

    if slots.get("bus_service_number"):
        params["serviceNo"] = slots["bus_service_number"]

    headers = {}
    if jwt_token:
        headers["Authorization"] = jwt_token
        print(f"JWT token: {jwt_token}")

    try:
        response = requests.get(base_url, params=params, headers=headers, timeout=5)
        response.raise_for_status()

        arrivals_data = response.json()  # List of bus service dicts

        if not arrivals_data:
            return {
                "messages": ["No upcoming buses found for the given stop and service."],
                "arrivals": {},
            }

        now = current_datetime()
        arrivals_by_bus = {}
        messages = []

        service_filter = params.get("serviceNo", "").lower()

        for service_info in arrivals_data:
            service_name = service_info.get("serviceName", "Unknown service")
            if service_filter and service_filter != service_name.lower():
                continue  # skip if not matching serviceNo filter

            arrival_times = service_info.get("arrivals", [])
            if not arrival_times:
                messages.append(f"No arrival times available for bus {service_name}.")
                continue

            minutes_list = []
            for eta_str in arrival_times[:2]:
                try:
                    eta = date_parser.parse(eta_str)
                    minutes = int((eta - now).total_seconds() / 60)
                    if minutes >= 0:
                        minutes_list.append(minutes)
                except Exception:
                    continue

            if minutes_list:
                arrivals_by_bus[service_name] = minutes_list
                first_eta = minutes_list[0]

                if first_eta == 0:
                    # First bus is arriving now
                    if len(minutes_list) == 1:
                        message = f"Bus {service_name} is arriving now at {stop_name}."
                    else:
                        message = (
                            f"Bus {service_name} is arriving now at {stop_name}. "
                            f"The next one will arrive in {minutes_list[1]} minute{'s' if minutes_list[1] > 1 else ''}."
                        )
                else:
                    # First bus is in the future
                    if len(minutes_list) == 1:
                        message = (
                            f"Bus {service_name} will arrive at {stop_name} "
                            f"in {first_eta} minute{'s' if first_eta > 1 else ''}."
                        )
                    else:
                        message = (
                            f"Bus {service_name} will arrive at {stop_name} "
                            f"in {first_eta} minute{'s' if first_eta > 1 else ''} "
                            f"and again in {minutes_list[1]} minute{'s' if minutes_list[1] > 1 else ''}."
                        )

                messages.append(message)

        if not messages:
            return {
                "messages": ["No matching bus service arrivals found."],
                "arrivals": {},
            }

        return {"messages": messages, "arrivals": arrivals_by_bus}

    except requests.RequestException as e:
        return {"messages": [f"Failed to fetch arrival data: {str(e)}"], "arrivals": {}}


def handle_routing(
    slots: Dict[str, Any], jwt_token: Optional[str], user_name: str
) -> Dict[str, Any]:
    ctx = get_user_context(user_name)
    current_location = ctx.get(
        "current_location"
    )  # e.g. {"latitude": "...", "longitude": "..."}

    def resolve_location(loc_name: str) -> Tuple[Optional[str], Optional[str]]:
        if loc_name and loc_name.lower() == "current location":
            if current_location and "latitude" in current_location and "longitude" in current_location:
                return f"{current_location['latitude']},{current_location['longitude']}", None
            else:
                return None, "Current location not available."
        else:
            return get_coordinates(loc_name, jwt_token)


    start_name = slots.get("start_location")
    end_name = slots.get("end_location")

    if not start_name or not end_name:
        return {
            "messages": ["Please provide both start and end location names."],
            "routes": [],
        }

    start_coords, start_err = resolve_location(start_name)
    end_coords, end_err = resolve_location(end_name)

    if start_err or end_err:
        return {
            "messages": [msg for msg in [start_err, end_err] if msg],
            "routes": [],
        }

    routing_url = f"{backend_url}/api/routing"
    payload = {
        "start": start_coords,
        "end": end_coords,
    }

    print(f"Routing Parameters: {payload}")
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
                "routes": [],
            }

        # Build user-friendly messages (optional)
        messages = []
        for idx, route in enumerate(suggested_routes, start=1):
            duration = route.get("durationInMinutes", "?")
            summary = route.get("summary", "No summary")
            legs = route.get("legs", [])

            leg_descriptions = []
            for leg in legs:
                leg_type = leg.get("type", "")
                dur = leg.get("durationInMinutes", 0)
                bus_no = leg.get("busServiceNumber")
                instr = leg.get("instruction", "")

                if leg_type == "BUS" and bus_no:
                    leg_descriptions.append(f"Take bus {bus_no} for {dur} minutes")
                elif leg_type == "WALK":
                    leg_descriptions.append(f"Walk for {dur} minutes")
                else:
                    leg_descriptions.append(instr or f"{leg_type} for {dur} minutes")

            leg_summary = "; then ".join(leg_descriptions)
            message = (
                f"Route {idx}: {summary}. Total duration {duration} minutes. "
                f"Details: {leg_summary}."
            )
            messages.append(message)

        return {
            "startLocation": start_name,
            "endLocation": end_name,
            "suggestedRoutes": suggested_routes,
            "messages": messages,  # Extra field for your frontend/UI convenience
        }

    except requests.RequestException as e:
        return {
            "messages": [f"Failed to fetch routing data: {str(e)}"],
            "routes": [],
        }
