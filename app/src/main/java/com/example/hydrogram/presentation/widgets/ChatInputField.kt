package com.example.hydrogram.presentation.widgets

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.model.ReplyData
import com.example.hydrogram.presentation.screens.PlaceholderContent
import com.example.hydrogram.presentation.screens.decodeBase64Image
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Gray
import com.example.hydrogram.ui.theme.SfProText
import kotlin.text.startsWith


@Composable
fun ChatInputField(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    onStickerClick: () -> Unit,
    isExpanded: Boolean,
    replyMessage: Message?,
    onCancelClick: () -> Unit,
    replyName: String,
    onReplyMessageClick: (String) -> Unit,
) {

    val isTextMessage = inputText.isNotEmpty()

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(
                top = 4.dp,
                bottom = 0.dp,
            )
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
            replyMessage = replyMessage,
            replyName = replyName,
            onCancelClick = {
                onCancelClick()
            },
            onReplyMessageClick = { messageId ->
                onReplyMessageClick(messageId)
            }
        )
        Spacer(modifier = Modifier.width(6.dp))
        SendButton(
            onSendClick = onSendClick,
            isTextMessage = isTextMessage
        )

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
) {

    if (isTextMessage) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(44.dp)
                .height(42.dp)
                .background(
                    color = Blue,
                    shape = CircleShape,
                )
                .border(
                    width = 1.dp,
                    color = Blue,
                    shape = CircleShape,
                )
                .clip(
                    shape = CircleShape
                )
                .clickable {
                    onSendClick()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_send_plane),
                contentDescription = null,
                tint = Color.White,
            )
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .background(
                    brush = GlassBackground,
                    shape = CircleShape,
                )
                .border(
                    width = 1.dp,
                    brush = GlassBorder,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_microphone),
                contentDescription = null,
                tint = Color.Black,
            )
        }
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
    replyMessage: Message?,
    replyName: String,
    onCancelClick: () -> Unit,
    onReplyMessageClick: (String) -> Unit,
) {

    val inputHeight by animateDpAsState(
        targetValue = if (isExpanded) 96.dp else 42.dp,
        animationSpec = tween(durationMillis = 300),
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
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    vertical = 4.dp
                )
        ) {

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
                    }
                }
            )
        }
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
@Preview(showBackground = true, backgroundColor = 0xFF7D5260)
private fun ChatInputFieldPreview() {

    var textState by remember { mutableStateOf("") }

//    ChatInputField(
//        inputText = textState,
//        onValueChange = { newValue ->
//            textState = newValue
//        },
//        onSendClick = {
//            println("Отправлено: $textState")
//            textState = ""
//        },
//        onAttachClick = {
//            println("Нажата скрепка")
//        },
//        onStickerClick = {
//
//        },
//        isExpanded = false,
//        replyMessage = Message(),
//        replyName = "Debil",
//        onCancelClick = {},
//    )

}
