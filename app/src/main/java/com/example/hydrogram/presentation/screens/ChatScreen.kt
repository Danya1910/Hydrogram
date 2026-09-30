package com.example.hydrogram.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.model.ReplyData
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.ChatUiState
import com.example.hydrogram.presentation.states.MineState
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.util.CameraHolder
import com.example.hydrogram.presentation.util.CopyTextToClipboard
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.util.MessageCallbacks
import com.example.hydrogram.presentation.util.MessageData
import com.example.hydrogram.presentation.util.formatHeaderDate
import com.example.hydrogram.presentation.util.generateChatId
import com.example.hydrogram.presentation.util.getStartOfDay
import com.example.hydrogram.presentation.viewModel.ChatViewModel
import com.example.hydrogram.presentation.viewModel.UserViewModel
import com.example.hydrogram.presentation.widgets.ChatInputField
import com.example.hydrogram.presentation.widgets.MessageActionMenu
import com.example.hydrogram.presentation.widgets.TopChatBar
import com.example.hydrogram.presentation.widgets.VideoMessageRecorder
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
import com.example.hydrogram.presentation.widgets.messages.video.CircleVideoMessage
import com.example.hydrogram.presentation.widgets.messages.voice.VoiceReplyWidget
import com.example.hydrogram.presentation.widgets.messages.voice.VoiceWidget
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.DateSeparatorGreen
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.Separator
import com.example.hydrogram.ui.theme.SfProText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


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

    val context = LocalContext.current
    val cameraHolder = remember { CameraHolder(context) }

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

    DisposableEffect(Unit) {
        onDispose { cameraHolder.release() }
    }

    val mineData by userViewModel.mineState.collectAsStateWithLifecycle()
    val penpalData by userViewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        chatViewModel.getCurrentUserId()
    }

    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            chatViewModel.observeChatHistory(chatId = chatId)
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
                            style = HazeDefaults.style(
                                backgroundColor = Color.White.copy(alpha = 0.01f),
                                blurRadius = 2.dp
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
                        .padding(bottom = 5.dp)
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
                                CircularProgressIndicator(color = Blue)
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
                            val mineUser = (mineData as? MineState.Success)?.user
                            val penpalUser = (penpalData as? UserState.Success)?.user

                            if (penpalUser != null) {
                                Content(
                                    messages = messages,
                                    chatViewModel = chatViewModel,
                                    cameraHolder = cameraHolder,
                                    bottomPadding = paddingValues.calculateBottomPadding(),
                                    mineId = mineId,
                                    chatId = chatId,
                                    penpalName = penpalUser.name ?: "",
                                    mineName = mineUser?.name ?: "",
                                    mineData = mineUser,
                                    penpalData = penpalUser,
                                    hazeState = hazeState,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class, ExperimentalHazeMaterialsApi::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun Content(
    messages: List<Message>,
    chatViewModel: ChatViewModel,
    cameraHolder: CameraHolder,
    bottomPadding: Dp,
    mineId: String,
    chatId: String,
    penpalName: String,
    mineName: String,
    mineData: User?,
    penpalData: User?,
    hazeState: HazeState,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current

    var contextMenuState by remember { mutableStateOf<ContextMenuState?>(null) }
    var textState by remember { mutableStateOf("") }
    var isVideoRecording by remember { mutableStateOf(false) }
    var isCancelVideo by remember { mutableStateOf(false) }
    var isVideoButton by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isStickerWidgetVisible by remember { mutableStateOf(false) }
    var currentMessageAnswer by remember { mutableStateOf<Message?>(null) }
    var currentEditingMessage by remember { mutableStateOf<Message?>(null) }
    var currentReactingMessage by remember { mutableStateOf<Message?>(null) }
    var currentPlayingMessageId by remember { mutableStateOf("") }
    var circleVideoDuration by remember { mutableStateOf(0L) }
    var firstUnreadMessageId by remember { mutableStateOf<String?>(null) }
    var hasInitializedUnreadId by remember { mutableStateOf(false) }


    val isExpanded = currentMessageAnswer != null

    val mediaPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    LaunchedEffect(mediaPermissionsState.allPermissionsGranted, isVideoRecording) {
        if (!mediaPermissionsState.allPermissionsGranted && isVideoRecording) {
            mediaPermissionsState.launchMultiplePermissionRequest()
        }
    }

    val player = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    val gifImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(AnimatedImageDecoder.Factory())
            }
            .build()
    }

    val groupedMessages = remember(messages) {
        messages.groupBy { getStartOfDay(it.timestamp) }
    }
    val messagesById = remember(messages) { messages.associateBy { it.messageId } }

    val messageIndices = remember(groupedMessages) {
        val map = HashMap<String, Int>(messagesById.size)
        var currentIndex = 0
        groupedMessages.forEach { (_, dayMessages) ->
            currentIndex++
            dayMessages.forEach { message ->
                map[message.messageId] = currentIndex
                currentIndex++
            }
        }
        map
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isChatReady by remember {
        derivedStateOf {
            listState.layoutInfo.totalItemsCount > 0 && !listState.isScrollInProgress
        }
    }

    if (!hasInitializedUnreadId && messages.isNotEmpty()) {
        firstUnreadMessageId = messages
            .asSequence()
            .filter { it.senderId != mineId && it.status != "read" }
            .minByOrNull { it.timestamp }
            ?.messageId
        hasInitializedUnreadId = true
    }

    LaunchedEffect(chatId) {
        val unreadId = firstUnreadMessageId
        if (unreadId != null) {
            val target = messageIndices[unreadId]
            if (target != null) {
                val fastJump = (target - 10).coerceAtLeast(0)
                listState.scrollToItem(fastJump)
                listState.animateScrollToItem(target)
            }
        } else if (messages.isNotEmpty()) {
            val total = listState.layoutInfo.totalItemsCount
            if (total > 0) {
                val intermediate = (total - 20).coerceAtLeast(0)
                listState.scrollToItem(intermediate)
                listState.animateScrollToItem(total - 1)
            }
        }
    }

    val scrollToMessage: (String) -> Unit = remember(messageIndices) {
        { targetReplyId ->
            val index = messageIndices[targetReplyId]
            if (index != null) {
                coroutineScope.launch {
                    listState.scrollToItem(index = index, scrollOffset = -150)
                }
            } else {
                Toast.makeText(context, "Сообщение не найдено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val visibleMessageIds by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
                .asSequence()
                .mapNotNull { it.key as? String }
                .filterNot { it.startsWith("date_") }
                .toList()
        }
    }

    LaunchedEffect(isChatReady, isVideoRecording) {
        if (!isChatReady && !isVideoRecording) return@LaunchedEffect
        if (cameraHolder.isReady) return@LaunchedEffect    // ← читаем из холдера

        val hasCamera = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCamera) return@LaunchedEffect

        if (!isVideoRecording) delay(1500)

        cameraHolder.warmUp(
            lifecycleOwner = lifecycleOwner,
            onReady = { /* ничего, isReady уже обновился внутри */ },
            onError = { Log.e("ChatScreen", "Camera warmUp failed", it) },
        )
    }

    LaunchedEffect(listState, messagesById, mineId, chatId) {
        snapshotFlow { visibleMessageIds }
            .collect { ids ->
                ids.forEach { id ->
                    val msg = messagesById[id] ?: return@forEach
                    if (msg.senderId != mineId && msg.status != "read") {
                        chatViewModel.changeMessageStatus(
                            chatId = chatId,
                            messageId = msg.messageId,
                            status = "read",
                        )
                    }
                }
            }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && messages.lastOrNull()?.senderId == mineId) {
            delay(50)
            val total = listState.layoutInfo.totalItemsCount
            if (total > 0) {
                listState.animateScrollToItem(total - 1)
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        if (currentEditingMessage != null) {
            val imageData = try {
                convertImageToOptimizedBase64(context, uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            chatViewModel.changeMessage(
                chatId = chatId,
                messageId = currentEditingMessage?.messageId ?: "",
                currentMessageType = currentEditingMessage?.type ?: "",
                typeOfChange = "image",
                change = imageData
            )
            currentEditingMessage = null
            return@rememberLauncherForActivityResult
        }

        val reply = currentMessageAnswer
        if (reply == null) {
            chatViewModel.sendImage(
                senderId = mineId,
                chatId = chatId,
                imageUri = uri,
                targetUserId = penpalData?.uid ?: "",
                senderName = mineName,
                senderAvatar = mineData?.avatarUrl ?: "",
            )
        } else {
            chatViewModel.sendImage(
                senderId = mineId,
                chatId = chatId,
                imageUri = uri,
                replyData = reply.toReplyData(),
                targetUserId = penpalData?.uid ?: "",
                senderName = mineName,
                senderAvatar = mineData?.avatarUrl ?: "",
            )
        }
    }

    val animatedBottomPadding by animateDpAsState(
        targetValue = if (isExpanded) 54.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "bottomPadding"
    )
    val bottomBarExtraPadding by animateDpAsState(
        targetValue = if (isRecording || isVideoRecording) 17.dp else 4.dp,
        animationSpec = tween(durationMillis = 200),
        label = "bottomBarPadding"
    )

    val totalBottomPaddingPx = remember(animatedBottomPadding, bottomBarExtraPadding) {
        with(density) { (47.dp + animatedBottomPadding + bottomBarExtraPadding).roundToPx() }
    }

    val isAtBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            if (info.totalItemsCount == 0) true
            else {
                val last = info.visibleItemsInfo.lastOrNull()
                last != null && last.index >= info.totalItemsCount - 2
            }
        }
    }

    val thresholdPx = with(density) { 100.dp.toPx() }
    val isScrollToBottomVisible by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            if (last.index != info.totalItemsCount - 1) return@derivedStateOf false
            val bottom = last.offset + last.size
            (bottom - info.viewportEndOffset) <= thresholdPx
        }
    }

    LaunchedEffect(isExpanded, messages.size) {
        if (isAtBottom && messages.isNotEmpty()) {
            delay(100)
            val total = listState.layoutInfo.totalItemsCount
            if (total > 0) {
                val offset = with(density) { 5.dp.roundToPx() }
                listState.animateScrollToItem(total - 1, -offset)
            }
        }
    }

    LaunchedEffect(isVideoRecording) {
        if (!isVideoRecording) circleVideoDuration = 0L
    }


    Box(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.fillMaxSize()) {
            if (messages.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    NewChatWidget(
                        onGreetingClick = {
                            chatViewModel.sendSticker(
                                senderId = mineId,
                                chatId = chatId,
                                stickerPath = R.raw.duck_greeting_sticker.toString(),
                                targetUserId = penpalData?.uid ?: "",
                                senderName = mineName,
                                senderAvatar = mineData?.avatarUrl ?: "",
                            )
                        },
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
                    top = 96.dp,
                    bottom = 47.dp + animatedBottomPadding + bottomBarExtraPadding,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .haze(hazeState)
                    .navigationBarsPadding()
                    .clickable(
                        enabled = isStickerWidgetVisible,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isStickerWidgetVisible) isStickerWidgetVisible = false
                    },
            ) {
                groupedMessages.forEach { (dayTimestamp, dayMessages) ->
                    item(key = "date_$dayTimestamp") {
                        DateSeparator(text = formatHeaderDate(dayTimestamp))
                    }

                    items(
                        items = dayMessages,
                        key = { it.messageId },
                        contentType = { it.type }
                    ) { message ->
                        Column {
                            if (message.messageId == firstUnreadMessageId) {
                                Spacer(Modifier.height(8.dp))
                                UnreadMessageSeparator()
                                Spacer(Modifier.height(8.dp))
                            }

                            MessageItem(
                                message = message,
                                isMine = message.senderId == mineId,
                                mineId = mineId,
                                mineName = mineName,
                                penpalName = penpalName,
                                mineData = mineData,
                                penpalData = penpalData,
                                chatId = chatId,
                                chatViewModel = chatViewModel,
                                context = context,
                                gifImageLoader = gifImageLoader,
                                player = player,
                                listState = listState,
                                totalBottomPaddingPx = totalBottomPaddingPx,
                                globalIndex = messageIndices[message.messageId],
                                currentPlayingMessageId = currentPlayingMessageId,
                                onSetCurrentVideo = { currentPlayingMessageId = it },
                                onAnswer = { currentMessageAnswer = it },
                                onLongClick = { msg, pos, size ->
                                    contextMenuState = ContextMenuState(msg, pos, true, size)
                                    currentReactingMessage = msg
                                },
                                onScrollToMessage = scrollToMessage,
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
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
                    currentReactingMessage = null
                    currentEditingMessage = null
                }
            ) {
                MessageActionMenu(
                    onReactionClick = { reaction ->
                        chatViewModel.toggleReaction(
                            reaction = reaction,
                            chatId = chatId,
                            messageId = currentReactingMessage?.messageId ?: "",
                        )
                        contextMenuState = null
                        currentReactingMessage = null
                    },
                    onCopyClick = if (currentReactingMessage?.type == "text") {
                        {
                            CopyTextToClipboard(
                                context = context,
                                text = (currentReactingMessage as Message.Text).text ?: "",
                            )
                            contextMenuState = null
                            currentReactingMessage = null
                        }
                    } else null,
                    onAnswerClick = {
                        currentMessageAnswer = currentReactingMessage
                        contextMenuState = null
                    },
                    onDeleteClick = {
                        chatViewModel.deleteMessage(
                            chatId = chatId,
                            messageId = currentReactingMessage?.messageId ?: "",
                        )
                        contextMenuState = null
                        currentReactingMessage = null
                    },
                    onEditClick = if (
                        currentReactingMessage?.type == "text" &&
                        currentReactingMessage?.senderId == mineId
                    ) {
                        {
                            currentEditingMessage = currentReactingMessage
                            textState = (currentEditingMessage as Message.Text).text ?: ""
                            contextMenuState = null
                            currentReactingMessage = null
                            currentMessageAnswer = null
                        }
                    } else null
                )
            }
        }

        // PreviewView ВСЕГДА в дереве — критично для surface
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val isVideoUIOpen = isVideoRecording && !isCancelVideo
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { cameraHolder.previewView },
                modifier = if (isVideoUIOpen) {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(horizontal = 16.dp)
                        .clip(CircleShape)
                } else {
                    Modifier
                        .size(1.dp)
                        .alpha(0f)
                }
            )
        }

        if (isVideoRecording && !isCancelVideo) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                val vc = cameraHolder.videoCapture
                if (cameraHolder.isReady && vc != null) {
                    VideoMessageRecorder(
                        videoCapture = vc,
                        isRecordingTriggered = isVideoRecording,
                        isCanceled = isCancelVideo,
                        onVideoRecorded = { file, duration ->
                            chatViewModel.sendCircleVideo(
                                senderId = mineId,
                                chatId = chatId,
                                video = file,
                                videoDuration = (duration / 1000).toInt(),
                                replyData = null,
                                targetUserId = penpalData?.uid ?: "",
                                senderName = mineName,
                                senderAvatar = mineData?.avatarUrl ?: "",
                            )
                        },
                        currentDuration = { duration -> circleVideoDuration = duration },
                    )
                } else {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .graphicsLayer(clip = false)
        ) {
            AnimatedVisibility(
                visible = isStickerWidgetVisible,
                enter = slideInVertically(
                    animationSpec = spring(stiffness = 400f),
                    initialOffsetY = { it }
                ) + fadeIn(tween(200)),
                exit = slideOutVertically(
                    animationSpec = spring(stiffness = 400f),
                    targetOffsetY = { it }
                ) + fadeOut(tween(200)),
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxSize()
                ) {
                    StickerWidget(
                        context = context,
                        gifImageLoader = gifImageLoader,
                        onStickerClick = { stickerString ->
                            currentEditingMessage?.let { editing ->
                                chatViewModel.changeMessage(
                                    chatId = chatId,
                                    messageId = editing.messageId,
                                    currentMessageType = editing.type,
                                    typeOfChange = "sticker",
                                    change = stickerString
                                )
                                currentEditingMessage = null
                                isStickerWidgetVisible = false
                                return@StickerWidget
                            }
                            chatViewModel.sendSticker(
                                senderId = mineId,
                                chatId = chatId,
                                stickerPath = stickerString,
                                replyData = currentMessageAnswer?.toReplyData(),
                                targetUserId = penpalData?.uid ?: "",
                                senderName = mineName,
                                senderAvatar = mineData?.avatarUrl ?: "",
                            )
                            isStickerWidgetVisible = false
                            currentMessageAnswer = null
                        },
                    )
                }
            }
            AnimatedVisibility(
                visible = !isScrollToBottomVisible,
                enter = fadeIn(tween(200)) + scaleIn(
                    initialScale = 0.5f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut(tween(150)) + scaleOut(
                    targetScale = 0.5f,
                    animationSpec = tween(150)
                ),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .graphicsLayer(clip = false)
            ) {
                ScrollToBottomButton(
                    onScrollToBottomClick = {
                        coroutineScope.launch {
                            val total = listState.layoutInfo.totalItemsCount
                            if (total > 0) listState.animateScrollToItem(total - 1)
                        }
                    }
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false)
                    .hazeChild(
                        state = hazeState,
                        style = HazeDefaults.style(
                            backgroundColor = Color.White.copy(alpha = 0.01f),
                            blurRadius = 2.dp
                        )
                    )
                    .padding(top = if (isRecording) bottomBarExtraPadding else 0.dp)
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
                    onValueChange = { textState = it },
                    onSendClick = {
                        // Edit
                        currentEditingMessage?.let { editing ->
                            chatViewModel.changeMessage(
                                chatId = chatId,
                                messageId = editing.messageId,
                                currentMessageType = editing.type,
                                typeOfChange = "text",
                                change = textState.trim()
                            )
                            currentEditingMessage = null
                            textState = ""
                            return@ChatInputField
                        }
                        // Send
                        if (textState.isNotBlank()) {
                            val text = textState
                            textState = ""
                            val reply = currentMessageAnswer
                            chatViewModel.sendText(
                                senderId = mineId,
                                chatId = chatId,
                                text = text,
                                replyData = reply?.toReplyData(),
                                targetUserId = penpalData?.uid ?: "",
                                senderName = mineName,
                                senderAvatar = mineData?.avatarUrl ?: "",
                            )
                            currentMessageAnswer = null
                        }
                    },
                    onAttachClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onStickerClick = { isStickerWidgetVisible = !isStickerWidgetVisible },
                    isExpanded = isExpanded,
                    isEditing = currentEditingMessage != null,
                    replyMessage = currentMessageAnswer,
                    replyName = if (currentMessageAnswer?.senderId == mineId) mineName else penpalName,
                    onCancelClick = { currentMessageAnswer = null },
                    onReplyMessageClick = scrollToMessage,
                    editingMessage = currentEditingMessage,
                    onCancelEditClick = { currentEditingMessage = null },
                    isRecording = isRecording,
                    isVideoRecording = isVideoRecording,
                    changeRecordState = { isRecording = it },
                    onRecordStart = {
                        if (mediaPermissionsState.allPermissionsGranted) {
                            chatViewModel.startRecording()
                        } else {
                            mediaPermissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    onRecordStop = {
                        val reply = currentMessageAnswer
                        chatViewModel.stopAndSendRecording(
                            senderId = mineId,
                            chatId = chatId,
                            replyData = reply?.toReplyData(),
                            targetUserId = penpalData?.uid ?: "",
                            senderName = mineName,
                            senderAvatar = mineData?.avatarUrl ?: "",
                        )
                        currentMessageAnswer = null
                    },
                    onRecordCancel = { chatViewModel.cancelRecording() },
                    videoRecordingToggle = { isVideoNow ->
                        isCancelVideo = false
                        isVideoRecording = isVideoNow
                    },
                    cancelVideo = {
                        isCancelVideo = true
                        isVideoRecording = false
                    },
                    isVideoButton = isVideoButton,
                    changeButton = { isVideoButton = !isVideoButton },
                    circleVideoDuration = circleVideoDuration,
                )
            }
        }

    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun MessageItem(
    message: Message,
    isMine: Boolean,
    mineId: String,
    mineName: String,
    penpalName: String,
    mineData: User?,
    penpalData: User?,
    chatId: String,
    chatViewModel: ChatViewModel,
    context: Context,
    gifImageLoader: ImageLoader,
    player: ExoPlayer,
    listState: LazyListState,
    totalBottomPaddingPx: Int,
    globalIndex: Int?,
    currentPlayingMessageId: String,
    onSetCurrentVideo: (String) -> Unit,
    onAnswer: (Message) -> Unit,
    onLongClick: (Message, IntOffset, IntSize) -> Unit,
    onScrollToMessage: (String) -> Unit,
) {
    var coordinates: LayoutCoordinates? = null
    val boxModifier = Modifier.onGloballyPositioned { coordinates = it }

    val messageData = remember(message.messageId, mineId, penpalName, mineName) {
        MessageData(
            replyName = if (message.replyData?.senderId == mineId) mineName else penpalName,
            mineId = mineId,
            mineAvatar = mineData?.avatarUrl ?: "",
            penpalAvatar = penpalData?.avatarUrl ?: "",
        )
    }

    val callbacks = remember(message.messageId, chatId) {
        MessageCallbacks(
            onReply = { onAnswer(it) },
            onDoubleClick = { hasReaction ->
                chatViewModel.toggleReaction(
                    reaction = if (hasReaction) null else "\u2764\uFE0F",
                    chatId = chatId,
                    messageId = message.messageId,
                )
            },
            onLongClick = {
                val coords = coordinates
                if (coords != null) {
                    val pos = coords.positionInRoot()
                    onLongClick(
                        message,
                        IntOffset(pos.x.toInt(), pos.y.toInt()),
                        coords.size
                    )
                }
            },
            onReactionClick = {
                val mineReaction = message.reactions?.get(mineId)
                chatViewModel.toggleReaction(
                    reaction = if (mineReaction == null) {
                        message.reactions?.get(penpalData?.uid)
                    } else null,
                    chatId = chatId,
                    messageId = message.messageId,
                )
            },
            onReplyMessageClick = { messageId -> onScrollToMessage(messageId) },
        )
    }

    Box(modifier = boxModifier) {
        when {
            isMine -> when (message.type) {
                "text" -> {
                    val text = message as Message.Text
                    if (text.replyData == null) {
                        MineTextMessage(
                            message = text,
                            messageCallbacks = callbacks,
                            messageData = messageData,
                        )
                    } else {
                        MineReplyTextMessage(
                            message = text,
                            messageCallbacks = callbacks,
                            messageData = messageData,
                        )
                    }
                }

                "sticker" -> {
                    val sticker = message as Message.Sticker
                    if (sticker.replyData == null) {
                        MineStickerMessage(
                            sticker = sticker,
                            context = context,
                            gifImageLoader = gifImageLoader,
                            onReply = { onAnswer(it) },
                            onDoubleClick = callbacks.onDoubleClick,
                            onLongClick = callbacks.onLongClick,
                            onReactionClick = callbacks.onReactionClick,
                            mineId = mineId,
                            mineAvatar = messageData.mineAvatar,
                            penpalAvatar = messageData.penpalAvatar,
                        )
                    } else {
                        MineStickerReplyMessage(
                            sticker = sticker,
                            context = context,
                            gifImageLoader = gifImageLoader,
                            replyName = messageData.replyName,
                            onReply = { onAnswer(it) },
                            onReplyMessageClick = { onScrollToMessage(it) },
                            onDoubleClick = callbacks.onDoubleClick,
                            onLongClick = callbacks.onLongClick,
                            onReactionClick = callbacks.onReactionClick,
                            mineId = mineId,
                            mineAvatar = messageData.mineAvatar,
                            penpalAvatar = messageData.penpalAvatar,
                        )
                    }
                }

                "voice" -> {
                    if (message.replyData == null) {
                        VoiceWidget(
                            audioPlayer = player,
                            message = message,
                            isMine = true,
                            messageCallbacks = callbacks,
                            messageData = messageData,
                            setCurrentVideo = onSetCurrentVideo,
                        )
                    } else {
                        VoiceReplyWidget(
                            audioPlayer = player,
                            message = message,
                            isMine = true,
                            context = context,
                            messageCallbacks = callbacks,
                            messageData = messageData,
                        )
                    }
                }

                "circleVideo" -> {
                    CircleVideoMessage(
                        videoPlayer = player,
                        isMine = true,
                        message = message,
                        globalIndex = globalIndex,
                        lazyListState = listState,
                        bottomPaddingPx = totalBottomPaddingPx,
                        setCurrentVideo = onSetCurrentVideo,
                        messageData = messageData,
                        messageCallbacks = callbacks,
                        currentVideoId = currentPlayingMessageId,
                    )
                }

                else -> {
                    val image = message as Message.Image
                    if (image.replyData == null) {
                        MineImageMessage(
                            message = image,
                            onReply = { onAnswer(it) },
                            onDoubleClick = callbacks.onDoubleClick,
                            onLongClick = callbacks.onLongClick,
                            onReactionClick = callbacks.onReactionClick,
                            mineId = mineId,
                            mineAvatar = messageData.mineAvatar,
                            penpalAvatar = messageData.penpalAvatar,
                        )
                    } else {
                        MineReplyImageMessage(
                            message = image,
                            onReply = { onAnswer(it) },
                            replyName = messageData.replyName,
                            onReplyMessageClick = { onScrollToMessage(it) },
                            onDoubleClick = callbacks.onDoubleClick,
                            onLongClick = callbacks.onLongClick,
                            onReactionClick = callbacks.onReactionClick,
                            mineId = mineId,
                            mineAvatar = messageData.mineAvatar,
                            penpalAvatar = messageData.penpalAvatar,
                        )
                    }
                }
            }

            else -> when (message.type) {
                "text" -> {
                    val text = message as Message.Text
                    if (text.replyData == null) {
                        PenpalTextMessage(
                            message = text,
                            messageCallbacks = callbacks,
                            messageData = messageData,
                        )
                    } else {
                        PenpalReplyTextMessage(
                            message = text,
                            messageCallbacks = callbacks,
                            messageData = messageData,
                        )
                    }
                }

                "sticker" -> {
                    val sticker = message as Message.Sticker
                    if (sticker.replyData == null) {
                        PenpalStickerMessage(
                            sticker = sticker,
                            context = context,
                            gifImageLoader = gifImageLoader,
                            onReply = { onAnswer(it) },
                            onDoubleClick = callbacks.onDoubleClick,
                            onLongClick = callbacks.onLongClick,
                            onReactionClick = callbacks.onReactionClick,
                            mineId = mineId,
                            mineAvatar = messageData.mineAvatar,
                            penpalAvatar = messageData.penpalAvatar,
                        )
                    } else {
                        PenpalStickerReplyMessage(
                            sticker = sticker,
                            context = context,
                            gifImageLoader = gifImageLoader,
                            replyName = messageData.replyName,
                            onReply = { onAnswer(it) },
                            onReplyMessageClick = { onScrollToMessage(it) },
                            onDoubleClick = callbacks.onDoubleClick,
                            onLongClick = callbacks.onLongClick,
                            onReactionClick = callbacks.onReactionClick,
                            mineId = mineId,
                            mineAvatar = messageData.mineAvatar,
                            penpalAvatar = messageData.penpalAvatar,
                        )
                    }
                }

                "voice" -> {
                    if (message.replyData == null) {
                        VoiceWidget(
                            audioPlayer = player,
                            message = message,
                            isMine = false,
                            messageCallbacks = callbacks,
                            messageData = messageData,
                            setCurrentVideo = onSetCurrentVideo,
                        )
                    } else {
                        VoiceReplyWidget(
                            audioPlayer = player,
                            message = message,
                            isMine = false,
                            context = context,
                            messageCallbacks = callbacks,
                            messageData = messageData,
                        )
                    }
                }

                "circleVideo" -> {
                    CircleVideoMessage(
                        videoPlayer = player,
                        isMine = false,
                        message = message,
                        globalIndex = globalIndex,
                        lazyListState = listState,
                        bottomPaddingPx = totalBottomPaddingPx,
                        setCurrentVideo = onSetCurrentVideo,
                        messageData = messageData,
                        messageCallbacks = callbacks,
                        currentVideoId = currentPlayingMessageId,
                    )
                }

                else -> {
                    val image = message as Message.Image
                    if (image.replyData == null) {
                        PenpalImageMessage(
                            message = image,
                            onReply = { onAnswer(it) },
                            onDoubleClick = callbacks.onDoubleClick,
                            onLongClick = callbacks.onLongClick,
                            onReactionClick = callbacks.onReactionClick,
                            mineId = mineId,
                            mineAvatar = messageData.mineAvatar,
                            penpalAvatar = messageData.penpalAvatar,
                        )
                    } else {
                        PenpalReplyImageMessage(
                            message = image,
                            onReply = { onAnswer(it) },
                            replyName = messageData.replyName,
                            onReplyMessageClick = { onScrollToMessage(it) },
                            onDoubleClick = callbacks.onDoubleClick,
                            onLongClick = callbacks.onLongClick,
                            onReactionClick = callbacks.onReactionClick,
                            mineId = mineId,
                            mineAvatar = messageData.mineAvatar,
                            penpalAvatar = messageData.penpalAvatar,
                        )
                    }
                }
            }
        }
    }
}


private fun Message.toReplyData(): ReplyData {
    val content = when (this) {
        is Message.Text -> text ?: ""
        is Message.Image -> image ?: ""
        is Message.Sticker -> stickerPath ?: ""
        is Message.Voice -> audioUrl ?: ""
        else -> ""
    }
    return ReplyData(
        messageId = messageId,
        senderId = senderId,
        type = type,
        content = content,
    )
}


@Composable
private fun UnreadMessageSeparator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(24.dp)
            .fillMaxWidth()
            .background(color = Separator.copy(alpha = 0.8f))
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
private fun DateSeparator(text: String) {
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

fun decodeBase64Image(imageData: String?): Bitmap? {
    if (imageData.isNullOrBlank() || !imageData.startsWith("data:image/jpeg;base64,")) return null
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush = brush, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable { onGreetingClick() }
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = "Сообщений пока нет...",
                fontFamily = SfProText,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Отправьте сообщение или нажмите на приветствие ниже.",
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(10.dp))
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.raw.duck_greeting_sticker)
                    .crossfade(true)
                    .build(),
                imageLoader = gifImageLoader,
                contentDescription = null,
                modifier = Modifier.size(200.dp)
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
            .clip(RoundedCornerShape(16.dp))
            .background(color = LightGrayBackground)
            .border(width = 1.dp, brush = GlassBorder, shape = RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "ИЗБРАННЫЕ СТИКЕРЫ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = SfProText,
                color = Color.Gray,
            )
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 90.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = LightGrayBackground)
            ) {
                items(list.size) { index ->
                    StickerItem(
                        iconPath = list[index],
                        onStickerClick = onStickerClick,
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
            .clickable { onStickerClick(iconPath.toString()) }
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

private fun convertImageToOptimizedBase64(context: Context, imageUri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(imageUri)
        ?: throw Exception("Не удалось открыть поток изображения")

    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream.close()

    if (originalBitmap == null) throw Exception("Не удалось декодировать изображение")

    try {
        val maxSideTarget = 800f
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scaleFactor = if (width > height) maxSideTarget / width else maxSideTarget / height

        val bitmapToCompress = if (scaleFactor < 1f) {
            Bitmap.createScaledBitmap(
                originalBitmap,
                (width * scaleFactor).toInt(),
                (height * scaleFactor).toInt(),
                true
            )
        } else {
            originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        originalBitmap.recycle()

        if (bitmapToCompress == null || bitmapToCompress.isRecycled) {
            throw Exception("Не удалось создать изображение для сжатия")
        }

        val outputStream = ByteArrayOutputStream()
        bitmapToCompress.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        outputStream.close()
        bitmapToCompress.recycle()

        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64String"
    } catch (e: Exception) {
        if (!originalBitmap.isRecycled) originalBitmap.recycle()
        throw e
    }
}

@Composable
private fun ScrollToBottomButton(onScrollToBottomClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                clip = true,
                ambientColor = Color.Black.copy(alpha = 0.9f),
            )
            .clip(CircleShape)
            .background(brush = GlassBackground, shape = CircleShape)
            .border(width = 1.dp, brush = GlassBorder, shape = CircleShape)
            .clickable { onScrollToBottomClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_down),
            contentDescription = null,
            tint = LightBlack,
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