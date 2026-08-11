package com.example.hydrogram.presentation.screens

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.request.ImageRequest
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.ChatUiState
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.util.formatHeaderDate
import com.example.hydrogram.presentation.util.generateChatId
import com.example.hydrogram.presentation.util.getStartOfDay
import com.example.hydrogram.presentation.viewModel.ChatViewModel
import com.example.hydrogram.presentation.viewModel.UserViewModel
import com.example.hydrogram.presentation.widgets.ChatInputField
import com.example.hydrogram.presentation.widgets.TopChatBar
import com.example.hydrogram.ui.theme.DateSeparatorGreen
import com.example.hydrogram.ui.theme.LightGreen
import com.example.hydrogram.ui.theme.MineMessageTimeColor
import com.example.hydrogram.ui.theme.PenpalMessageTimeColor
import com.example.hydrogram.ui.theme.Separator
import com.example.hydrogram.ui.theme.SfProText
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import java.util.Date
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.ImageLoader
import coil3.request.crossfade
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.ui.theme.LightGrayBackground


@OptIn(ExperimentalHazeMaterialsApi::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    penpalId: String?,
) {

    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val mineId by chatViewModel.currentId.collectAsStateWithLifecycle()
    val presenceState by userViewModel.opponentPresenceState.collectAsStateWithLifecycle()

    val hazeState = remember { HazeState() }

    val chatId = remember(mineId, penpalId) {
        if (mineId.isNotEmpty() && !penpalId.isNullOrEmpty()) {
            generateChatId(userId1 = mineId, userId2 = penpalId)
        } else ""
    }

    LaunchedEffect(penpalId) {
        userViewModel.setTargetUserId(uid = penpalId ?: "")
    }

    val penpalData by userViewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        chatViewModel.getCurrentUserId()
        Log.d("ChatScreen", "mineId: $mineId, penpalId: $penpalId")
    }

    LaunchedEffect(mineId) {
        Log.d("ChatScreen", "mineId: $mineId")
    }

    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            chatViewModel.observeChatHistory(chatId = chatId)
            Log.d("ChatScreen", "Успешный старт чата по ID: $chatId")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hazeChild(
                            state = hazeState,
                            shape = RectangleShape,
                            style = HazeDefaults.style(
                                backgroundColor = Color.White.copy(alpha = 0.01f),
                                blurRadius = 6.dp
                            )
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.7f),
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.0f)
                                )
                            )
                        )
                ) {
                    when (val state = penpalData) {
                        is UserState.Loading -> {
                            TopChatBar(
                                user = User(name = "Loading"),
                                onUserClick = {},
                                onBackClick = { navController.popBackStack() },
                                presenceState = presenceState,
                                isFavorites = penpalId == mineId,
                            )
                        }

                        is UserState.Error -> {
                            TopChatBar(
                                user = User(name = "Error"),
                                onUserClick = {},
                                onBackClick = { navController.popBackStack() },
                                presenceState = presenceState,
                                isFavorites = penpalId == mineId,
                            )
                        }

                        is UserState.Success -> {
                            val user = state.user
                            Log.d("ChatScreen", "данные собеседника: $user")

                            TopChatBar(
                                user = user ?: User(),
                                onBackClick = { navController.popBackStack() },
                                onUserClick = {
                                    if (penpalId != mineId) {
                                        navController.navigate(
                                            Screen.UserProfile.createRoute(id = penpalId ?: "")
                                        )
                                    }
                                },
                                presenceState = presenceState,
                                isFavorites = penpalId == mineId,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(hazeState)
            ) {
                Image(
                    painter = painterResource(R.drawable.light_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is ChatUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is ChatUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = state.message, color = Color.Red)
                            }
                        }

                        is ChatUiState.Success -> {
                            val messages = state.messages
                            Log.d("ChatScreen", "messages: $messages")

                            Content(
                                messages = messages,
                                chatViewModel = chatViewModel,
                                bottomPadding = paddingValues.calculateBottomPadding(),
                                mineId = mineId,
                                chatId = chatId,
                            )
                        }
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun Content(
    messages: List<Message>,
    chatViewModel: ChatViewModel,
    bottomPadding: Dp,
    mineId: String,
    chatId: String,
) {

    val context = LocalContext.current

    var textState by remember { mutableStateOf("") }

    val gifImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val groupedMessages = remember(messages) {
        messages.groupBy { message -> getStartOfDay(message.timestamp) }
    }

    val listState = rememberLazyListState()

    var firstUnreadMessageId by remember { mutableStateOf<String?>(null) }

    val hazeState = remember { HazeState() }

    var isStickerWidgetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firstUnreadMessageId = messages
            .filter { it.senderId != mineId && it.status != "read" }
            .minByOrNull { it.timestamp }?.messageId
    }

    LaunchedEffect(firstUnreadMessageId) {
        val unreadId = firstUnreadMessageId
        if (unreadId != null) {
            val itemIndex = listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == unreadId }?.index

            if (itemIndex != null) {
                listState.scrollToItem(index = itemIndex, scrollOffset = 0)
            } else {
                var targetIndex = 0
                var found = false

                for ((_, dayMessages) in groupedMessages) {
                    if (found) break
                    targetIndex++

                    for (msg in dayMessages) {
                        if (msg.messageId == unreadId) {
                            found = true
                            break
                        }
                        targetIndex++
                    }
                }

                if (found) {
                    listState.scrollToItem(index = targetIndex, scrollOffset = 0)
                }
            }
        } else {
            if (messages.isNotEmpty()) {
                listState.scrollToItem(index = listState.layoutInfo.totalItemsCount)
            }
        }
    }

    LaunchedEffect(listState.layoutInfo.visibleItemsInfo) {
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        if (visibleItems.isNotEmpty()) {
            visibleItems.forEach { visibleItem ->
                val keyString = visibleItem.key as? String

                if (keyString != null && !keyString.startsWith("date_")) {
                    val message = messages.find { it.messageId == keyString }
                    if (message != null && message.senderId != mineId && message.status != "read") {
                        chatViewModel.changeMessageStatus(
                            chatId = chatId,
                            messageId = message.messageId,
                            status = "read",
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(messages.size) {

        if (messages.isNotEmpty()) {
            val lastItemIndex = listState.layoutInfo.totalItemsCount

            if (lastItemIndex > 0) {
                listState.scrollToItem(
                    index = lastItemIndex - 1,
                    scrollOffset = 0,
                )
            }

        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        if (messages.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NewChatWidget(
                    onGreetingClick = {},
                    context = context,
                    gifImageLoader = gifImageLoader,
                )
            }
        }

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                top = 86.dp,
                bottom = 75.dp,
            ),
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
                .clickable(
                    enabled = isStickerWidgetVisible,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isStickerWidgetVisible) {
                        isStickerWidgetVisible = false
                    }
                },
        ) {
            groupedMessages.forEach { (dayTimestamp, dayMessages) ->


                item(key = "date_$dayTimestamp") {
                    DateSeparator(text = formatHeaderDate(dayTimestamp))
                }

                items(
                    items = dayMessages,
                    key = { message -> message.messageId }
                ) { message ->

                    if (message.messageId == firstUnreadMessageId) {
                        Spacer(modifier = Modifier.height(8.dp))
                        UnreadMessageSeparator()
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (message.senderId == mineId) {
                        MineTextMessage(message = message)
                    } else {
                        PenpalTextMessage(message = message)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .hazeChild(
                        state = hazeState,
                        shape = RectangleShape,
                        style = HazeDefaults.style(
                            backgroundColor = Color.White.copy(alpha = 0.01f),
                            blurRadius = 6.dp
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.0f),
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                ChatInputField(
                    inputText = textState,
                    onValueChange = { newValue -> textState = newValue },
                    onSendClick = {
                        if (textState.isNotBlank()) {
                            val messageText = textState
                            textState = ""
                            chatViewModel.sendMessage(
                                senderId = mineId,
                                chatId = chatId,
                                text = messageText,
                                type = "text",
                            )
                        }
                    },
                    onAttachClick = { println("Нажата скрепка") },
                    onStickerClick = {
                        isStickerWidgetVisible = !isStickerWidgetVisible
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = isStickerWidgetVisible,
            enter = slideInVertically(
                animationSpec = spring(stiffness = 400f),
                initialOffsetY = { fullHeight -> fullHeight }
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                animationSpec = spring(stiffness = 400f),
                targetOffsetY = { fullHeight -> fullHeight }
            ) + fadeOut(animationSpec = tween(200)),
        ) {
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxSize()
            ) {
                StickerWidget(
                    context = context,
                    gifImageLoader = gifImageLoader,
                )
            }
        }

    }

}

@Composable
private fun UnreadMessageSeparator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(24.dp)
            .fillMaxWidth()
            .background(
                color = Separator.copy(alpha = 0.8f)
            )
    ) {
        Text(
            text = "Непрочитанные сообщения",
            fontFamily = SfProText,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Gray,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun DateSeparator(
    text: String,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(radius = 1.dp)
                    .background(
                        color = DateSeparatorGreen.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
            )
            Text(
                text = text,
                fontFamily = SfProText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp),
                letterSpacing = (-0.08).sp,
            )
        }
    }
}

@Composable
private fun MineTextMessage(
    message: Message,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    BoxWithConstraints(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val maxBubbleWidth = maxWidth * 0.85f

        Box(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .widthIn(max = maxBubbleWidth)
                .clip(
                    shape = RoundedCornerShape(
                        bottomEnd = 2.dp,
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                    )
                )
                .background(
                    color = LightGreen,
                )
        ) {
            if (message.text.length <= 20) {
                Text(
                    text = message.text,
                    fontFamily = SfProText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(
                            top = 5.dp,
                            start = 10.dp,
                            end = 62.dp,
                            bottom = 5.dp
                        )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 3.dp)
                        .align(
                            Alignment.BottomEnd
                        ),
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SfProText,
                        color = MineMessageTimeColor,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    if (message.status == "read") {
                        Icon(
                            painter = painterResource(R.drawable.ic_read_status),
                            contentDescription = null,
                            tint = MineMessageTimeColor,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_sent_status),
                            contentDescription = null,
                            tint = MineMessageTimeColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = message.text,
                    fontFamily = SfProText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    letterSpacing = (-0.43).sp,
                    modifier = Modifier
                        .padding(
                            top = 5.dp,
                            start = 10.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 2.dp)
                        .align(
                            Alignment.BottomEnd
                        ),
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SfProText,
                        color = MineMessageTimeColor,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    if (message.status == "read") {
                        Icon(
                            painter = painterResource(R.drawable.ic_read_status),
                            contentDescription = null,
                            tint = MineMessageTimeColor,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_sent_status),
                            contentDescription = null,
                            tint = MineMessageTimeColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PenpalTextMessage(
    message: Message,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    BoxWithConstraints(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val maxBubbleWidth = maxWidth * 0.85f

        Box(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .widthIn(max = maxBubbleWidth)
                .clip(
                    shape = RoundedCornerShape(
                        bottomEnd = 16.dp,
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 2.dp,
                    )
                )
                .background(
                    color = Color.White,
                )
        ) {
            if (message.text.length <= 20) {
                Text(
                    text = message.text,
                    fontFamily = SfProText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(
                            top = 5.dp,
                            start = 10.dp,
                            end = 42.dp,
                            bottom = 5.dp
                        )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 3.dp)
                        .align(
                            Alignment.BottomEnd
                        ),
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SfProText,
                        color = PenpalMessageTimeColor,
                    )
                }
            } else {
                Text(
                    text = message.text,
                    fontFamily = SfProText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    letterSpacing = (-0.43).sp,
                    modifier = Modifier
                        .padding(
                            top = 5.dp,
                            start = 10.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 2.dp)
                        .align(
                            Alignment.BottomEnd
                        ),
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SfProText,
                        color = PenpalMessageTimeColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun NewChatWidget(
    onGreetingClick: () -> Unit,
    context: Context,
    gifImageLoader: ImageLoader,
) {

    val brush = Brush.horizontalGradient(
        colors = listOf(
            DateSeparatorGreen,
            DateSeparatorGreen,
            DateSeparatorGreen,
            Color(0xFF72A167),
            Color(0xFF7DB270),
            Color(0xFF80B672),
            Color(0xFF7DB270),
            DateSeparatorGreen,
        )
    )



    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    brush = brush,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    onGreetingClick()
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp,
                )
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = "Сообщений пока нет...",
                fontFamily = SfProText,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Отправьте сообещние или нажмите на приветсвие ниже.",
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(10.dp))
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.raw.duck_greeting_sticker)
                    .crossfade(true)
                    .build(),
                imageLoader = gifImageLoader,
                contentDescription = null,
                modifier = Modifier.size(200.dp),
            )
        }
    }
}

@Composable
private fun StickerWidget(
    context: Context,
    gifImageLoader: ImageLoader,
) {

    val list = listOf(
        R.raw.duck_greeting_sticker,
        R.raw.duck_crying_sticker,
        R.raw.duck_andry_sticker,
        R.raw.duck_puking_sticker,
    )

    var selectedSticker by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = LightGrayBackground,
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                vertical = 16.dp,
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ИЗБРАННЫЕ СТИКЕРЫ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = SfProText,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 90.dp),
                contentPadding = PaddingValues(8.dp),

                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = LightGrayBackground,
                    )
            ) {
                items(list.size) { index ->
                    val resourceId = list[index]

                    StickerItem(
                        iconPath = resourceId,
                        onStickerClick = { resId ->
                            selectedSticker = resId
                        },
                        context = context,
                        gifImageLoader = gifImageLoader,
                    )
                }
            }
        }
    }

}

@Composable
private fun StickerItem(
    iconPath: Int,
    onStickerClick: (String) -> Unit,
    context: Context,
    gifImageLoader: ImageLoader,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .clickable {
                onStickerClick(iconPath.toString())
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(iconPath)
                .crossfade(true)
                .build(),
            imageLoader = gifImageLoader,
            contentDescription = null,
            modifier = Modifier.size(200.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NewChatWidgetPreview() {

}
