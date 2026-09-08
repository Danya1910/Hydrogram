package com.example.hydrogram.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.hydrogram.MainActivity
import com.example.hydrogram.R
import com.example.hydrogram.domain.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MessengerMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            authRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "Новое сообщение"
        val body = remoteMessage.notification?.body ?: ""
        val chatId = remoteMessage.data["chatId"]

        showNotification(title, body, chatId)
    }

    private fun showNotification(title: String, body: String, chatId: String?) {
        val channelId = "hydrogram_chats_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем обязательный канал уведомлений для Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Сообщения чата",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Настраиваем Intent: что будет, если нажать на плашку уведомления
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "OPEN_CHAT_ACTIVITY"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("CHAT_ID", chatId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode(), // Уникальный код, чтобы уведомления разных чатов не заменяли друг друга
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_contacts) // ⚠️ Убедитесь, что у вас есть иконка в res/drawable
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true) // Удалять из шторки после клика
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(chatId.hashCode(), notification)
    }

}