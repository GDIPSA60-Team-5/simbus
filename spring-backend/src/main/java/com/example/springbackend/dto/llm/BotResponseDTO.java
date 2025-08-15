package com.example.springbackend.dto.llm;

import com.example.springbackend.model.BusArrival;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A sealed interface for all bot responses, configured for polymorphic
 * JSON serialization and deserialization with Jackson.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DirectionsResponseDTO.class, name = "directions"),
        @JsonSubTypes.Type(value = MessageResponseDTO.class, name = "message"),
        @JsonSubTypes.Type(value = ErrorResponseDTO.class, name = "error"),
        @JsonSubTypes.Type(value = CommutePlanResponseDTO.class, name = "commute-plan"),
        @JsonSubTypes.Type(value = NextBusResponseDTO.class, name = "next-bus")
})
public sealed interface BotResponseDTO permits
        DirectionsResponseDTO,
        ErrorResponseDTO,
        MessageResponseDTO,
        CommutePlanResponseDTO,
        NextBusResponseDTO {
    String getType();
}