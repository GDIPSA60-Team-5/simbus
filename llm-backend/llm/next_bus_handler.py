from typing import Dict, Any
from dateutil import parser as date_parser
import requests
from llm.utils import current_datetime
from llm.state import BACKEND_URL


def handle_next_bus(slots: Dict[str, Any], jwt_token: str) -> Dict[str, Any]:
    base_url = f"{BACKEND_URL}/api/bus/arrivals"
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
                except Exception as e:
                    print(f"Failed to parse ETA '{eta_str}': {e}")
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
