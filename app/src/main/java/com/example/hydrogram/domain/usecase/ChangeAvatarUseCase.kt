package com.example.hydrogram.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.hydrogram.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import android.net.Uri
import android.util.Base64
import com.example.hydrogram.domain.repository.StorageRepository

class ChangeAvatarUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository,
) {

    suspend operator fun invoke(
        imageBytes: ByteArray,
        uid: String,
        type: String,
    ) : Result<Unit> = withContext(Dispatchers.IO) {
        try {

            val imageUrl = storageRepository.uploadAvatar(
                imageBytes = imageBytes,
                userId = uid,
                type = type,
            )

            userRepository.changeAvatar(uid = uid, avatarString = imageUrl)

            Result.success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

}