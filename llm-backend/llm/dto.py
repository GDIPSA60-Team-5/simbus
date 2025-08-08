from pydantic import BaseModel
from typing import List, Optional, Dict, Any


class ChatRequest(BaseModel):
    userInput: str
    currentLocation: Optional[Dict[str, float]] = None
    currentTimestamp: Optional[int] = None


class LegDTO(BaseModel):
    type: str
    durationInMinutes: int
    busServiceNumber: Optional[str]
    instruction: Optional[str]


class RouteDTO(BaseModel):
    durationInMinutes: int
    legs: List[LegDTO]
    summary: str
    routeGeometry: Optional[str]


class DirectionsResponseDTO(BaseModel):
    type: str = "directions"
    startLocation: str
    endLocation: str
    suggestedRoutes: List[RouteDTO]


class MessageResponseDTO(BaseModel):
    type: str = "message"
    message: str
    intent: Optional[str] = None
    slots: Optional[Dict[str, Any]] = None


class ErrorResponseDTO(BaseModel):
    type: str = "error"
    message: str
