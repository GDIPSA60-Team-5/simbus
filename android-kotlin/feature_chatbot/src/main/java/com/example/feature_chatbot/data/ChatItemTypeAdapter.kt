package com.example.feature_chatbot.data

import com.google.gson.*
import java.lang.reflect.Type

class ChatItemTypeAdapter : JsonSerializer<ChatItem>, JsonDeserializer<ChatItem> {
    
    override fun serialize(
        src: ChatItem,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        
        when (src) {
            is ChatItem.Greeting -> {
                jsonObject.addProperty("type", "greeting")
            }
            is ChatItem.UserMessage -> {
                jsonObject.addProperty("type", "user_message")
                jsonObject.addProperty("id", src.id)
                jsonObject.addProperty("text", src.text)
            }
            is ChatItem.BotMessage -> {
                jsonObject.addProperty("type", "bot_message")
                jsonObject.addProperty("id", src.id)
                jsonObject.add("botResponse", context.serialize(src.botResponse))
            }
            is ChatItem.TypingIndicator -> {
                jsonObject.addProperty("type", "typing_indicator")
                jsonObject.addProperty("message", src.message)
            }
        }
        
        return jsonObject
    }
    
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ChatItem {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString
        
        return when (type) {
            "greeting" -> ChatItem.Greeting
            "user_message" -> {
                val id = jsonObject.get("id").asString
                val text = jsonObject.get("text").asString
                ChatItem.UserMessage(id, text)
            }
            "bot_message" -> {
                val id = jsonObject.get("id").asString
                val botResponse = context.deserialize<BotResponse>(
                    jsonObject.get("botResponse"), 
                    BotResponse::class.java
                )
                ChatItem.BotMessage(id, botResponse)
            }
            "typing_indicator" -> {
                val message = jsonObject.get("message")?.asString ?: "Typing..."
                ChatItem.TypingIndicator(message)
            }
            null -> {
                // Handle old format without type field - try to infer from structure
                when {
                    jsonObject.has("text") && jsonObject.has("id") -> {
                        val id = jsonObject.get("id").asString
                        val text = jsonObject.get("text").asString
                        ChatItem.UserMessage(id, text)
                    }
                    jsonObject.has("botResponse") && jsonObject.has("id") -> {
                        val id = jsonObject.get("id").asString
                        val botResponse = context.deserialize<BotResponse>(
                            jsonObject.get("botResponse"), 
                            BotResponse::class.java
                        )
                        ChatItem.BotMessage(id, botResponse)
                    }
                    else -> throw JsonParseException("Unable to determine ChatItem type from JSON structure")
                }
            }
            else -> throw JsonParseException("Unknown ChatItem type: $type")
        }
    }
}