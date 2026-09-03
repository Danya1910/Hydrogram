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
            type = type,
            status = status,
            reactions = reactions,
            replyData = replyData,
            isEdited = isEdited,
            stickerPath = stickerPath,
        )

        "text" -> Message.Text(
            messageId = messageId,
            senderId = senderId,
            timestamp = timestamp,
            type = type,
            status = status,
            reactions = reactions,
            replyData = replyData,
            isEdited = isEdited,
            text = text,
        )
        "image" -> Message.Image(
            messageId = messageId,
            senderId = senderId,
            timestamp = timestamp,
            type = type,
            status = status,
            reactions = reactions,
            replyData = replyData,
            isEdited = isEdited,
            image = image,
        )

        else -> {
            Message.Text(
                messageId = messageId,
                senderId = senderId,
                type = "text",
                status = status,
                timestamp = timestamp,
                reactions = reactions,
                replyData = replyData,
                isEdited = isEdited,
                text = "Unknown message type: $type"
            )
        }
    }
}