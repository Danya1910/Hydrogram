package com.example.hydrogram.data.service

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.core.app.NotificationCompat
import android.graphics.Bitmap
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner

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

        // 🌟 НОВЫЙ СПОСОБ: Проверяем реальное состояние приложения через Jetpack Lifecycle
        val isAppInForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

        if (isAppInForeground) {
            showInAppNotification(remoteMessage)
            return
        }

        val title = remoteMessage.data["title"] ?: "Новое сообщение"
        val body = remoteMessage.data["body"] ?: ""
        val chatId = remoteMessage.data["chatId"]
        val avatarBase64 = remoteMessage.data["avatarBase64"]
        val avatarBitmap = avatarBase64?.let { getBitmapFromBase64(it) }

        showNotification(title, body, chatId, avatarBitmap)
    }

    private fun showInAppNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Новое сообщение"
        val body = remoteMessage.data["body"] ?: ""
        val chatId = remoteMessage.data["chatId"]

        // Безопасный способ передачи данных в UI через Broadcast
        sendBroadcast(Intent("com.example.hydrogram.NEW_MESSAGE").apply {
            putExtra("title", title)
            putExtra("body", body)
            putExtra("chatId", chatId)
        })
    }

    private fun showNotification(
        title: String,
        body: String,
        chatId: String?,
        avatarBitmap: Bitmap?,
    ) {
        val channelId = "hydrogram_chats_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Сообщения чата",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            action = "OPEN_CHAT_ACTIVITY"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("CHAT_ID", chatId)
        }

        val notificationId = chatId?.hashCode() ?: 0
        val shortcutId = "shortcut_chat_$chatId"

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val circleAvatar = avatarBitmap?.let { getCircleBitmap(it) }

        val iconCompat = if (circleAvatar != null) {
            IconCompat.createWithBitmap(circleAvatar)
        } else {
            IconCompat.createWithResource(this, R.drawable.ic_contacts)
        }

        val sender = Person.Builder()
            .setName(title)
            .setIcon(iconCompat)
            .setImportant(true)
            .build()

        val shortcut = ShortcutInfoCompat.Builder(this, shortcutId)
            .setShortLabel(title)
            .setIcon(iconCompat)
            .setIntent(intent)
            .setLongLived(true)
            .setPerson(sender)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)

        val messagingStyle = NotificationCompat.MessagingStyle(sender)
            .setConversationTitle(title)
            .addMessage(body, System.currentTimeMillis(), sender)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_telegram)
            .setStyle(messagingStyle)
            .setShortcutId(shortcutId)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun getCircleBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint().apply { isAntiAlias = true }
        val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, bitmap.width / 2f, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    private fun getBitmapFromBase64(base64Str: String): Bitmap? {
        return try {
            val cleanBase64 = base64Str.substringAfter(",")
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
