package com.example.hydrogram.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.presentation.widgets.ChatItem
import com.example.hydrogram.presentation.widgets.DividingLine
import java.util.UUID


class ChatListScreen {
}

@Composable
@Preview(showBackground = true)
fun ChatListScreenPreview() {

    val currentTimestamp = System.currentTimeMillis()
    val previewChats = listOf<Chat>(
        Chat(
            senderId = "user_pavel_durov",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Проверь новые обновления безопасности, код отправлен на ревью.",
            lastMessageType = "TEXT",
            lastMessageSenderId = "user_pavel_durov",
            lastMessageTimestamp = currentTimestamp,
            unreadCount = 3// Только что
        ),
        Chat(
            senderId = "user_android_dev",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Я перевел список чатов на LazyColumn, теперь все скроллится просто идеально! 🚀",
            lastMessageType = "TEXT",
            lastMessageSenderId = "my_current_user_id", // Отправлено нами
            lastMessageTimestamp = currentTimestamp - 5 * 60 * 1000, // 5 минут назад
            unreadCount = 4,
        ),
        Chat(
            senderId = "user_mom",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Покушал? Шапку надел?",
            lastMessageType = "TEXT",
            lastMessageSenderId = "user_mom",
            lastMessageTimestamp = currentTimestamp - 45 * 60 * 1000 // 45 минут назад
        ),
        Chat(
            senderId = "channel_hydrogram_news",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "🔥 Релиз новой версии Hydrogram 1.0! Скачивайте и тестируйте прямо сейчас.",
            lastMessageType = "TEXT",
            lastMessageSenderId = "channel_hydrogram_news",
            lastMessageTimestamp = currentTimestamp - 3 * 60 * 60 * 1000 // 3 часа назад
        ),
        Chat(
            senderId = "user_designer_artem",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "[Голосовое сообщение]",
            lastMessageType = "VOICE",
            lastMessageSenderId = "user_designer_artem",
            lastMessageTimestamp = currentTimestamp - 24 * 60 * 60 * 1000 // Вчера в это же время
        ),
        Chat(
            senderId = "user_work_group",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Иван: Созвон по багам переносим на пятницу на 14:00.",
            lastMessageType = "TEXT",
            lastMessageSenderId = "user_ivan_colleague",
            lastMessageTimestamp = currentTimestamp - 2 * 24 * 60 * 60 * 1000 // 2 дня назад
        )
    )

    ChatsList(
        chats = previewChats
    )
}


@Composable
private fun ChatsList(
    chats: List<Chat>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(
            items = chats,
            key = { _, chat -> chat.chatId }
        ) { index, chat ->

            ChatItem(
                chat = chat,
                user = null,
            )

            if (index != chats.size - 1) {
                DividingLine(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 82.dp,
                            end = 16.dp
                        )
                )
            }
        }
    }

}