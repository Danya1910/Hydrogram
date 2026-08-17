package com.example.hydrogram.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.repository.ChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.String

class SendMessageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository,
) {

    suspend operator fun invoke(
        senderId: String,
        chatId: String,
        content: String = "",
        messageType: String,
        imageUri: Uri? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {

        try {
            val message = when (messageType) {
                "text" -> {
                    Message.Text(
                        senderId = senderId,
                        status = "sent",
                        timestamp = System.currentTimeMillis(),
                        text = content,
                    )
                }
                "sticker" -> {
                    Message.Sticker(
                        senderId = senderId,
                        status = "sent",
                        timestamp = System.currentTimeMillis(),
                        stickerPath = content,
                    )
                }
                "image" -> {
                    val uri = imageUri ?: return@withContext Result.failure(
                        Exception("URI изображения не передан")
                    )

                    val imageData = convertImageToOptimizedBase64(uri)

                    Message.Image(
                        senderId = senderId,
                        status = "sent",
                        timestamp = System.currentTimeMillis(),
                        image = imageData,
                    )
                }
                else -> {
                    return@withContext Result.failure(Exception("Неизвестный тип сообщения"))
                }
            }

            return@withContext chatRepository.sendMessage(
                senderId = senderId,
                chatId = chatId,
                message = message,
            )

        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private fun convertImageToOptimizedBase64(imageUri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw Exception("Не удалось открыть поток изображения")

        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (originalBitmap == null) {
            throw Exception("Не удалось декодировать изображение")
        }

        try {
            val maxSideTarget = 800f
            val width = originalBitmap.width
            val height = originalBitmap.height

            val scaleFactor = if (width > height) {
                maxSideTarget / width
            } else {
                maxSideTarget / height
            }

            val bitmapToCompress = if (scaleFactor < 1f) {
                val finalWidth = (width * scaleFactor).toInt()
                val finalHeight = (height * scaleFactor).toInt()
                Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)
            } else {
                originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            }

            originalBitmap.recycle()

            if (bitmapToCompress == null || bitmapToCompress.isRecycled) {
                throw Exception("Не удалось создать изображение для сжатия")
            }

            val outputStream = ByteArrayOutputStream()
            bitmapToCompress.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            outputStream.close()

            bitmapToCompress.recycle()

            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            return "data:image/jpeg;base64,$base64String"

        } catch (e: Exception) {
            if (!originalBitmap.isRecycled) {
                originalBitmap.recycle()
            }
            throw e
        }
    }
}