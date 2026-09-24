package com.example.hydrogram.domain.repository

import java.io.File

interface StorageRepository {

    suspend fun uploadVoiceMessage(
        localFile: File?,
        messageId: String,
    ): String

    suspend fun uploadCircleVideoMessage(
        localFile: File?,
        messageId: String,
    ) : String

    suspend fun uploadImageMessage(
        imageBytes: ByteArray,
        type: String,
        userId: String,
        chatId: String,
    ): String

    suspend fun uploadAvatar(
        imageBytes: ByteArray,
        userId: String,
        type: String,
    ): String

}