package com.example.hydrogram.presentation.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.os.Build.VERSION.SDK_INT
import android.util.Base64
import android.widget.Space
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
import com.example.hydrogram.domain.model.ReplyData
import com.example.hydrogram.presentation.states.MineState
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.ui.theme.LightGrayBackground
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.text.startsWith
import kotlin.text.substringAfter


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

    LaunchedEffect(mineId) {
        if (mineId.isNotBlank()) {
            userViewModel.setTargetMineId(uid = mineId)
        }
    }

    val mineData by userViewModel.mineState.collectAsStateWithLifecycle()

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
                                penpalName = (penpalData as UserState.Success).user?.name ?: "",
                                mineName = (mineData as MineState.Success).user?.name ?: "",
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
    penpalName: String,
    mineName: String,
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


    val hazeState = remember { HazeState() }

    var firstUnreadMessageId by remember { mutableStateOf<String?>(null) }
    var hasInitializedUnreadId by remember { mutableStateOf(false) }

    var isStickerWidgetVisible by remember { mutableStateOf(false) }

    var currentMessageAnswer by remember { mutableStateOf<Message?>(null) }


    if (!hasInitializedUnreadId && messages.isNotEmpty()) {
        val firstUnread = messages
            .filter { it.senderId != mineId && it.status != "read" }
            .minByOrNull { it.timestamp }?.messageId

        firstUnreadMessageId = firstUnread
        hasInitializedUnreadId = true
    }

    LaunchedEffect(firstUnreadMessageId, messages.size, isStickerWidgetVisible) {
        val unreadId = firstUnreadMessageId

        if (unreadId != null) {
            // Сценарий А: Есть зафиксированное непрочитанное -> скроллим к нему
            val itemIndex = listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == unreadId }?.index

            if (itemIndex != null) {
                listState.scrollToItem(index = itemIndex, scrollOffset = 0)
            } else {
                var targetIndex = 0
                var found = false
                for ((_, dayMessages) in groupedMessages) {
                    if (found) break
                    targetIndex++ // Пропускаем плашку даты
                    for (msg in dayMessages) {
                        if (msg.messageId == unreadId) {
                            found = true; break
                        }
                        targetIndex++
                    }
                }
                if (found) listState.scrollToItem(index = targetIndex, scrollOffset = 0)
            }
        } else {
            // Сценарий Б: Чат полностью прочитан -> скроллим плавно в самый низ к инпут-бару
            if (messages.isNotEmpty()) {
                val totalItems = listState.layoutInfo.totalItemsCount
                if (totalItems > 0) {
                    // Анимация идет синхронно с ростом dynamicBottomPadding
                    listState.animateScrollToItem(index = totalItems - 1, scrollOffset = 0)
                }
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            chatViewModel.sendImage(
                senderId = mineId,
                chatId = chatId,
                imageUri = uri,
            )
        }
    }

    LaunchedEffect(currentMessageAnswer) {
        Log.d("ChatScreen", currentMessageAnswer.toString())
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
//                bottom = dynamicBottomPadding,
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
                        if (message.type == "text") {
                            if (message.replyData == null) {
                                MineTextMessage(
                                    message = message as Message.Text,
                                    onReply = {
                                        currentMessageAnswer = it
                                        Log.d("ChatScreen", it.toString())
                                    }
                                )
                            } else {
                                MineReplyTextMessage(
                                    message = message as Message.Text,
                                    replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
                                    onReply = {
                                        currentMessageAnswer = it
                                    }
                                )
                            }
                        } else if (message.type == "sticker") {
                            MineStickerMessage(
                                sticker = message as Message.Sticker,
                                context = context,
                                gifImageLoader = gifImageLoader,
                                onReply = {
                                    currentMessageAnswer = it
                                }
                            )
                        } else {
                            MineImageMessage(
                                message = message as Message.Image,
                                onReply = {
                                    currentMessageAnswer = it
                                }
                            )
                        }
                    } else {
                        if (message.type == "text") {
                            if (message.replyData == null) {
                                PenpalTextMessage(
                                    message = message as Message.Text,
                                    onReply = {
                                        currentMessageAnswer = message
                                        Log.d("ChatScreen", it.toString())
                                    }
                                )
                            } else PenpalReplyTextMessage(
                                message = message as Message.Text,
                                replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
                                onReply = {
                                    currentMessageAnswer = it
                                }
                            )
                        } else if (message.type == "sticker") {
                            PenpalStickerMessage(
                                sticker = message as Message.Sticker,
                                context = context,
                                gifImageLoader = gifImageLoader,
                                onReply = {
                                    currentMessageAnswer = it
                                }
                            )
                        } else {
                            PenpalImageMessage(
                                message = message as Message.Image,
                                onReply = {
                                    currentMessageAnswer = it
                                }
                            )
                        }
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
                            Log.d("ChatScreen", currentMessageAnswer.toString())
                            if (currentMessageAnswer == null) {
                                chatViewModel.sendText(
                                    senderId = mineId,
                                    chatId = chatId,
                                    text = messageText,
                                )
                            } else {
                                val content = when (currentMessageAnswer) {
                                    is Message.Text -> (currentMessageAnswer as Message.Text).text
                                        ?: ""

                                    is Message.Image -> (currentMessageAnswer as Message.Image).image
                                        ?: ""

                                    is Message.Sticker -> (currentMessageAnswer as Message.Sticker).stickerPath
                                        ?: ""

                                    else -> {
                                        ""
                                    }
                                }
                                chatViewModel.sendText(
                                    senderId = mineId,
                                    chatId = chatId,
                                    text = messageText,
                                    replyData = ReplyData(
                                        messageId = currentMessageAnswer!!.messageId,
                                        senderId = currentMessageAnswer!!.senderId,
                                        type = currentMessageAnswer!!.type,
                                        content = content,
                                    )
                                )
                            }
                        }
                    },
                    onAttachClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
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
                    onStickerClick = { stickerString ->
                        chatViewModel.sendSticker(
                            senderId = mineId,
                            chatId = chatId,
                            stickerPath = stickerString,
                        )
                    },
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
    message: Message.Text,
    onReply: (Message.Text) -> Unit,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    BoxWithConstraints(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            onReply(message)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            }
    ) {
        val maxBubbleWidth = maxWidth * 0.85f

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
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
            message.text?.length?.let {
                if (it <= 20) {
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
}

@Composable
@Preview(showBackground = true)
private fun MineReplyTextMessagePreview() {

    val originalMessage: Message.Text = Message.Text(
        messageId = "-O123456789abcdef",
        senderId = "user_ivan",
        timestamp = 1718873400000L,
        text = "Привет, как дела уеrgfdkgdkp23402-3059345-2034124324бок?"
    )

// 2. Создаем переменную текстового сообщения-ответа
    val replyTextMessage: Message.Text = Message.Text(
        messageId = "-O987654321fedcba", // Сгенерированный ID нового сообщения
        senderId = "my_current_user_id", // ID текущего пользователя, который пишет ответ
        timestamp = System.currentTimeMillis(), // Текущее время отправки
        text = "Всё отлично!?", // Текст вашего ответа

        // Заполняем данные о том, на что мы ответили
        replyData = ReplyData(
            messageId = originalMessage.messageId, // Ссылка на ID оригинала
            senderId = originalMessage.senderId,   // Кто отправил оригинал
            type = originalMessage.type,           // Тип оригинала ("text")
            content = originalMessage.text
                ?: ""   // Берутся текстовые данные из оригинала для превью
        )
    )

    Column {
        MineReplyTextMessage(
            message = replyTextMessage,
            replyName = "Ivan",
            onReply = {}
        )
        Spacer(modifier = Modifier.height(5.dp))
        PenpalReplyTextMessage(
            message = replyTextMessage,
            replyName = "Dmitry",
            onReply = {}
        )
    }
}

@Composable
private fun MineReplyTextMessage(
    message: Message.Text,
    onReply: (Message.Text) -> Unit,
    replyName: String,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    BoxWithConstraints(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            onReply(message)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            }
    ) {
        val maxBubbleWidth = maxWidth * 0.85f

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .heightIn(min = 73.dp)
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
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 120.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(
                            all = 9.dp,
                        )
                        .fillMaxWidth()
                        .height(41.dp)
                        .clip(
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(
                            color = Color(0xFFE2F7CA)
                        )
                        .padding(
                            end = 8.dp
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(41.dp)
                            .background(
                                color = Color(0xFF9EDB4E),
                            )
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = replyName,
                            fontFamily = SfProText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            letterSpacing = -(0.23).sp,
                            color = Color(0xFF9EDB4E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        message.replyData?.content.let {
                            if (it != null) {
                                Text(
                                    text = it,
                                    fontFamily = SfProText,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 15.sp,
                                    letterSpacing = -(0.23).sp,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = message.text ?: "",
                        fontFamily = SfProText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 65.dp, bottom = 5.dp)
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
                }

            }
        }
    }
}

@Composable
private fun PenpalReplyTextMessage(
    message: Message.Text,
    replyName: String,
    onReply: (Message.Text) -> Unit,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    BoxWithConstraints(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            onReply(message)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            }
    ) {
        val maxBubbleWidth = maxWidth * 0.85f

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .heightIn(min = 73.dp)
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
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 120.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(
                            all = 9.dp,
                        )
                        .fillMaxWidth()
                        .height(41.dp)
                        .clip(
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(
                            color = Color(0xFFFFEBD6)
                        )
                        .padding(
                            end = 8.dp
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(41.dp)
                            .background(
                                color = Color(0xFFFFBF7B),
                            )
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = replyName,
                            fontFamily = SfProText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            letterSpacing = -(0.23).sp,
                            color = Color(0xFFFFBF7B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        message.replyData?.content.let {
                            if (it != null) {
                                Text(
                                    text = it,
                                    fontFamily = SfProText,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 15.sp,
                                    letterSpacing = -(0.23).sp,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = message.text ?: "",
                        fontFamily = SfProText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 65.dp, bottom = 5.dp)
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
                            color = PenpalMessageTimeColor,
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun PenpalStickerMessage(
    sticker: Message.Sticker,
    context: Context,
    gifImageLoader: ImageLoader,
    onReply: (Message.Sticker) -> Unit,
) {

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            onReply(sticker)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .size(192.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("android.resource://${context.packageName}/${sticker.stickerPath}")
                    .crossfade(true)
                    .build(),
                imageLoader = gifImageLoader,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            StickerPenpalTime(
                time = sticker.timestamp,
                modifier = Modifier.padding(top = 12.dp, end = 6.dp)
            )
        }
    }
}


@Composable
private fun StickerPenpalTime(
    time: Long,
    modifier: Modifier = Modifier
) {
    val formattedTime = remember(time) {
        DateFormat.format("HH:mm", Date(time)).toString()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = formattedTime,
            fontFamily = SfProText,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = Color.White,
            letterSpacing = (-0.08).sp
        )
    }
}


@Composable
private fun MineStickerMessage(
    sticker: Message.Sticker,
    context: Context,
    gifImageLoader: ImageLoader,
    onReply: (Message.Sticker) -> Unit,
) {

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            onReply(sticker)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .size(192.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("android.resource://${context.packageName}/${sticker.stickerPath}")
                    .crossfade(true)
                    .build(),
                imageLoader = gifImageLoader,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            StickerMineTime(
                time = sticker.timestamp,
                status = sticker.status,
                modifier = Modifier.padding(top = 12.dp, end = 6.dp),
            )
        }
    }
}

@Composable
private fun StickerMineTime(
    time: Long,
    status: String,
    modifier: Modifier,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(time)
    ).toString()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = formattedTime,
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color.White,
                letterSpacing = (-0.08).sp,
            )
            Spacer(modifier = Modifier.width(5.dp))
            if (status == "read") {
                Icon(
                    painter = painterResource(R.drawable.ic_read_status),
                    contentDescription = null,
                    tint = Color.White,
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

@Composable
private fun PenpalTextMessage(
    message: Message.Text,
    onReply: (Message.Text) -> Unit,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    BoxWithConstraints(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            onReply(message)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            }
    ) {
        val maxBubbleWidth = maxWidth * 0.85f

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
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
            message.text?.length?.let {
                if (it <= 20) {
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
}

@Composable
private fun PenpalImageMessage(
    message: Message.Image,
    onReply: (Message.Image) -> Unit,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    val isBase64 = remember(message.image) {
        !message.image.isNullOrBlank() && message.image.startsWith("data:image/jpeg;base64,")
    }

    val imageSize = remember(message.image) {
        if (isBase64) {
            val bitmap = decodeBase64Image(message.image)
            if (bitmap != null) {
                bitmap.width to bitmap.height
            } else {
                null to null
            }
        } else {
            null to null
        }
    }

    val (imageWidth, imageHeight) = imageSize
    val maxWidth = 300.dp
    val maxHeight = 400.dp

    val containerModifier =
        if (imageWidth != null && imageHeight != null && imageWidth > 0 && imageHeight > 0) {
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

            val widthLimit = maxWidth.value
            val heightLimit = maxHeight.value

            var finalWidth = widthLimit
            var finalHeight = widthLimit / aspectRatio

            if (finalHeight > heightLimit) {
                finalHeight = heightLimit
                finalWidth = heightLimit * aspectRatio
            }

            Modifier
                .widthIn(max = maxWidth)
                .height(finalHeight.dp)
                .clickable { /* Открыть в полном размере */ }
        } else {
            Modifier
                .widthIn(max = maxWidth)
                .heightIn(max = maxHeight)
                .aspectRatio(1f)
                .clickable { /* Открыть в полном размере */ }
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            onReply(message)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            modifier = containerModifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { /* Открыть в полном размере */ }
            ) {
                if (isBase64) {
                    val bitmap = remember(message.image) {
                        decodeBase64Image(message.image)
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Изображение в чате",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        PlaceholderContent()
                    }
                } else {
                    AsyncImage(
                        model = message.image,
                        contentDescription = "Изображение в чате",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.ic_avatar),
                        error = painterResource(R.drawable.ic_avatar),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 7.dp, bottom = 7.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = formattedTime,
                        fontFamily = SfProText,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = Color.White,
                        letterSpacing = (-0.08).sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MineImageMessage(
    message: Message.Image,
    onReply: (Message.Image) -> Unit,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    var dragAmount by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (dragAmount == 0f) 0f else dragAmount,
        label = "SwipeOffset"
    )

    val isBase64 = remember(message.image) {
        !message.image.isNullOrBlank() && message.image.startsWith("data:image/jpeg;base64,")
    }

    val imageSize = remember(message.image) {
        if (isBase64) {
            val bitmap = decodeBase64Image(message.image)
            if (bitmap != null) {
                bitmap.width to bitmap.height
            } else {
                null to null
            }
        } else {
            null to null
        }
    }

    val (imageWidth, imageHeight) = imageSize

    // Получаем ширину экрана
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    // Максимальная ширина - 70% от экрана, но не больше 300dp
    val maxWidth = (screenWidthDp * 0.7f).coerceAtMost(300.dp)
    val maxHeight = 400.dp

    // Вычисляем размеры изображения
    val containerModifier =
        if (imageWidth != null && imageHeight != null && imageWidth > 0 && imageHeight > 0) {
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

            var finalWidth = maxWidth.value
            var finalHeight = maxWidth.value / aspectRatio

            if (finalHeight > maxHeight.value) {
                finalHeight = maxHeight.value
                finalWidth = maxHeight.value * aspectRatio
            }

            Modifier
                .width(finalWidth.dp)
                .height(finalHeight.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { /* Открыть в полном размере */ }
        } else {
            Modifier
                .width(maxWidth)
                .heightIn(max = maxHeight)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { /* Открыть в полном размере */ }
        }

    // Выравнивание по правому краю
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            onReply(message)
                        }
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onDragCancel = {
                        dragAmount = 0f
                        isHapticTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmountPx ->
                        change.consume()

                        val newOffset = (dragAmount + dragAmountPx).coerceIn(-200f, 0f)
                        dragAmount = newOffset

                        if (newOffset < -150f && !isHapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isHapticTriggered = true
                        } else if (newOffset > -150f && isHapticTriggered) {
                            isHapticTriggered = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        Card(
            modifier = containerModifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { /* Открыть в полном размере */ }
            ) {
                if (isBase64) {
                    val bitmap = remember(message.image) {
                        decodeBase64Image(message.image)
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Изображение в чате",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        PlaceholderContent()
                    }
                } else {
                    AsyncImage(
                        model = message.image,
                        contentDescription = "Изображение в чате",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.ic_avatar),
                        error = painterResource(R.drawable.ic_avatar),
                    )
                }

                // Время и статус - поверх изображения, справа внизу
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 7.dp, bottom = 7.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = formattedTime,
                            fontFamily = SfProText,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = Color.White,
                            letterSpacing = (-0.08).sp,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (message.status == "read") {
                            Icon(
                                painter = painterResource(R.drawable.ic_read_status),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_sent_status),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun decodeBase64Image(imageData: String?): Bitmap? {
    if (imageData.isNullOrBlank() || !imageData.startsWith("data:image/jpeg;base64,")) {
        return null
    }
    return try {
        val base64String = imageData.substringAfter("base64,")
        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
private fun PlaceholderContent() {
    Box(
        modifier = Modifier
            .size(104.dp)
            .background(Color.Gray)
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = "Ошибка загрузки",
            tint = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
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
    onStickerClick: (String) -> Unit,
) {

    val list = listOf(
        R.raw.duck_greeting_sticker,
        R.raw.duck_crying_sticker,
        R.raw.duck_andry_sticker,
        R.raw.duck_puking_sticker,
    )

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
                        onStickerClick = { stickerString ->
                            onStickerClick(stickerString)
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
                Log.d("ChatScreen", "stickerPath: $iconPath")
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
