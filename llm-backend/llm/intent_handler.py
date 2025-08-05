import requests
from typing import Dict, Any
from llm.utils import current_datetime
from dateutil import parser as date_parser  # pip install python-dateutil


def handle_next_bus(slots: Dict[str, Any]) -> str:
    base_url = "http://localhost:8080/api/bus/arrivals"
    params = {}

    # Pick either bus stop code or name
    if slots.get("boarding_bus_stop_code"):
        params["busStopCode"] = slots["boarding_bus_stop_code"]
    elif slots.get("boarding_bus_stop_name"):
        params["busStopCode"] = slots["boarding_bus_stop_name"]

    if slots.get("bus_service_number"):
        params["serviceNo"] = slots["bus_service_number"]

    if not params.get("busStopCode") and not params.get("busStopName"):
        return "Please provide a bus stop code or name to check the next bus."

    try:
        response = requests.get(base_url, params=params, timeout=5)
        response.raise_for_status()

        arrivals_data = response.json()  # List of bus service dicts

        if not arrivals_data:
            return "No upcoming buses found for the given stop and service."

        now = current_datetime()
        messages = []

        # If bus_number specified, filter results to that service only
        service_filter = params.get("serviceNo", "").lower()

        for service_info in arrivals_data:
            service_name = service_info.get("serviceName", "Unknown service")
            if service_filter and service_filter != service_name.lower():
                continue  # skip if not matching serviceNo filter

            arrival_times = service_info.get("arrivals", [])
            if not arrival_times:
                messages.append(f"No arrival times available for bus {service_name}.")
                continue

            # Calculate minutes to each arrival, but show only next 2 arrivals
            for eta_str in arrival_times[:2]:
                try:
                    eta = date_parser.parse(eta_str)
                    minutes = int((eta - now).total_seconds() / 60)
                    if minutes < 0:
                        msg = f"Bus {service_name} has just departed."
                    elif minutes == 0:
                        msg = f"Bus {service_name} is arriving now."
                    else:
                        msg = f"Bus {service_name} will arrive in {minutes} minute{'s' if minutes > 1 else ''}."
                    messages.append(msg)
                except Exception:
                    messages.append(f"Could not parse arrival time for Bus {service_name}.")

        if not messages:
            return "No matching bus service arrivals found."

        return "\n".join(messages)

    except requests.RequestException as e:
        return f"Failed to fetch arrival data: {str(e)}"

