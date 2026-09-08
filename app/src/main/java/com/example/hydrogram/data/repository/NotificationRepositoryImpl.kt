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
import org.json.JSONObject
import java.io.InputStream
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
        // Проверяем содержимое файла
        val stream = context.assets.open("service_account.json")
        val jsonString = stream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)

        // Логируем ключи, чтобы убедиться, что файл правильный
        Log.d("FCM_FINAL", "Ключи в service_account.json: ${jsonObject.keys().asSequence().toList()}")
        Log.d("FCM_FINAL", "project_id: ${jsonObject.optString("project_id")}")
        Log.d("FCM_FINAL", "client_email: ${jsonObject.optString("client_email")}")

        // Открываем заново для создания credentials
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
        chatId: String
    ): Result<Unit> = runCatching {
        Log.d("FCM_FINAL", "1. Метод отправки пуша ЗАПУЩЕН")

        // 1. Получаем FCM токен получателя из Firestore
        val userDoc = firestore.collection("users").document(targetUserId).get().await()
        val fcmTokensMap = userDoc.get("fcmTokens") as? Map<*, *> ?: emptyMap<String, Boolean>()
        val activeTokens = fcmTokensMap.filterValues { it == true }.keys.map { it.toString() }

        if (activeTokens.isEmpty()) {
            Log.d("FCM_FINAL", "⚠️ У пользователя $targetUserId нет активных токенов.")
            return@runCatching Unit
        }

        // 2. Генерируем Access Token
        Log.d("FCM_FINAL", "2. Начинаем генерацию OAuth 2.0 токена...")
        val oauthToken = getAccessToken()
        Log.d("FCM_FINAL", "3. Токен успешно сгенерирован! Первые 10 символов: ${oauthToken.take(10)}...")

        // 3. Декодируем URL для FCM V1
        val encodedUrl = "aHR0cHM6Ly9mY20uZ29vZ2xlYXBpcy5jb20vdjEvcHJvamVjdHMvaHlkcm9ncmFtL21lc3NhZ2VzOnNlbmQ="
        val decodedUrl = String(android.util.Base64.decode(encodedUrl, android.util.Base64.DEFAULT))

        // 4. Отправляем сообщение на каждое устройство
        for (token in activeTokens) {
            val jsonPayload = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("notification", JSONObject().apply {
                        put("title", senderName)
                        put("body", messageText)
                    })
                    put("data", JSONObject().apply {
                        put("chatId", chatId)
                        put("senderName", senderName)
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
}