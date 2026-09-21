package com.example.hydrogram.presentation.widgets

import android.util.Log
import androidx.collection.buildLongLongMap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.presentation.util.BlueGlassBackground
import com.example.hydrogram.presentation.util.BlueGlassBorder
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Gray
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.Red
import com.example.hydrogram.ui.theme.SfProText
import kotlinx.coroutines.isActive
import java.util.Locale


@Composable
fun ChatInputField(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    onStickerClick: () -> Unit,
    isExpanded: Boolean,
    isEditing: Boolean,
    replyMessage: Message?,
    onCancelClick: () -> Unit,
    replyName: String,
    onReplyMessageClick: (String) -> Unit,
    editingMessage: Message?,
    onCancelEditClick: () -> Unit,
    isRecording: Boolean,
    changeRecordState: (Boolean) -> Unit,
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    onRecordCancel: () -> Unit,
) {

    val isTextMessage = inputText.isNotEmpty()

    var elapsedTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            val startTime = System.currentTimeMillis() - elapsedTime
            while (isActive) {
                withFrameMillis { frameTimeMillis ->
                    elapsedTime = System.currentTimeMillis() - startTime
                }
            }
        } else {
            elapsedTime = 0L
        }
    }

    val formattedTime = remember(elapsedTime) {
        val minutes = (elapsedTime / 60000) % 60
        val seconds = (elapsedTime / 1000) % 60
        val millis = (elapsedTime % 1000) / 10

        val minutesFormat = if (minutes < 10) "%1d" else "%02d"

        String.format(Locale.US, "$minutesFormat:%02d,%02d", minutes, seconds, millis)
    }

    Log.d("ChatInput", "currentEditingMessage: $editingMessage, replyMessage: $replyMessage")

    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(
                bottom = 7.dp,
            )
    ) {

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
        ) {
            AttachButton(
                onAttachClick = onAttachClick
            )
            Spacer(modifier = Modifier.width(6.dp))
            MessageInputField(
                inputText = inputText,
                onValueChange = onValueChange,
                onSendClick = onSendClick,
                onStickerClick = onStickerClick,
                modifier = Modifier.weight(1f),
                isExpanded = isExpanded,
                isEditing = isEditing,
                replyMessage = replyMessage,
                replyName = replyName,
                onCancelClick = {
                    onCancelClick()
                },
                onReplyMessageClick = { messageId ->
                    onReplyMessageClick(messageId)
                },
                editingMessage = editingMessage,
                onCancelEditClick = {
                    onCancelEditClick()
                },
                isRecording = isRecording,
                formattedTime = formattedTime,
                isTextMessage = isTextMessage,
            )
            AnimatedVisibility(
                visible = !isTextMessage,
                enter = expandHorizontally(
                    expandFrom = Alignment.Start,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.7f,
                    transformOrigin = TransformOrigin(0f, 0.5f),  // растёт от левого края
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(tween(150)),
                exit = shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                    animationSpec = tween(150)
                ) + scaleOut(
                    targetScale = 0.7f,
                    transformOrigin = TransformOrigin(0f, 0.5f),
                    animationSpec = tween(150)
                ) + fadeOut(tween(100)),
            ) {
                SendButton(
                    onSendClick = onSendClick,
                    isTextMessage = isTextMessage,
                    isRecording = isRecording,
                    changeRecordState = {
                        changeRecordState(it)
                    },
                    onRecordStart = {
                        onRecordStart()
                    },
                    onRecordStop = {
                        onRecordStop()
                    },
                    onRecordCancel = {
                        onRecordCancel()
                    },
                )
            }
        }
    }

}


@Composable
private fun AttachButton(
    onAttachClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(
                shape = CircleShape,
            )
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                clip = true,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.4f),
            )
            .background(
                brush = GlassBackground,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape
            )
            .clickable {
                onAttachClick()
            }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_clip),
            contentDescription = null,
            tint = Color.Black,
        )
    }
}

