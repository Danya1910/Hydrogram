package com.example.hydrogram.data.repository

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.example.hydrogram.domain.repository.StorageRepository
import com.example.hydrogram.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URLConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(

) : StorageRepository {

    private val s3Client = AmazonS3Client(
        BasicAWSCredentials(
            BuildConfig.S3_KEY,
            BuildConfig.S3_SECRET,
        )
    ).apply {
        setEndpoint("https://storage.yandexcloud.net")
    }

    private val bucketName = "hydrogram-tg-clone"

    override suspend fun uploadVoiceMessage(localFile: File?, messageId: String): String =
        withContext(Dispatchers.IO) {
            val s3Key = "voice_messages/$messageId.m4a"

            s3Client.putObject(PutObjectRequest(bucketName, s3Key, localFile))

            return@withContext "https://storage.yandexcloud.net/$bucketName/$s3Key"

        }

    override suspend fun uploadImageMessage(
        imageBytes: ByteArray,
        type: String,
        userId: String,
        chatId: String
    ): String = withContext(Dispatchers.IO) {

        if (imageBytes.isEmpty()) throw IllegalArgumentException("Массив байт пуст")

        val extension = type.substringAfter("/", "jpg")

        val timestamp = System.currentTimeMillis()
        val s3Key = "chats/$chatId/${userId}_$timestamp.$extension"

        val metadata = ObjectMetadata().apply {
            contentType = type
            contentLength = imageBytes.size.toLong()
        }

        val inputStream = ByteArrayInputStream(imageBytes)

        val request = PutObjectRequest(bucketName, s3Key, inputStream, metadata)
        s3Client.putObject(request)

        return@withContext "https://storage.yandexcloud.net/$bucketName/$s3Key"
    }

    override suspend fun uploadAvatar(
        imageBytes: ByteArray,
        userId: String,
        type: String,
    ): String =
        withContext(Dispatchers.IO) {

            if (imageBytes.isEmpty()) throw IllegalArgumentException("Массив байт пуст")

            val extension = type.substringAfter("/", "jpg")

            val timestamp = System.currentTimeMillis()
            val s3Key = "users/$userId/avatar_$timestamp.$extension"


            val metadata = ObjectMetadata().apply {
                contentType = type
                contentLength = imageBytes.size.toLong()
            }

            val inputStream = ByteArrayInputStream(imageBytes)

            val request = PutObjectRequest(bucketName, s3Key, inputStream, metadata)
            s3Client.putObject(request)

            return@withContext "https://storage.yandexcloud.net/$bucketName/$s3Key"
        }

}