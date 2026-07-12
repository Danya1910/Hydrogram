package com.example.hydrogram.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.states.ChatAndUserUiState
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.presentation.widgets.ChatItem
import com.example.hydrogram.presentation.widgets.ChatListTopBar
import com.example.hydrogram.presentation.widgets.SeparatorLine
import java.util.UUID


@Composable
fun ChatListScreen() {
    Scaffold(
        topBar = {
            ChatListTopBar()
        },
        bottomBar = {
            BottomBar(
                currentRoute = "Chats",
            )
        },
    ) { paddingValues ->
        Content(
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun Content(
    paddingValues: PaddingValues,
) {

    val currentTimestamp = System.currentTimeMillis()
    val previewChats = listOf(
        Chat(
            senderId = "user_pavel_durov",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Проверь новые обновления безопасности, код отправлен на ревью.",
            lastMessageTimestamp = currentTimestamp
        ),
        Chat(
            senderId = "user_android_dev",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Я перевел список чатов на LazyColumn! 🚀",
            lastMessageTimestamp = currentTimestamp - 5 * 60 * 1000,
            unreadCount = 4
        ),
        Chat(
            senderId = "user_pavel_durov",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Проверь новые обновления безопасности, код отправлен на ревью.",
            lastMessageTimestamp = currentTimestamp
        ),
        Chat(
            senderId = "user_android_dev",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Я перевел список чатов на LazyColumn! 🚀",
            lastMessageTimestamp = currentTimestamp - 5 * 60 * 1000,
            unreadCount = 4
        ),
        Chat(
            senderId = "user_pavel_durov",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Проверь новые обновления безопасности, код отправлен на ревью.",
            lastMessageTimestamp = currentTimestamp
        ),
        Chat(
            senderId = "user_android_dev",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Я перевел список чатов на LazyColumn! 🚀",
            lastMessageTimestamp = currentTimestamp - 5 * 60 * 1000,
            unreadCount = 4
        ),
        Chat(
            senderId = "user_pavel_durov",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Проверь новые обновления безопасности, код отправлен на ревью.",
            lastMessageTimestamp = currentTimestamp
        ),
        Chat(
            senderId = "user_android_dev",
            chatId = UUID.randomUUID().toString(),
            lastMessage = "Я перевел список чатов на LazyColumn! 🚀",
            lastMessageTimestamp = currentTimestamp - 5 * 60 * 1000,
            unreadCount = 4
        ),
    )

    // 2. Создаем тестовых пользователей, сопоставленных с ID из чатов
    val previewUsers = mapOf(
        "user_pavel_durov" to User(
            uid = "user_pavel_durov",
            name = "Павел Дуров",
            avatarUrl = "https://example.com"
        ),
        "user_android_dev" to User(
            uid = "user_android_dev",
            name = "Дмитрий (Android)",
            avatarUrl = ""
        )
    )

    // 3. Маппим (объединяем) их в List<ChatUiState>
    val uiStates = previewChats.map { chat ->
        ChatAndUserUiState(
            chat = chat,
            user = previewUsers[chat.senderId],
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
    ) {
        ChatsList(
            uiState = uiStates,
        )
    }
}


@Composable
private fun ChatsList(
    uiState: List<ChatAndUserUiState>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(
            items = uiState,
            key = { _, state -> state.chat.chatId }
        ) { index, state ->

            ChatItem(
                chat = state.chat,
                user = state.user,
            )

            if (index != uiState.size - 1) {
                SeparatorLine(
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

@Composable
@Preview(showBackground = true)
fun ChatListScreenPreview() {

    ChatListScreen()

}