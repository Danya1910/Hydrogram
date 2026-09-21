package com.example.hydrogram.domain.usecase

import com.example.hydrogram.domain.repository.StorageRepository
import java.io.File
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
) {

    suspend operator fun invoke(
        imageBytes: ByteArray,
        userId: String,
        type: String,
    ) : String {
        return  storageRepository.uploadAvatar(
            imageBytes = imageBytes,
            userId = userId,
            type = type,
        )
    }

}