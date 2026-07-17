package com.example.hydrogram.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.presentation.states.InboxUiState
import com.example.hydrogram.presentation.viewModel.InboxViewModel
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.presentation.widgets.ChatItem
import com.example.hydrogram.presentation.widgets.ChatListTopBar
import com.example.hydrogram.presentation.widgets.SeparatorLine


@Composable
fun ChatListScreen(
    inboxViewModel: InboxViewModel,
    navController: NavController,
) {
    Scaffold(
        topBar = {
            ChatListTopBar()
        },
        bottomBar = {
            BottomBar(
                navController = navController,
            )
        },
    ) { paddingValues ->
        Content(
            inboxViewModel = inboxViewModel,
            navController = navController,
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun Content(
    inboxViewModel: InboxViewModel,
    navController: NavController,
    paddingValues: PaddingValues,
) {

    val mineId by inboxViewModel.currentId.collectAsStateWithLifecycle()

    val uiState by inboxViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        inboxViewModel.getCurrentUserId()
    }

    LaunchedEffect(mineId) {
        inboxViewModel.observeInboxChats(
            userId = mineId,
        )
    }


    when (val state = uiState) {
        is InboxUiState.Success -> {
            val chats = state.chats
            Log.d("ChatListScreen", "chats: $chats")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
            ) {
                ChatsList(
                    chats = chats,
                    mineId = mineId,
                    navController = navController,
                )
            }

        }

        else -> {
            // Пока данные конкретного человека грузятся, показываем красивый скелетон-плейсхолдер
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Загрузка...", color = Color.LightGray)
            }
        }
    }
}


@Composable
private fun ChatsList(
    chats: List<Chat>,
    mineId: String,
    navController: NavController,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(
            items = chats,
            key = { _, state -> state.chatId }
        ) { index, chat ->

            ChatItem(
                chat = chat,
                mineId = mineId,
                navController = navController,
            )

            if (index != chats.size - 1) {
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


}