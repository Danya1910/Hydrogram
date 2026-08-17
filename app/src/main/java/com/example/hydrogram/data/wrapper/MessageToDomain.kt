package com.example.hydrogram.data.wrapper

import com.example.hydrogram.data.dto.MessageDto
import com.example.hydrogram.domain.model.Message
import kotlinx.serialization.modules.serializersModuleOf

fun MessageDto.toDomain(): Message {
    return when (type) {
        "sticker" -> Message.Sticker(
            messageId = messageId,
            senderId = senderId,
            timestamp = timestamp,
            status = status,
            stickerPath = stickerPath,
        )

        "text" -> Message.Text(
            messageId = messageId,
            senderId = senderId,
            timestamp = timestamp,
            status = status,
            text = text,
        )
        "image" -> Message.Image(
            messageId = messageId,
            senderId = senderId,
            timestamp = timestamp,
            status = status,
            image = image,
        )

        else -> {
            Message.Text(
                messageId = messageId,
                senderId = senderId,
                type = "text",
                status = status,
                timestamp = timestamp,
                text = "Unknown message type: $type"
            )
        }
    }
}