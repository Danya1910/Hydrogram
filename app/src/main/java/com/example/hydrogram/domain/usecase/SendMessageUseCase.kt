package com.example.hydrogram.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.model.ReplyData
import com.example.hydrogram.domain.repository.ChatRepository
import com.example.hydrogram.domain.repository.NotificationRepository
import com.example.hydrogram.domain.repository.StorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import kotlin.String

class SendMessageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository,
    private val notificationRepository: NotificationRepository,
    private val storageRepository: StorageRepository,
) {

    suspend operator fun invoke(
        senderId: String,
        chatId: String,
        content: String = "",
        audio: File? = null,
        voiceDuration: Int? = 0,
        messageType: String,
        imageBytes: ByteArray? = null,
        type: String? = null,
        replyData: ReplyData? = null,
        targetUserId: String,
        senderName: String,
        senderAvatar: String,
        recordingAmplitudes: List<Float>? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {

        try {

            val pushMessageText = when (messageType) {
                "text" -> content
                "sticker" -> "Стикер"
                "image" -> "Фотография"
                "voice" -> "Голосовое сообщение"
                else -> "Сообщение"
            }

            val message = when (messageType) {
                "text" -> {
                    Message.Text(
                        senderId = senderId,
                        status = "sent",
                        timestamp = System.currentTimeMillis(),
                        text = content,
                        replyData = replyData,
                    )
                }

                "sticker" -> {
                    Message.Sticker(
                        senderId = senderId,
                        status = "sent",
                        timestamp = System.currentTimeMillis(),
                        stickerPath = content,
                        replyData = replyData,
                    )
                }

                "image" -> {
                    val imageUrl = imageBytes?.let {
                        storageRepository.uploadImageMessage(
                            imageBytes = it,
                            type = type ?: "",
                            userId = senderId,
                            chatId = chatId
                        )
                    }

                    Message.Image(
                        senderId = senderId,
                        status = "sent",
                        timestamp = System.currentTimeMillis(),
                        image = imageUrl,
                        replyData = replyData,
                    )
                }

                "voice" -> {

                    val messageId = chatRepository.generateMessageId(chatId)

                    val audioUrl = storageRepository.uploadVoiceMessage(
                        localFile = audio,
                        messageId = messageId,
                    )

                    Message.Voice(
                        messageId = messageId,
                        senderId = senderId,
                        status = "sent",
                        timestamp = System.currentTimeMillis(),
                        audioUrl = audioUrl,
                        durationSeconds = voiceDuration,
                        replyData = replyData,
                        recordingAmplitudes = recordingAmplitudes,
                    )

                }

                else -> {
                    return@withContext Result.failure(Exception("Неизвестный тип сообщения"))
                }
            }


            val sendResult = chatRepository.sendMessage(
                senderId = senderId,
                chatId = chatId,
                message = message,
            )

            if (sendResult.isSuccess) {
                notificationRepository.sendPushNotification(
                    targetUserId = targetUserId,
                    senderName = senderName,
                    messageText = pushMessageText,
                    chatId = chatId,
                    avatarBase64 = senderAvatar,
                )

                Result.success(Unit)
            } else {
                Result.failure(
                    sendResult.exceptionOrNull() ?: Exception("Ошибка сохранения сообщения")
                )
            }

        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}