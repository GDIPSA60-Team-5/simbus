import requests
import os
from typing import Dict, Any
from llm.utils import current_datetime
from dateutil import parser as date_parser
from dotenv import load_dotenv


load_dotenv()
backend_url = os.getenv("BACKEND_URL")

def handle_next_bus(slots: Dict[str, Any]) -> Dict[str, Any]:
    base_url = f"{backend_url}/api/bus/arrivals"
    params = {}

    # Pick either bus stop code or name
    if slots.get("boarding_bus_stop_code"):
        params["busStopCode"] = slots["boarding_bus_stop_code"]
        stop_name = slots.get("boarding_bus_stop_code")
    elif slots.get("boarding_bus_stop_name"):
        params["busStopCode"] = slots["boarding_bus_stop_name"]
        stop_name = slots.get("boarding_bus_stop_name")
    else:
        return {
            "messages": ["Please provide a bus stop code or name to check the next bus."],
            "arrivals": {}
        }

    if slots.get("bus_service_number"):
        params["serviceNo"] = slots["bus_service_number"]

    try:
        response = requests.get(base_url, params=params, timeout=5)
        response.raise_for_status()

        arrivals_data = response.json()  # List of bus service dicts

        if not arrivals_data:
            return {
                "messages": ["No upcoming buses found for the given stop and service."],
                "arrivals": {}
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
                # Build message
                times_str = " and again in ".join([
                    "arriving now" if m == 0 else f"{m} minute{'s' if m > 1 else ''}"
                    for m in minutes_list
                ])
                messages.append(f"Bus {service_name} will arrive at {stop_name} in {times_str}.")

        if not messages:
            return {
                "messages": ["No matching bus service arrivals found."],
                "arrivals": {}
            }

        return {
            "messages": messages,
            "arrivals": arrivals_by_bus
        }

    except requests.RequestException as e:
        return {
            "messages": [f"Failed to fetch arrival data: {str(e)}"],
            "arrivals": {}
        }
