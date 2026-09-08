package com.example.hydrogram.data.repository

import android.content.Context
import android.util.Log
import com.example.hydrogram.domain.repository.NotificationRepository
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context,
) : NotificationRepository {

    private val httpClient = OkHttpClient()

    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val stream: InputStream = context.assets.open("service_account.json")
        val credentials = GoogleCredentials.fromStream(stream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        credentials.refreshIfExpired()
        return@withContext credentials.accessToken.tokenValue
    }

    override suspend fun sendPushNotification(
        targetUserId: String,
        senderName: String,
        messageText: String,
        chatId: String
    ): Result<Unit> = runCatching {
        val userDoc =firestore
            .collection("users")
            .document(targetUserId)
            .get()
            .await()
        val fcmTokensMap = userDoc.get("fcmTokens") as? Map<*, *> ?: emptyMap<String, Boolean>()
        val activeTokens = fcmTokensMap.filterValues { it == true }.keys.map { it.toString() }

        if (activeTokens.isEmpty()) {
            Log.d("FCM_V1", "У пользователя $targetUserId нет активных токенов.")
            return@runCatching
        }

        val accessTokens = getAccessToken()
        val mediaType = "application/json; charset=utf-8".toMediaType()

        for(token in accessTokens) {
            val jsonPayload = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("notification", JSONObject().apply {
                        put("title", senderName)
                        put("body", messageText)
                    })
                    put("data", JSONObject().apply {
                        put("chatId", chatId)
                    })
                })
            }
            val requestBody = jsonPayload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://googleapis.com")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $accessTokens")
                .addHeader("Content-Type", "application/json")
                .build()

            withContext(Dispatchers.IO) {
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("FCM_V1", "Ошибка отправки на токен: ${response.code} | ${response.body?.string()}")
                    } else {
                        Log.d("FCM_V1", "Пуш успешно отправлен на устройство!")
                    }
                }
            }
        }

    }

}