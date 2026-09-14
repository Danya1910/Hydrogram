package com.example.hydrogram.data.repository

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.example.hydrogram.domain.repository.StorageRepository
import com.example.hydrogram.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
}