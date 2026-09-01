package com.example.hydrogram.presentation.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.os.Build.VERSION.SDK_INT
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
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
import com.example.hydrogram.ui.theme.Separator
import com.example.hydrogram.ui.theme.SfProText
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.ImageLoader
import coil3.request.crossfade
import com.example.hydrogram.domain.model.ReplyData
import com.example.hydrogram.presentation.states.MineState
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.widgets.MessageActionMenu
import com.example.hydrogram.presentation.widgets.messages.image.MineImageMessage
import com.example.hydrogram.presentation.widgets.messages.image.MineReplyImageMessage
import com.example.hydrogram.presentation.widgets.messages.image.PenpalImageMessage
import com.example.hydrogram.presentation.widgets.messages.image.PenpalReplyImageMessage
import com.example.hydrogram.presentation.widgets.messages.sticker.MineStickerMessage
import com.example.hydrogram.presentation.widgets.messages.sticker.MineStickerReplyMessage
import com.example.hydrogram.presentation.widgets.messages.sticker.PenpalStickerMessage
import com.example.hydrogram.presentation.widgets.messages.sticker.PenpalStickerReplyMessage
import com.example.hydrogram.presentation.widgets.messages.text.MineReplyTextMessage
import com.example.hydrogram.presentation.widgets.messages.text.MineTextMessage
import com.example.hydrogram.presentation.widgets.messages.text.PenpalReplyTextMessage
import com.example.hydrogram.presentation.widgets.messages.text.PenpalTextMessage
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightGrayBackground
import kotlinx.coroutines.launch
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

    var contextMenuState by remember { mutableStateOf<ContextMenuState?>(null) }

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

    val coroutineScope = rememberCoroutineScope()

    val scrollToMessage = { targetReplyId: String ->
        var finalUIIndex = -1
        var currentUIIndex = 0

        for ((dayTimestamp, dayMessages) in groupedMessages) {

            if (finalUIIndex == -1) {
                currentUIIndex++
            }

            val indexInDay = dayMessages.indexOfFirst { it.messageId.toString() == targetReplyId }

            if (indexInDay != -1) {
                finalUIIndex = currentUIIndex + indexInDay
                break
            }

            currentUIIndex += dayMessages.size
        }

        Log.d(
            "ChatScroll",
            "ПРЯМОЙ ЧАТ | Ищем ID: $targetReplyId. Рассчитанный индекс UI: $finalUIIndex"
        )

        if (finalUIIndex != -1) {
            coroutineScope.launch {
                try {
                    kotlinx.coroutines.delay(100)

                    listState.animateScrollToItem(
                        index = finalUIIndex,
                        scrollOffset = -150
                    )
                    Log.d("ChatScroll", "Скролл на индекс $finalUIIndex выполнен")
                } catch (e: Exception) {
                    listState.scrollToItem(index = finalUIIndex, scrollOffset = -150)
                }
            }
        } else {
            android.widget.Toast.makeText(
                context,
                "Сообщение не найдено",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    val hazeState = remember { HazeState() }

    var firstUnreadMessageId by remember { mutableStateOf<String?>(null) }
    var hasInitializedUnreadId by remember { mutableStateOf(false) }

    var isStickerWidgetVisible by remember { mutableStateOf(false) }

    var currentMessageAnswer by remember { mutableStateOf<Message?>(null) }

    var isExpanded by remember { mutableStateOf(false) }


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
            if (currentMessageAnswer == null) {
                chatViewModel.sendImage(
                    senderId = mineId,
                    chatId = chatId,
                    imageUri = uri,
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

                val replyData = currentMessageAnswer?.replyData ?: ReplyData(
                    messageId = currentMessageAnswer!!.messageId,
                    senderId = currentMessageAnswer!!.senderId,
                    type = currentMessageAnswer!!.type,
                    content = content,
                )

                chatViewModel.sendImage(
                    senderId = mineId,
                    chatId = chatId,
                    imageUri = uri,
                    replyData = replyData,
                )
            }
        }
    }

    LaunchedEffect(currentMessageAnswer) {
        Log.d("ChatScreen", "selected message: ${currentMessageAnswer.toString()}")
        if (currentMessageAnswer != null) {
            isExpanded = true
        } else {
            isExpanded = false
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val lastMessage = messages.lastOrNull()
            if (lastMessage?.senderId == mineId) {
                kotlinx.coroutines.delay(50)

                val totalItems = listState.layoutInfo.totalItemsCount
                if (totalItems > 0) {
                    listState.animateScrollToItem(
                        index = totalItems - 1,
                        scrollOffset = 0
                    )
                }
            }
        }
    }

    val animatedBottomPadding by animateDpAsState(
        targetValue = if (isExpanded) 54.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
    )

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItemsCount = layoutInfo.totalItemsCount

            if (totalItemsCount == 0) true
            else {
                val lastVisibleItem = visibleItems.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index >= totalItemsCount - 2
            }
        }
    }

    val density = LocalDensity.current

    LaunchedEffect(isExpanded, messages.size) {
        if (isAtBottom && messages.isNotEmpty()) {
            kotlinx.coroutines.delay(100)

            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems > 0) {
                val offsetInPx = with(density) { 5.dp.roundToPx() }
                listState.animateScrollToItem(
                    index = totalItems - 1,
                    scrollOffset = -offsetInPx
                )
            }
        }
    }

    var currentReactingMessageId by remember { mutableStateOf<String?>(null) }

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
                bottom = 75.dp + animatedBottomPadding,
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

                    val messageCoordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }

                    if (message.messageId == firstUnreadMessageId) {
                        Spacer(modifier = Modifier.height(8.dp))
                        UnreadMessageSeparator()
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                messageCoordinates.value = coordinates
                            }
                    ) {

                        if (message.senderId == mineId) {
                            if (message.type == "text") {
                                if (message.replyData == null) {
                                    MineTextMessage(
                                        message = message as Message.Text,
                                        onReply = {
                                            currentMessageAnswer = it
                                            Log.d("ChatScreen", it.toString())
                                        },
                                        onDoubleClick = {
                                            Log.d(
                                                "ChatScreen",
                                                "chatId: $chatId, messageId: ${message.messageId}"
                                            )
                                            Log.d(
                                                "ChatScreen",
                                                "have mine Id: $it"
                                            )
                                            chatViewModel.toggleReaction(
                                                reaction = if (it) null else "\u2764\uFE0F",
                                                chatId = chatId,
                                                messageId = message.messageId,
                                            )
                                        },
                                        onLongClick = {
                                            val coordinates = messageCoordinates.value
                                            if (coordinates != null) {
                                                val positionInRoot = coordinates.positionInRoot()


                                                contextMenuState = ContextMenuState(
                                                    message = message,
                                                    position = IntOffset(
                                                        positionInRoot.x.toInt(),
                                                        positionInRoot.y.toInt()
                                                    ),
                                                    isMine = true,
                                                    size = coordinates.size
                                                )
                                                currentReactingMessageId = message.messageId
                                            }
                                        },
                                        onReactionClick = {
                                            chatViewModel.toggleReaction(
                                                reaction = null,
                                                chatId = chatId,
                                                messageId = message.messageId,
                                            )
                                        },
                                        mineId = mineId,
                                    )
                                } else {
                                    MineReplyTextMessage(
                                        message = message as Message.Text,
                                        replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
                                        onReply = {
                                            currentMessageAnswer = it
                                        },
                                        onReplyMessageClick = { messageId ->
                                            scrollToMessage(messageId)
                                        }
                                    )
                                }
                            } else if (message.type == "sticker") {
                                if (message.replyData == null) {
                                    MineStickerMessage(
                                        sticker = message as Message.Sticker,
                                        context = context,
                                        gifImageLoader = gifImageLoader,
                                        onReply = {
                                            currentMessageAnswer = it
                                        }
                                    )
                                } else {
                                    MineStickerReplyMessage(
                                        sticker = message as Message.Sticker,
                                        context = context,
                                        gifImageLoader = gifImageLoader,
                                        replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
                                        onReply = {
                                            val newReplyData = ReplyData(
                                                messageId = it.messageId,
                                                type = "sticker",
                                                senderId = it.replyData?.senderId ?: "",
                                                content = it.stickerPath ?: "",
                                            )

                                            currentMessageAnswer = Message.Sticker(
                                                messageId = it.replyData?.messageId ?: "",
                                                senderId = it.senderId,
                                                type = it.type,
                                                status = "sent",
                                                timestamp = System.currentTimeMillis(),
                                                replyData = newReplyData,
                                                stickerPath = it.stickerPath,
                                            )
                                        },
                                        onReplyMessageClick = { messageId ->
                                            scrollToMessage(messageId)
                                        }
                                    )
                                }
                            } else {
                                if (message.replyData == null) {
                                    MineImageMessage(
                                        message = message as Message.Image,
                                        onReply = {
                                            currentMessageAnswer = Message.Image(
                                                messageId = it.replyData?.messageId ?: "",
                                                senderId = it.replyData?.senderId ?: "",
                                                type = "image",
                                                status = "sent",
                                                timestamp = System.currentTimeMillis(),
                                                image = it.image,
                                            )
                                        }
                                    )
                                } else {
                                    MineReplyImageMessage(
                                        message = message as Message.Image,
                                        onReply = {
                                            val newReplyData = ReplyData(
                                                messageId = it.messageId,
                                                type = "image",
                                                senderId = it.replyData?.senderId ?: "",
                                                content = it.image ?: "",
                                            )

                                            currentMessageAnswer = Message.Image(
                                                messageId = it.replyData?.messageId ?: "",
                                                senderId = it.senderId,
                                                type = it.type,
                                                status = "sent",
                                                timestamp = System.currentTimeMillis(),
                                                replyData = newReplyData,
                                                image = it.image,
                                            )
                                        },
                                        replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
                                        onReplyMessageClick = { messageId ->
                                            scrollToMessage(messageId)
                                        }
                                    )
                                }
                            }
                        } else {
                            if (message.type == "text") {
                                if (message.replyData == null) {
                                    PenpalTextMessage(
                                        message = message as Message.Text,
                                        onReply = {
                                            currentMessageAnswer = message
                                            Log.d("ChatScreen", message.toString())
                                        }
                                    )
                                } else PenpalReplyTextMessage(
                                    message = message as Message.Text,
                                    replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
                                    onReply = {
                                        currentMessageAnswer = it
                                    },
                                    onReplyMessageClick = { messageId ->
                                        scrollToMessage(messageId)
                                    }
                                )
                            } else if (message.type == "sticker") {
                                if (message.replyData == null) {
                                    PenpalStickerMessage(
                                        sticker = message as Message.Sticker,
                                        context = context,
                                        gifImageLoader = gifImageLoader,
                                        onReply = {
                                            currentMessageAnswer = Message.Sticker(
                                                messageId = it.replyData?.messageId ?: "",
                                                senderId = it.replyData?.senderId ?: "",
                                                type = "sticker",
                                                status = "sent",
                                                timestamp = System.currentTimeMillis(),
                                                stickerPath = it.stickerPath,
                                            )
                                        }
                                    )
                                } else {
                                    PenpalStickerReplyMessage(
                                        sticker = message as Message.Sticker,
                                        context = context,
                                        gifImageLoader = gifImageLoader,
                                        replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
                                        onReply = {
                                            val newReplyData = ReplyData(
                                                messageId = it.messageId,
                                                type = "sticker",
                                                senderId = it.replyData?.senderId ?: "",
                                                content = it.stickerPath ?: "",
                                            )

                                            currentMessageAnswer = Message.Sticker(
                                                messageId = it.replyData?.messageId ?: "",
                                                senderId = it.senderId,
                                                type = it.type,
                                                status = "sent",
                                                timestamp = System.currentTimeMillis(),
                                                replyData = newReplyData,
                                                stickerPath = it.stickerPath,
                                            )
                                        },
                                        onReplyMessageClick = { messageId ->
                                            scrollToMessage(messageId)
                                        }
                                    )
                                }
                            } else {
                                if (message.replyData == null) {
                                    PenpalImageMessage(
                                        message = message as Message.Image,
                                        onReply = {
                                            currentMessageAnswer = Message.Image(
                                                messageId = it.replyData?.messageId ?: "",
                                                senderId = it.replyData?.senderId ?: "",
                                                type = "sticker",
                                                status = "sent",
                                                timestamp = System.currentTimeMillis(),
                                                image = it.image,
                                            )
                                        }
                                    )
                                } else {
                                    PenpalReplyImageMessage(
                                        message = message as Message.Image,
                                        onReply = {
                                            val newReplyData = ReplyData(
                                                messageId = it.messageId,
                                                type = "image",
                                                senderId = it.senderId,
                                                content = it.image ?: "",
                                            )

                                            currentMessageAnswer = Message.Image(
                                                messageId = it.replyData?.messageId ?: "",
                                                senderId = it.replyData?.senderId ?: "",
                                                type = it.type,
                                                status = "sent",
                                                timestamp = System.currentTimeMillis(),
                                                replyData = newReplyData,
                                                image = it.image,
                                            )
                                        },
                                        replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
                                        onReplyMessageClick = { messageId ->
                                            scrollToMessage(messageId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        contextMenuState?.let { state ->
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = state.position.x + state.size.width / 2,
                    y = state.position.y - 52,
                ),
                onDismissRequest = {
                    contextMenuState = null
                    currentReactingMessageId = null
                }
            ) {
                MessageActionMenu(
                    onReactionClick = { reaction ->
                        chatViewModel.toggleReaction(
                            reaction = reaction,
                            chatId = chatId,
                            messageId = currentReactingMessageId ?: "",
                        )
                        contextMenuState = null
                        currentReactingMessageId = null
                    }
                )
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
                                val replyData = ReplyData(
                                    messageId = currentMessageAnswer!!.messageId,
                                    senderId = currentMessageAnswer!!.senderId,
                                    type = currentMessageAnswer!!.type,
                                    content = content,
                                )
                                chatViewModel.sendText(
                                    senderId = mineId,
                                    chatId = chatId,
                                    text = messageText,
                                    replyData = replyData,
                                )
                            }
                            currentMessageAnswer = null
                        }
                    },
                    onAttachClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onStickerClick = {
                        isStickerWidgetVisible = !isStickerWidgetVisible
                    },
                    isExpanded = isExpanded,
                    replyMessage = currentMessageAnswer,
                    replyName = if (currentMessageAnswer?.senderId == mineId) mineName else penpalName,
                    onCancelClick = {
                        currentMessageAnswer = null
                        isExpanded = false
                    },
                    onReplyMessageClick = { messageId ->
                        scrollToMessage(messageId)
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
                        if (currentMessageAnswer == null) {
                            chatViewModel.sendSticker(
                                senderId = mineId,
                                chatId = chatId,
                                stickerPath = stickerString,
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

                            val replyData = currentMessageAnswer?.replyData ?: ReplyData(
                                messageId = currentMessageAnswer!!.messageId,
                                senderId = currentMessageAnswer!!.senderId,
                                type = currentMessageAnswer!!.type,
                                content = content,
                            )

                            chatViewModel.sendSticker(
                                senderId = mineId,
                                chatId = chatId,
                                stickerPath = stickerString,
                                replyData = replyData,
                            )
                        }
                        currentMessageAnswer = null
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
@Preview(showBackground = true)
private fun MineReplyTextMessagePreview() {

    val context = LocalContext.current


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

    val originalMessage: Message.Text = Message.Text(
        messageId = "-O123456789abcdef",
        senderId = "user_ivan",
        timestamp = 1718873400000L,
        text = "Как дела?",
    )

    val replyStickerMessage: Message.Sticker = Message.Sticker(
        messageId = "-O987654321fedcba",
        senderId = "my_current_user_id",
        timestamp = System.currentTimeMillis(),
        stickerPath = "",

        replyData = ReplyData(
            messageId = originalMessage.messageId,
            senderId = originalMessage.senderId,
            type = originalMessage.type,
            content = originalMessage.text
                ?: ""
        )
    )

    val replyTextMessage: Message.Text = Message.Text(
        messageId = "-O987654321fedcba",
        senderId = "my_current_user_id",
        timestamp = System.currentTimeMillis(),
        text = "fdgddkfgpesd",

        replyData = ReplyData(
            messageId = originalMessage.messageId,
            senderId = originalMessage.senderId,
            type = originalMessage.type,
            content = originalMessage.text
                ?: ""
        )
    )

    val replyImageMessage: Message.Image = Message.Image(
        messageId = "-O987654321fedcba",
        senderId = "my_current_user_id",
        timestamp = System.currentTimeMillis(),
        image = "",

        replyData = ReplyData(
            messageId = originalMessage.messageId,
            senderId = originalMessage.senderId,
            type = originalMessage.type,
            content = originalMessage.text
                ?: ""
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Image(
            painter = painterResource(R.drawable.light_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            MineStickerReplyMessage(
                sticker = replyStickerMessage,
                replyName = "Dmitry",
                onReply = {},
                context = context,
                gifImageLoader = gifImageLoader,
                onReplyMessageClick = {},
            )
            Spacer(modifier = Modifier.height(5.dp))
            PenpalReplyImageMessage(
                message = replyImageMessage,
                replyName = "Dmitry",
                onReply = {},
                onReplyMessageClick = {},
            )
            Spacer(modifier = Modifier.height(5.dp))
            MineReplyImageMessage(
                message = replyImageMessage,
                replyName = "Dmitry",
                onReply = {},
                onReplyMessageClick = {},
            )
        }
    }
}

fun decodeBase64Image(imageData: String?): Bitmap? {
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
fun PlaceholderContent() {
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

data class ContextMenuState(
    val message: Message,
    val position: IntOffset,
    val isMine: Boolean,
    val size: IntSize,
)

@Preview(showBackground = true)
@Composable
private fun NewChatWidgetPreview() {

}