@Composable
private fun SendButton(
    onSendClick: () -> Unit,
    isTextMessage: Boolean,
    isRecording: Boolean,
    changeRecordState: (Boolean) -> Unit,
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    onRecordCancel: () -> Unit,
) {

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isRecording) 1.5f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val animatedColorStart by animateColorAsState(
        targetValue =
            if (isRecording) Blue.copy(alpha = 0.95f)
            else Color(0xFFDDDDDD).copy(alpha = 1f),
        animationSpec = tween(durationMillis = 200),
        label = "GradientStart"
    )

    val animatedColorCenter by animateColorAsState(
        targetValue =
            if (isRecording) Blue.copy(alpha = 0.75f)
            else Color(0xFFF7F7F7).copy(alpha = 1f),
        animationSpec = tween(durationMillis = 200),
        label = "GradientEnd"
    )

    val animatedColorEnd by animateColorAsState(
        targetValue =
            if (isRecording) Blue.copy(alpha = 0.88f)
            else Color(0xFFFFFFFF).copy(alpha = 0.65f),
        animationSpec = tween(durationMillis = 200),
        label = "GradientEnd"
    )

    val dynamicBrush = Brush.linearGradient(
        colors = listOf(animatedColorStart, animatedColorCenter, animatedColorEnd)
    )

    val density = LocalDensity.current

    val cancelThresholdPx = with(density) { (-100).dp.toPx() }
    val criticalLevelOfDecreasePx = with(density) { (-80).dp.toPx() }

    var dragOffset by remember { mutableFloatStateOf(0f) }

    val haptic = LocalHapticFeedback.current
    var isHapticTriggered by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = if (dragOffset == 0f) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        } else {
            tween(durationMillis = 0)
        },
    )

    val dragProgress = if (criticalLevelOfDecreasePx != 0f) {
        (animatedOffset / criticalLevelOfDecreasePx).coerceIn(0f, 1f)
    } else 0f

    val finalScale = scaleAnimation + (0.9f - scaleAnimation) * dragProgress

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(start = 8.dp)
            .graphicsLayer(
                scaleX = finalScale,
                scaleY = finalScale,
                translationX = animatedOffset,
            )
            .size(42.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.4f),
            )
            .drawBehind {
                drawCircle(brush = dynamicBrush)
            }
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape,
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    changeRecordState(true)
                    onRecordStart()

                    var isCanceled = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        when (event.type) {
                            PointerEventType.Move -> {
                                val delta = change.position.x - change.previousPosition.x
                                dragOffset = (dragOffset + delta).coerceIn(cancelThresholdPx, 0f)

                                if (dragOffset <= cancelThresholdPx) {
                                    isCanceled = true
                                    change.consume()
                                    break
                                }

                                change.consume()
                            }

                            PointerEventType.Release -> {
                                if (dragOffset <= cancelThresholdPx) {
                                    isCanceled = true
                                }
                                break
                            }
                        }
                    }

                    if (isCanceled) {
                        changeRecordState(false)
                        onRecordCancel()
                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                    } else {
                        changeRecordState(false)
                        onRecordStop()
                    }

                    dragOffset = 0f
                    isHapticTriggered = false
                }
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_microphone),
            contentDescription = null,
            tint = Color.Black,
        )
    }
}

@Composable
private fun MessageInputField(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onStickerClick: () -> Unit,
    modifier: Modifier,
    isExpanded: Boolean,
    isEditing: Boolean,
    replyMessage: Message?,
    replyName: String,
    onCancelClick: () -> Unit,
    onReplyMessageClick: (String) -> Unit,
    editingMessage: Message?,
    onCancelEditClick: () -> Unit,
    isRecording: Boolean,
    formattedTime: String,
    isTextMessage: Boolean,
) {

    val inputHeight by animateDpAsState(
        targetValue = when {
            isExpanded -> 96.dp
            isEditing -> 96.dp
            else -> 42.dp
        },
        animationSpec = tween(durationMillis = 300),
    )

    Log.d(
        "ChatInput",
        "currentEditingMessage: $editingMessage, replyMessage: $replyMessage, isEditing: $isEditing"
    )

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(inputHeight)
            .background(
                brush = GlassBackground,
                shape = RoundedCornerShape(21.dp)
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = RoundedCornerShape(21.dp)
            )
    ) {
        if (isRecording) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                RecordingIndicator()
                Spacer(modifier = Modifier.width(25.dp))
                RecordingTime(
                    formattedTime = formattedTime,
                )
                Spacer(modifier = Modifier.width(16.dp))
                HelpText()
            }
        } else {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        vertical = 4.dp
                    )
            ) {

                AnimatedVisibility(
                    visible = isEditing && editingMessage is Message.Text,
                    enter = fadeIn(
                        animationSpec = tween(300, delayMillis = 50)
                    ) + slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = tween(300, delayMillis = 50)
                    ) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = tween(300, delayMillis = 50)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(200)
                    ) + slideOutVertically(
                        targetOffsetY = { -it / 2 },
                        animationSpec = tween(200)
                    ) + scaleOut(
                        targetScale = 0.9f,
                        animationSpec = tween(200)
                    )
                ) {
                    Log.d("ChatInput", "editingMessage: $editingMessage")
                    EditMessageData(
                        editMessage = editingMessage,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 6.dp),
                        onCancelEditClick = { onCancelEditClick() },
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(
                        animationSpec = tween(300, delayMillis = 50)
                    ) + slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = tween(300, delayMillis = 50)
                    ) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = tween(300, delayMillis = 50)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(200)
                    ) + slideOutVertically(
                        targetOffsetY = { -it / 2 },
                        animationSpec = tween(200)
                    ) + scaleOut(
                        targetScale = 0.9f,
                        animationSpec = tween(200)
                    )
                ) {
                    Log.d("ChatInput", "replyMessage: $replyMessage")
                    ReplyMessageData(
                        replyMessage = replyMessage,
                        replyName = replyName,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 6.dp),
                        onCancelClick = { onCancelClick() },
                        onReplyMessageClick = { messageId ->
                            onReplyMessageClick(messageId)
                        }
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = SfProText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Сообщение",
                                        fontFamily = SfProText,
                                        fontSize = 17.sp,
                                        color = Color.Gray.copy(alpha = 0.8f)
                                    )
                                }
                                innerTextField()
                            }
                            Icon(
                                painter = painterResource(R.drawable.ic_sticker),
                                contentDescription = null,
                                tint = Gray,
                                modifier = Modifier
                                    .clickable {
                                        onStickerClick()
                                    }
                            )
                            AnimatedVisibility(
                                visible = isTextMessage,
                                enter = expandHorizontally(
                                    expandFrom = Alignment.End,
                                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                                ) + scaleIn(
                                    initialScale = 0.8f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) + fadeIn(tween(150)),
                                exit = shrinkHorizontally(
                                    shrinkTowards = Alignment.End,
                                    animationSpec = tween(150)
                                ) + fadeOut(animationSpec = tween(100)),
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .width(44.dp)
                                        .height(36.dp)
                                        .clip(
                                            shape = CircleShape
                                        )
                                        .background(
                                            brush = BlueGlassBackground
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = BlueGlassBorder,
                                            shape = CircleShape,
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_send_plane),
                                        contentDescription = null,
                                        tint = Color.White,
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            clip = true,
                            ambientColor = Color.Black.copy(alpha = 0.5f),
                            spotColor = Color.Black.copy(alpha = 0.4f),
                        ),
                )
            }
        }
    }
}

