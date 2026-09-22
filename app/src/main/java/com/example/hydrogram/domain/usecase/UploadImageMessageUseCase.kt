package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.StorageRepository
import javax.inject.Inject

class UploadImageMessageUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
) {

    suspend operator fun invoke(
        imageBytes: ByteArray,
        type: String,
        userId: String,
        chatId: String,
    ) : String {
        return storageRepository.uploadImageMessage(
            imageBytes = imageBytes,
            type = type,
            userId = userId,
            chatId = chatId,
        )
    }

}