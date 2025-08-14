from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from datetime import datetime, time


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


class CommutePlanDTO(BaseModel):
    id: str
    commutePlanName: str
    notifyAt: Optional[time] = None          # maps to LocalTime in Java
    arrivalTime: Optional[time] = None       # maps to LocalTime in Java
    reminderOffsetMin: Optional[int] = None
    recurrence: Optional[bool] = None

    startLocationId: Optional[str] = None
    endLocationId: Optional[str] = None
    userId: Optional[str] = None
    savedTripRouteId: Optional[str] = None   # new field in your updated Java model

    commuteRecurrenceDayIds: Optional[List[str]] = []


class CommutePlanResponseDTO(BaseModel):
    type: str = "commute-plan"
    creationSuccess: bool
    commutePlan: Optional[CommutePlanDTO] = None


class BusArrivalDTO(BaseModel):
    serviceName: str
    operator: str
    arrivals: List[datetime]  # Pydantic will parse ISO-8601 strings into datetime


class NextBusResponseDTO(BaseModel):
    type: str = "next-bus"
    stopCode: str
    stopName: str
    services: List[BusArrivalDTO]