@Composable
private fun EditMessageData(
    editMessage: Message?,
    modifier: Modifier = Modifier,
    onCancelEditClick: () -> Unit,
) {

    if (editMessage !is Message.Text) {
        Log.e("EditMessageData", "editMessage is not Message.Text or null: $editMessage")
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(
                horizontal = 9.dp,
            )
            .fillMaxWidth()
            .height(41.dp)
            .clip(
                shape = RoundedCornerShape(4.dp)
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
                    color = Blue,
                )
        )
        Spacer(modifier = Modifier.width(7.dp))
        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Редактирование",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = -(0.23).sp,
                color = Blue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            editMessage.text?.let {
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

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.padding(16.dp))
        Icon(
            painter = painterResource(R.drawable.ic_cross),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(10.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onCancelEditClick()
                }
        )
    }
}

@Composable
private fun ReplyMessageData(
    replyMessage: Message?,
    replyName: String,
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit,
    onReplyMessageClick: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(
                horizontal = 9.dp,
            )
            .fillMaxWidth()
            .height(41.dp)
            .clip(
                shape = RoundedCornerShape(4.dp)
            )
            .clickable {
                onReplyMessageClick(replyMessage?.messageId ?: "")
            }
            .padding(
                end = 8.dp
            )
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(41.dp)
                .background(
                    color = Blue,
                )
        )
        Spacer(modifier = Modifier.width(7.dp))
        when (replyMessage?.type) {
            "image" -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = replyName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = -(0.23).sp,
                        color = Blue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Фотография",
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

            "voice" -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = replyName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = -(0.23).sp,
                        color = Blue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Голосовое сообщение",
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

            "text" -> {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = replyName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = -(0.23).sp,
                        color = Blue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    (replyMessage as Message.Text).text?.let {
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

            else -> {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = replyName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = -(0.23).sp,
                        color = Blue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Стикер",
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
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.padding(16.dp))
        Icon(
            painter = painterResource(R.drawable.ic_cross),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(10.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onCancelClick()
                }
        )
    }
}

@Composable
private fun RecordingIndicator(
) {
    val infiniteTransition = rememberInfiniteTransition()

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(
                shape = CircleShape
            )
            .alpha(
                alpha = alpha,
            )
            .background(
                color = Red,
            )
    )
}

@Composable
private fun RecordingTime(
    formattedTime: String,
) {

    Text(
        text = formattedTime,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        fontFamily = SfProText,
        color = LightBlack,
    )

}


@Composable
private fun HelpText() {

    val infiniteTransition = rememberInfiniteTransition()

    val animation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        )
    )

    val density = LocalDensity.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .graphicsLayer {
                translationX = animation * density.density
            }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_left),
            contentDescription = null,
            tint = LightBlack,
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = "Влево - отмена",
            fontFamily = SfProText,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = LightBlack,
        )
    }

}