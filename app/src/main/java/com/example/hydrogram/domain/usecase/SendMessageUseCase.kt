package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.repository.ChatRepository
import javax.inject.Inject
import kotlin.String

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(
        senderId: String,
        chatId: String,
        content: String,
        isText: Boolean,
    ): Result<Unit> {

        val message = if (isText) {
            Message.Text(
                senderId = senderId,
                status = "sent",
                timestamp = System.currentTimeMillis(),
                text = content,
            )
        } else {
            Message.Sticker(
                senderId = senderId,
                status = "sent",
                timestamp = System.currentTimeMillis(),
                stickerPath = content,
            )
        }

        return chatRepository.sendMessage(
            senderId = senderId,
            chatId = chatId,
            message = message,
        )
    }

}