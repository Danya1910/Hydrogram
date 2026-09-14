package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.StorageRepository
import java.io.File
import javax.inject.Inject

class UploadVoiceMessageUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
) {

    suspend operator fun invoke(
        localFile: File?,
        messageId: String,
    ) : String {
        return storageRepository.uploadVoiceMessage(
            localFile = localFile,
            messageId = messageId,
        )
    }


}