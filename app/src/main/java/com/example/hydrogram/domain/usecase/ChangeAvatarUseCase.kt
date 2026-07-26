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

class ChangeAvatarUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke(
        uid: String,
        imageUri: Uri,
    ) : Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                return@withContext Result.failure(Exception("Не удалось прочитать изображение"))
            }

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 120, 120, true)
            val outputStream = ByteArrayOutputStream()

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            originalBitmap.recycle()
            scaledBitmap.recycle()

            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            val finalAvatarDataString = "data:image/jpeg;base64,$base64String"

            userRepository.changeAvatar(uid = uid, avatarString = finalAvatarDataString)


        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

}