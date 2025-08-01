package com.example.feature_chatbot.data

import com.google.gson.*
import java.lang.reflect.Type

class BotResponseTypeAdapter : JsonDeserializer<BotResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BotResponse {
        // Get the "type" field from the JSON object
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString

        // Delegate to the appropriate deserializer based on the type
        return when (type) {
            "directions" -> context.deserialize(jsonObject, BotResponse.Directions::class.java)
            "message" -> context.deserialize(jsonObject, BotResponse.Message::class.java)
            "error" -> context.deserialize(jsonObject, BotResponse.Error::class.java)
            else -> throw JsonParseException("Unknown bot response type: $type")
        }
    }
}