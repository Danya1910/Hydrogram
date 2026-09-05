package com.example.hydrogram.presentation.screens

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.InboxUiState
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.viewModel.InboxViewModel
import com.example.hydrogram.presentation.viewModel.UserViewModel
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.presentation.widgets.ChatItem
import com.example.hydrogram.presentation.widgets.ChatListTopBar
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.presentation.widgets.UnreadMessageWidget
import com.example.hydrogram.ui.theme.Gray
import com.example.hydrogram.ui.theme.SfProDisplay
import com.example.hydrogram.ui.theme.SfProText
import java.util.Date


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

            if(chat.chatId == "${mineId}_${mineId}") {
                FavoriteChatItem(
                    chat = chat,
                    mineId = mineId,
                    navController = navController,
                )
            } else {
                ChatItem(
                    chat = chat,
                    mineId = mineId,
                    navController = navController,
                )
            }

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
fun FavoriteChatItem(
    chat: Chat,
    mineId: String,
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(key = chat.chatId),
) {

    val penpalId = remember(chat.chatId, mineId) {
        val parts = chat.chatId.split("_")
        // Ищем чужой ID, а если его нет — берем свой (чат с собой)
        parts.firstOrNull { it != mineId } ?: parts.firstOrNull() ?: ""
    }

    val userState by userViewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(penpalId) {
        if (penpalId.isNotBlank()) {
            userViewModel.setTargetUserId(
                uid = penpalId,
            )
        }
    }

    val formattedTime = DateFormat.format(
        "HH:mm", Date(chat.lastMessageTimestamp)
    ).toString()


    when (val state = userState) {
        is UserState.Success -> {

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .height(78.dp)
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.Chat.createRoute(id = penpalId))
                    }
                    .padding(
                        start = 10.dp,
                        end = 16.dp,
                    )
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_favorites),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = "Избранное",
                        fontFamily = SfProDisplay,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = chat.lastMessage,
                        fontFamily = SfProText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 3.dp, bottom = 5.dp)
                        .weight(0.15f)
                ) {
                    Text(
                        text = formattedTime,
                        fontFamily = SfProText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Gray,
                        maxLines = 1,
                    )
                }
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
@Preview(showBackground = true)
fun ChatListScreenPreview() {


}