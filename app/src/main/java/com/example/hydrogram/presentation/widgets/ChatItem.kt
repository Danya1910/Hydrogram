package com.example.hydrogram.presentation.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hydrogram.domain.model.Chat
import java.util.UUID


@Composable
fun ChatItem(
    chat: Chat,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(78.dp)
            .fillMaxWidth()
            .padding(start = 10.dp, end = 16.dp)
    ) {

    }
}

@Composable
@Preview(showBackground = true)
fun ChatItemPreview(
) {

    val sampleChat = Chat(
        senderId = "user_12345",                        // ID собеседника
        chatId = UUID.randomUUID().toString(),          // Уникальный ID самого чата
        lastMessage = "Привет! Как продвигается копия ТГ?", // Текст последнего сообщения
        lastMessageType = "TEXT",                       // Тип (TEXT, IMAGE, VOICE и т.д.)
        lastMessageSenderId = "user_12345",             // Кто отправил последнее сообщение
        lastMessageTimestamp = System.currentTimeMillis() // Время отправки в миллисекундах
    )

    ChatItem(
        chat = sampleChat
    )
}