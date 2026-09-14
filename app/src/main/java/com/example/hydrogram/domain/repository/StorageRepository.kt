package com.example.hydrogram.domain.repository

import java.io.File

interface StorageRepository {

    suspend fun uploadVoiceMessage(
        localFile: File?,
        messageId: String,
    ) : String

}