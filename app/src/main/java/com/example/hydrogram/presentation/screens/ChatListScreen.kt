package com.example.hydrogram.presentation.screens

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.InboxUiState
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.viewModel.InboxViewModel
import com.example.hydrogram.presentation.viewModel.UserViewModel
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.presentation.widgets.ChatItem
import com.example.hydrogram.presentation.widgets.ChatListTopBar
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Gray
import com.example.hydrogram.ui.theme.Red
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

    var contextMenuState by remember { mutableStateOf<ChatContextMenuState?>(null) }
    var selectedChatCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var selectedChat by remember { mutableStateOf<Chat?>(null) }


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
                    onChatLongClick = { chat, coordinates ->
                        selectedChat = chat
                        selectedChatCoordinates = coordinates

                        val positionInRoot = coordinates.positionInRoot()
                        contextMenuState = ChatContextMenuState(
                            chat = chat,
                            position = IntOffset(
                                positionInRoot.x.toInt(),
                                positionInRoot.y.toInt()
                            ),
                            size = coordinates.size.width,
                            isMine = true
                        )
                    }
                )
            }

        }

        else -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Загрузка...", color = Color.LightGray)
            }
        }
    }
    contextMenuState?.let { state ->
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(
                x = state.position.x + state.size / 2,
                y = state.position.y - 52,
            ),
            onDismissRequest = {
                contextMenuState = null
                selectedChat = null
                selectedChatCoordinates = null
            }
        ) {
            ChatActionRow(
                onDeleteClick = {
                    inboxViewModel.deleteChat(
                        chatId = selectedChat?.chatId ?: ""
                    )
                    contextMenuState = null
                    selectedChat = null
                    selectedChatCoordinates = null
                }
            )
        }
    }
}


@Composable
private fun ChatsList(
    chats: List<Chat>,
    mineId: String,
    navController: NavController,
    onChatLongClick: (Chat, LayoutCoordinates) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(
            items = chats,
            key = { _, state -> state.chatId }
        ) { index, chat ->
            val chatCoordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }

            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        chatCoordinates.value = coordinates
                    }
            ) {
                if (chat.chatId == "${mineId}_${mineId}") {
                    FavoriteChatItem(
                        chat = chat,
                        mineId = mineId,
                        navController = navController,
                        onLongClick = {
                            chatCoordinates.value?.let { coordinates ->
                                onChatLongClick(chat, coordinates)
                            }
                        },
                    )
                } else {
                    ChatItem(
                        chat = chat,
                        mineId = mineId,
                        navController = navController,
                        onLongClick = {
                            chatCoordinates.value?.let { coordinates ->
                                onChatLongClick(chat, coordinates)
                            }
                        },
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
}

@Composable
private fun ChatActionRow(
    onDeleteClick: () -> Unit,
) {

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .padding(end = 25.dp)
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(238.dp)
                .clip(
                    shape = RoundedCornerShape(34.dp),
                )
                .background(
                    brush = GlassBackground
                )
                .border(
                    width = 1.dp,
                    brush = GlassBorder,
                    shape = RoundedCornerShape(34.dp),
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                RowMessageAction(
                    item = RowChatActionItem(
                        icon = R.drawable.ic_trashbox,
                        title = "Удалить",
                        onClick = {
                            onDeleteClick()
                        },
                        color = Red,
                    )
                )
            }
        }
    }
}

@Composable
private fun RowMessageAction(
    item: RowChatActionItem,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(40.dp)
            .clickable {
                item.onClick()
            }
            .padding(horizontal = 27.dp)

    ) {
        Icon(
            painter = painterResource(
                item.icon
            ),
            contentDescription = null,
            tint = item.color,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = item.title,
            fontFamily = SfProText,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = item.color,
            letterSpacing = -(0.43).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

data class RowChatActionItem(
    val icon: Int,
    val title: String,
    val onClick: () -> Unit,
    val color: Color,
)

@Composable
fun FavoriteChatItem(
    chat: Chat,
    mineId: String,
    navController: NavController,
    onLongClick: () -> Unit = {},
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
                    .combinedClickable(
                        onClick = {
                            navController.navigate(Screen.Chat.createRoute(id = penpalId))

                        },
                        onLongClick = {
                            onLongClick()
                        }
                    )
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

data class ChatContextMenuState(
    val chat: Chat,
    val position: IntOffset,
    val size: Int,
    val isMine: Boolean = true,
)

@Composable
@Preview(showBackground = true)
fun ChatListScreenPreview() {


}