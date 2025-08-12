package com.example.feature_chatbot.data

import com.example.core.model.Coordinates
import com.example.core.model.Route
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class BotResponseTypeAdapter : JsonSerializer<BotResponse>, JsonDeserializer<BotResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BotResponse {
        // Get the "type" field from the JSON object
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString

        return when (type) {
            "directions" -> {
                val startLocation = jsonObject.get("startLocation").asString
                val endLocation = jsonObject.get("endLocation").asString
                val startCoordinates = context.deserialize<Coordinates>(
                    jsonObject.get("startCoordinates"), 
                    Coordinates::class.java
                )
                val endCoordinates = context.deserialize<Coordinates>(
                    jsonObject.get("endCoordinates"), 
                    Coordinates::class.java
                )
                val suggestedRoutes = if (jsonObject.has("suggestedRoutes") && !jsonObject.get("suggestedRoutes").isJsonNull) {
                    context.deserialize<List<Route>>(
                        jsonObject.get("suggestedRoutes"),
                        object : TypeToken<List<Route>>() {}.type
                    )
                } else {
                    null
                }
                BotResponse.Directions(startLocation, endLocation, startCoordinates, endCoordinates, suggestedRoutes)
            }
            "message" -> {
                val message = jsonObject.get("message").asString
                BotResponse.Message(message)
            }
            "error" -> {
                val message = jsonObject.get("message").asString
                BotResponse.Error(message)
            }
            else -> throw JsonParseException("Unknown bot response type: $type")
        }
    }
    
    override fun serialize(
        src: BotResponse,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        
        when (src) {
            is BotResponse.Message -> {
                jsonObject.addProperty("type", "message")
                jsonObject.addProperty("message", src.message)
            }
            is BotResponse.Directions -> {
                jsonObject.addProperty("type", "directions")
                jsonObject.addProperty("startLocation", src.startLocation)
                jsonObject.addProperty("endLocation", src.endLocation)
                jsonObject.add("startCoordinates", context.serialize(src.startCoordinates))
                jsonObject.add("endCoordinates", context.serialize(src.endCoordinates))
                jsonObject.add("suggestedRoutes", context.serialize(src.suggestedRoutes))
            }
            is BotResponse.Error -> {
                jsonObject.addProperty("type", "error")
                jsonObject.addProperty("message", src.message)
            }
        }
        
        return jsonObject
    }
}