package com.example.hydrogram.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.hydrogram.domain.repository.NotificationRepository
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : NotificationRepository {

    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val stream = context.assets.open("service_account.json")
        val jsonString = stream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)

        Log.d("FCM_FINAL", "Ключи в service_account.json: ${jsonObject.keys().asSequence().toList()}")
        Log.d("FCM_FINAL", "project_id: ${jsonObject.optString("project_id")}")
        Log.d("FCM_FINAL", "client_email: ${jsonObject.optString("client_email")}")

        val newStream = context.assets.open("service_account.json")
        val credentials = GoogleCredentials.fromStream(newStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

        credentials.refresh()
        return@withContext credentials.accessToken.tokenValue
    }

    override suspend fun sendPushNotification(
        targetUserId: String,
        senderName: String,
        messageText: String,
        chatId: String,
        avatarBase64: String,
    ): Result<Unit> = runCatching {
        Log.d("FCM_FINAL", "1. Метод отправки пуша ЗАПУЩЕН")
        Log.d("FCM_FINAL", "mineAvatar: $avatarBase64")

        val userDoc = firestore.collection("users").document(targetUserId).get().await()
        val fcmTokensMap = userDoc.get("fcmTokens") as? Map<*, *> ?: emptyMap<String, Boolean>()
        val activeTokens = fcmTokensMap.filterValues { it == true }.keys.map { it.toString() }

        if (activeTokens.isEmpty()) {
            Log.d("FCM_FINAL", "⚠️ У пользователя $targetUserId нет активных токенов.")
            return@runCatching Unit
        }

        Log.d("FCM_FINAL", "2. Начинаем генерацию OAuth 2.0 токена...")
        val oauthToken = getAccessToken()
        Log.d("FCM_FINAL", "3. Токен успешно сгенерирован! Первые 10 символов: ${oauthToken.take(10)}...")

        val encodedUrl = "aHR0cHM6Ly9mY20uZ29vZ2xlYXBpcy5jb20vdjEvcHJvamVjdHMvaHlkcm9ncmFtL21lc3NhZ2VzOnNlbmQ="
        val decodedUrl = String(android.util.Base64.decode(encodedUrl, android.util.Base64.DEFAULT))

        val compressedAvatar = resizeBase64Avatar(avatarBase64)

        for (token in activeTokens) {
            // 🌟 ИСПРАВЛЕНИЕ: Перенесли title и body в data, удалив объект notification
            val jsonPayload = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("data", JSONObject().apply {
                        put("title", senderName)
                        put("body", messageText)
                        put("chatId", chatId)
                        put("senderName", senderName)
                        put("avatarBase64", compressedAvatar)
                    })
                })
            }

            withContext(Dispatchers.IO) {
                val url = URL(decodedUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Authorization", "Bearer $oauthToken")
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("FCM_FINAL", "✅ Пуш доставлен! Ответ: $responseBody")
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    Log.e("FCM_FINAL", "❌ Ошибка Firebase V1: $responseCode | $errorBody")
                }
                connection.disconnect()
            }
        }
        Unit
    }.onFailure { exception ->
        Log.e("FCM_FINAL", "❌ ФАТАЛЬНЫЙ СБОЙ В РЕПОЗИТОРИИ:", exception)
    }

    private fun resizeBase64Avatar(originalBase64: String): String {
        return try {
            if (originalBase64.isEmpty()) return ""
            val cleanBase64 = originalBase64.substringAfter(",")
            val decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
            val originalBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size) ?: return ""

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 64, 64, true)
            val alphaBitmap = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(alphaBitmap)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, null)

            val outputStream = java.io.ByteArrayOutputStream()
            alphaBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val byteArray = outputStream.toByteArray()

            android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
