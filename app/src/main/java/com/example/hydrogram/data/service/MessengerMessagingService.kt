package com.example.hydrogram.data.service

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

        // Получаем строку
        val avatarBase64 = remoteMessage.data["avatarBase64"]


        // ЛОГ 1: Проверяем, пришла ли вообще строка и её длину
        android.util.Log.d("FCM_AVATAR", "Пришла строка Base64: ${avatarBase64?.take(30)}... Длина: ${avatarBase64?.length}")

        val avatarBitmap = avatarBase64?.let { getBitmapFromBase64(it) }

        // ЛОГ 2: Проверяем, создался ли Bitmap успешным
        android.util.Log.d("FCM_AVATAR", "Результат декодирования Bitmap: ${avatarBitmap != null} (Ширина: ${avatarBitmap?.width}, Высота: ${avatarBitmap?.height})")

        showNotification(title, body, chatId, avatarBitmap)
    }

    private fun showNotification(
        title: String, // Имя отправителя
        body: String,  // Текст сообщения
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
        val shortcutId = "shortcut_chat_$chatId" // Уникальный ID ярлыка для этого чата

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 1. Создаем иконку для ярлыка и стиля
        val iconCompat = if (avatarBitmap != null) {
            IconCompat.createWithBitmap(avatarBitmap)
        } else {
            IconCompat.createWithResource(this, R.drawable.ic_contacts)
        }

        // 2. Создаем объект пользователя чата
        val sender = Person.Builder()
            .setName(title)
            .setIcon(iconCompat)
            .setImportant(true)
            .build()

        // 3. ОБЯЗАТЕЛЬНЫЙ ШАГ ДЛЯ АНДРОИД 11+: Создаем и регистрируем ярлык чата
        val shortcut = ShortcutInfoCompat.Builder(this, shortcutId)
            .setShortLabel(title)
            .setIcon(iconCompat)
            .setIntent(intent)
            .setLongLived(true) // Позволяет системе сохранять ярлык в кэше
            .setPerson(sender)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)

        // 4. Настраиваем MessagingStyle
        val messagingStyle = NotificationCompat.MessagingStyle(sender)
            .setConversationTitle(title)
            .addMessage(body, System.currentTimeMillis(), sender)

        // 5. Собираем уведомление
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_contacts)
            .setStyle(messagingStyle)
            .setShortcutId(shortcutId) // 🌟 Связываем уведомление с созданным ярлыком
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    // Вспомогательный метод скругления аватарки (обязательно оставьте его в сервисе)
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