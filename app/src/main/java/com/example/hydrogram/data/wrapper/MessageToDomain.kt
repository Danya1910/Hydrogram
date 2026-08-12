package com.example.hydrogram.data.wrapper

import com.example.hydrogram.data.dto.MessageDto
import com.example.hydrogram.domain.model.Message

fun MessageDto.toDomain() : Message {
    return when (type) {
        "sticker" -> Message.Sticker(
            messageId = messageId,
            senderId = senderId,
            timestamp = timestamp,
            status = status,
            stickerPath = stickerPath,
        )
        else -> Message.Text(
            messageId = messageId,
            senderId = senderId,
            timestamp = timestamp,
            status = status,
            text = text,
        )
    }
}