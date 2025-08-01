package com.example.springbackend.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// This annotation tells Jackson (the JSON library) how to handle polymorphism.
// It will add a "type" field to the JSON to distinguish between subclasses.
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DirectionsResponseDTO.class, name = "directions"),
        @JsonSubTypes.Type(value = MessageResponseDTO.class, name = "message"),
        @JsonSubTypes.Type(value = ErrorResponseDTO.class, name = "error")
})
public abstract class BotResponseDTO {
    public abstract String getType();
}