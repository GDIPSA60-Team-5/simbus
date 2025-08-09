from pydantic import BaseModel
from typing import List, Optional, Dict, Any


class ChatRequest(BaseModel):
    userInput: str
    currentLocation: Optional[Dict[str, float]] = None
    currentTimestamp: Optional[int] = None

class CoordinatesDTO(BaseModel):
    latitude: float
    longitude: float

class DirectionsResponseDTO(BaseModel):
    type: str = "directions"
    startLocation: str
    endLocation: str
    startCoordinates: CoordinatesDTO
    endCoordinates: CoordinatesDTO
    suggestedRoutes: List[Any]

class MessageResponseDTO(BaseModel):
    type: str = "message"
    message: str
    intent: Optional[str] = None
    slots: Optional[Dict[str, Any]] = None

class ErrorResponseDTO(BaseModel):
    type: str = "error"
    message: str
