package com.example.hydrogram.presentation.widgets.messages.text

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.presentation.screens.PlaceholderContent
import com.example.hydrogram.presentation.screens.decodeBase64Image
import com.example.hydrogram.presentation.util.MessageCallbacks
import com.example.hydrogram.presentation.util.MessageData
import com.example.hydrogram.presentation.widgets.messages.EditedText
import com.example.hydrogram.presentation.widgets.messages.ReactionWidget
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.LightGreen
import com.example.hydrogram.ui.theme.MineMessageTimeColor
import com.example.hydrogram.ui.theme.PenpalMessageTimeColor
import com.example.hydrogram.ui.theme.SfProText
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun MineTextMessage(
    message: Message.Text,
    messageCallbacks: MessageCallbacks,
    messageData: MessageData,
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

    val validReactions = message.reactions
        ?.filterValues { it != null }
        ?: emptyMap()

    val haveReaction = validReactions.isNotEmpty()

    var mineReactionId: String? = null
    var mineReactionEmoji: String? = null
    var penpalReactionId: String? = null
    var penpalReactionEmoji: String? = null

    var reactions: MessageReactions? = null


    message.reactions?.entries?.forEach { entry ->
        if (entry.key == messageData.mineId) {
            mineReactionId = entry.key
            mineReactionEmoji = entry.value

        } else {
            penpalReactionId = entry.key
            penpalReactionEmoji = entry.value
        }
        reactions = MessageReactions(
            mineReaction = mineReactionEmoji,
            penpalReaction = penpalReactionEmoji,
        )
        Log.d("Reaction", "$mineReactionId reacted with $mineReactionEmoji")
        Log.d("Reaction", "$penpalReactionId reacted with $penpalReactionEmoji")
    }



    BoxWithConstraints(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            messageCallbacks.onReply(message)
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
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        messageCallbacks.onDoubleClick(
                            message.reactions?.get(messageData.mineId) != null
                        )
                    },
                    onLongClick = {
                        messageCallbacks.onLongClick(
                            false
                        )
                    }
                )
        ) {
            message.text?.length?.let {
                if (it <= 20) {
                    Column(
                        modifier = Modifier
                            .padding(
                                top = 5.dp,
                                start = 10.dp,
                                end = if(message.isEdited) 122.dp else 68.dp,
                                bottom = 5.dp
                            )
                    ) {
                        Text(
                            text = message.text,
                            fontFamily = SfProText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                        )
                        AnimatedVisibility(
                            visible = haveReaction,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Log.d("MineTextMessage", "mineAvatar: $messageData.mineAvatar")
                            val hasBothDifferentReactions = reactions?.mineReaction != null &&
                                    reactions.penpalReaction != null &&
                                    reactions.mineReaction != reactions.penpalReaction
                            if (hasBothDifferentReactions) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = reactions.mineReaction,
                                            penpalReaction = null
                                        ),
                                        color = Color(0xFF40C13B),
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = null,
                                            penpalReaction = reactions.penpalReaction
                                        ),
                                        color = Green,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                }
                            } else {
                                ReactionWidget(
                                    reactions = reactions,
                                    color = Color(0xFF40C13B),
                                    onReactionClick = {
                                        messageCallbacks.onReactionClick()
                                    },
                                    mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 3.dp)
                            .align(
                                Alignment.BottomEnd
                            ),
                    ) {
                        if (message.isEdited) {
                            EditedText()
                            Spacer(modifier = Modifier.width(5.dp))
                        }
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
                    Column(
                        modifier = Modifier
                            .padding(
                                top = 5.dp,
                                start = 10.dp,
                                end = if(message.isEdited) 70.dp else 16.dp,
                                bottom = 16.dp
                            )
                    ) {
                        Text(
                            text = message.text,
                            fontFamily = SfProText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            letterSpacing = (-0.43).sp,
                        )
                        AnimatedVisibility(
                            visible = haveReaction,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Log.d("MineTextMessage", "mineAvatar: $messageData.mineAvatar")
                            val hasBothDifferentReactions = reactions?.mineReaction != null &&
                                    reactions.penpalReaction != null &&
                                    reactions.mineReaction != reactions.penpalReaction
                            if (hasBothDifferentReactions) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = reactions.mineReaction,
                                            penpalReaction = null
                                        ),
                                        color = Green,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = null,
                                            penpalReaction = reactions.penpalReaction
                                        ),
                                        color = Green,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                }
                            } else {
                                ReactionWidget(
                                    reactions = reactions,
                                    color = Green,
                                    onReactionClick = {
                                        messageCallbacks.onReactionClick()
                                    },
                                    mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 2.dp)
                            .align(
                                Alignment.BottomEnd
                            ),
                    ) {
                        if (message.isEdited) {
                            EditedText()
                            Spacer(modifier = Modifier.width(5.dp))
                        }
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
private fun PreviewMessage() {

    val textMessage = Message.Text(
        messageId = "msg_123456",
        senderId = "user_789",
        type = "text",
        status = "sent",
        timestamp = System.currentTimeMillis(),
        reactions = null,
        replyData = null,
        isEdited = true,
        text = "Привет, !"
    )

    MineTextMessage(
        message = textMessage,
        messageCallbacks = MessageCallbacks(
            onReply = {},
            onReplyMessageClick = {},
            onDoubleClick = {},
            onLongClick = {},
            onReactionClick = {},
        ),
        messageData = MessageData(
            replyName = "",
            mineId = "",
            mineAvatar = "",
            penpalAvatar = "",
        ),
    )
}

@Composable
fun MineReplyTextMessage(
    message: Message.Text,
    messageCallbacks: MessageCallbacks,
    messageData: MessageData,
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

    val validReactions = message.reactions
        ?.filterValues { it != null }
        ?: emptyMap()

    val haveReaction = validReactions.isNotEmpty()

    var mineReactionId: String? = null
    var mineReactionEmoji: String? = null
    var penpalReactionId: String? = null
    var penpalReactionEmoji: String? = null

    var reactions: MessageReactions? = null


    message.reactions?.entries?.forEach { entry ->
        if (entry.key == messageData.mineId) {
            mineReactionId = entry.key
            mineReactionEmoji = entry.value

        } else {
            penpalReactionId = entry.key
            penpalReactionEmoji = entry.value
        }
        reactions = MessageReactions(
            mineReaction = mineReactionEmoji,
            penpalReaction = penpalReactionEmoji,
        )
        Log.d("Reaction", "$mineReactionId reacted with $mineReactionEmoji")
        Log.d("Reaction", "$penpalReactionId reacted with $penpalReactionEmoji")
    }

    BoxWithConstraints(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            messageCallbacks.onReply(message)
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
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        messageCallbacks.onDoubleClick(
                            message.reactions?.get(messageData.mineId) != null
                        )
                    },
                    onLongClick = {
                        messageCallbacks.onLongClick(
                            false
                        )
                    }
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
                        .clickable {
                            messageCallbacks.onReplyMessageClick(
                                message.replyData?.messageId ?: ""
                            )
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
                                color = Color(0xFF9EDB4E),
                            )
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    if (message.replyData?.type == "sticker") {
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = messageData.replyName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                letterSpacing = -(0.23).sp,
                                color = Color(0xFF9EDB4E),
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
                    } else if (message.replyData?.type == "text") {
                        message.replyData?.content.let {
                            if (it != null) {
                                Column(
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = messageData.replyName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        letterSpacing = -(0.23).sp,
                                        color = Color(0xFF9EDB4E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
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
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val replyContent = message.replyData?.content
                            val isBase64 = remember(replyContent) {
                                !replyContent.isNullOrBlank() && replyContent.startsWith("data:image/jpeg;base64,")
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                            ) {
                                if (isBase64) {
                                    val bitmap = remember(replyContent) {
                                        decodeBase64Image(replyContent)
                                    }

                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Превью изображения в ответе",
                                            contentScale = ContentScale.Crop,
                                        )
                                    } else {
                                        PlaceholderContent()
                                    }
                                } else {
                                    AsyncImage(
                                        model = replyContent,
                                        contentDescription = "Превью изображения в ответе",
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.ic_avatar),
                                        error = painterResource(R.drawable.ic_avatar),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Column(
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = messageData.replyName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    letterSpacing = -(0.23).sp,
                                    color = Color(0xFF9EDB4E),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Фотография",
                                    fontFamily = SfProText,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 15.sp,
                                    letterSpacing = -(0.23).sp,
                                    color = Color(0xFF8FC748),
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
                    Column(
                        modifier = Modifier
                            .padding(
                                end = 65.dp,
                                bottom = 5.dp)
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
                        AnimatedVisibility(
                            visible = haveReaction,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Log.d("MineTextMessage", "mineAvatar: $messageData.mineAvatar")
                            val hasBothDifferentReactions = reactions?.mineReaction != null &&
                                    reactions.penpalReaction != null &&
                                    reactions.mineReaction != reactions.penpalReaction
                            if (hasBothDifferentReactions) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = reactions.mineReaction,
                                            penpalReaction = null
                                        ),
                                        color = Color(0xFF40C13B),
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = null,
                                            penpalReaction = reactions.penpalReaction
                                        ),
                                        color = Green,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                }
                            } else {
                                ReactionWidget(
                                    reactions = reactions,
                                    color = Color(0xFF40C13B),
                                    onReactionClick = {
                                        messageCallbacks.onReactionClick()
                                    },
                                    mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 3.dp)
                            .align(
                                Alignment.BottomEnd
                            ),
                    ) {
                        if (message.isEdited) {
                            EditedText()
                            Spacer(modifier = Modifier.width(5.dp))
                        }
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
fun PenpalReplyTextMessage(
    message: Message.Text,
    messageCallbacks: MessageCallbacks,
    messageData: MessageData,
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

    val validReactions = message.reactions
        ?.filterValues { it != null }
        ?: emptyMap()

    val haveReaction = validReactions.isNotEmpty()

    var mineReactionId: String? = null
    var mineReactionEmoji: String? = null
    var penpalReactionId: String? = null
    var penpalReactionEmoji: String? = null

    var reactions: MessageReactions? = null


    message.reactions?.entries?.forEach { entry ->
        if (entry.key == messageData.mineId) {
            mineReactionId = entry.key
            mineReactionEmoji = entry.value

        } else {
            penpalReactionId = entry.key
            penpalReactionEmoji = entry.value
        }
        reactions = MessageReactions(
            mineReaction = mineReactionEmoji,
            penpalReaction = penpalReactionEmoji,
        )
        Log.d("Reaction", "$mineReactionId reacted with $mineReactionEmoji")
        Log.d("Reaction", "$penpalReactionId reacted with $penpalReactionEmoji")
    }

    BoxWithConstraints(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            messageCallbacks.onReply(message)
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
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        messageCallbacks.onDoubleClick(
                            message.reactions?.get(messageData.mineId) != null
                        )
                    },
                    onLongClick = {
                        messageCallbacks.onLongClick(
                            false
                        )
                    }
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
                        .clickable {
                            messageCallbacks.onReplyMessageClick(
                                message.replyData?.messageId ?: ""
                            )
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
                                color = Color(0xFFFDB86F),
                            )
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (message.replyData?.type == "sticker") {
                            Column(
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = messageData.replyName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    letterSpacing = -(0.23).sp,
                                    color = Color(0xFFFDB86F),
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
                        } else if (message.replyData?.type == "text") {
                            message.replyData?.content.let {
                                if (it != null) {
                                    Column(
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = messageData.replyName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            letterSpacing = -(0.23).sp,
                                            color = Color(0xFFFDB86F),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
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
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val replyContent = message.replyData?.content
                                val isBase64 = remember(replyContent) {
                                    !replyContent.isNullOrBlank() && replyContent.startsWith("data:image/jpeg;base64,")
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                ) {
                                    if (isBase64) {
                                        val bitmap = remember(replyContent) {
                                            decodeBase64Image(replyContent)
                                        }

                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Превью изображения в ответе",
                                                contentScale = ContentScale.Crop,
                                            )
                                        } else {
                                            PlaceholderContent()
                                        }
                                    } else {
                                        AsyncImage(
                                            model = replyContent,
                                            contentDescription = "Превью изображения в ответе",
                                            contentScale = ContentScale.Crop,
                                            placeholder = painterResource(R.drawable.ic_avatar),
                                            error = painterResource(R.drawable.ic_avatar),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Column(
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = messageData.replyName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        letterSpacing = -(0.23).sp,
                                        color = Color(0xFFFDB86F),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "Фотография",
                                        fontFamily = SfProText,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                        letterSpacing = -(0.23).sp,
                                        color = Color(0xFFEFB578),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(end = 65.dp, bottom = 5.dp)
                    ) {
                        Text(
                            text = message.text ?: "",
                            fontFamily = SfProText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()

                        )
                        AnimatedVisibility(
                            visible = haveReaction,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Log.d("MineTextMessage", "mineAvatar: $messageData.mineAvatar")
                            val hasBothDifferentReactions = reactions?.mineReaction != null &&
                                    reactions.penpalReaction != null &&
                                    reactions.mineReaction != reactions.penpalReaction
                            if (hasBothDifferentReactions) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = reactions.mineReaction,
                                            penpalReaction = null
                                        ),
                                        color = Blue,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = null,
                                            penpalReaction = reactions.penpalReaction
                                        ),
                                        color = Color(0xFFCCE3F8),
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                }
                            } else {
                                if (reactions?.mineReaction == null && reactions?.penpalReaction != null) {
                                    ReactionWidget(
                                        reactions = reactions,
                                        color = Color(0xFFCCE3F8),
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                        penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                } else {
                                    ReactionWidget(
                                        reactions = reactions,
                                        color = Blue,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                        penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 3.dp)
                            .align(
                                Alignment.BottomEnd
                            ),
                    ) {
                        if (message.isEdited) {
                            EditedText()
                            Spacer(modifier = Modifier.width(5.dp))
                        }
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
fun PenpalTextMessage(
    message: Message.Text,
    messageCallbacks: MessageCallbacks,
    messageData: MessageData,
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

    val validReactions = message.reactions
        ?.filterValues { it != null }
        ?: emptyMap()

    val haveReaction = validReactions.isNotEmpty()

    var mineReactionId: String? = null
    var mineReactionEmoji: String? = null

    var penpalReactionId: String? = null
    var penpalReactionEmoji: String? = null

    var reactions: MessageReactions? = null


    message.reactions?.entries?.forEach { entry ->
        if (entry.key == messageData.mineId) {
            mineReactionId = entry.key
            mineReactionEmoji = entry.value

        } else {
            penpalReactionId = entry.key
            penpalReactionEmoji = entry.value
        }
        reactions = MessageReactions(
            mineReaction = mineReactionEmoji,
            penpalReaction = penpalReactionEmoji,
        )
        Log.d("Reaction", "$mineReactionId reacted with $mineReactionEmoji")
        Log.d("Reaction", "$penpalReactionId reacted with $penpalReactionEmoji")
    }

    Log.d("PenpalTextMessage", "reactions: $reactions")

    BoxWithConstraints(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount < -150f) {
                            messageCallbacks.onReply(message)
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
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        messageCallbacks.onDoubleClick(
                            message.reactions?.get(messageData.mineId) != null
                        )
                    },
                    onLongClick = {
                        messageCallbacks.onLongClick(
                            false
                        )
                    }
                )
        ) {
            message.text?.length?.let {
                if (it <= 20) {
                    Column(
                        modifier = Modifier
                            .padding(
                                top = 5.dp,
                                start = 10.dp,
                                end = 42.dp,
                                bottom = 5.dp
                            )
                    ) {
                        Text(
                            text = message.text,
                            fontFamily = SfProText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                        )
                        AnimatedVisibility(
                            visible = haveReaction,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Log.d("MineTextMessage", "mineAvatar: $messageData.mineAvatar")
                            val hasBothDifferentReactions = reactions?.mineReaction != null &&
                                    reactions.penpalReaction != null &&
                                    reactions.mineReaction != reactions.penpalReaction
                            if (hasBothDifferentReactions) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = reactions.mineReaction,
                                            penpalReaction = null
                                        ),
                                        color = Blue,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = null,
                                            penpalReaction = reactions.penpalReaction
                                        ),
                                        color = Color(0xFFCCE3F8),
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                }
                            } else {
                                if (reactions?.mineReaction == null && reactions?.penpalReaction != null) {
                                    ReactionWidget(
                                        reactions = reactions,
                                        color = Color(0xFFCCE3F8),
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                        penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                } else {
                                    ReactionWidget(
                                        reactions = reactions,
                                        color = Blue,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                        penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 3.dp)
                            .align(
                                Alignment.BottomEnd
                            ),
                    ) {
                        if (message.isEdited) {
                            EditedText()
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = SfProText,
                            color = PenpalMessageTimeColor,
                        )

                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(
                                top = 5.dp,
                                start = 10.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            )
                    ) {
                        Text(
                            text = message.text,
                            fontFamily = SfProText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            letterSpacing = (-0.43).sp,
                        )
                        AnimatedVisibility(
                            visible = haveReaction,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Log.d("MineTextMessage", "mineAvatar: $messageData.mineAvatar")
                            val hasBothDifferentReactions = reactions?.mineReaction != null &&
                                    reactions.penpalReaction != null &&
                                    reactions.mineReaction != reactions.penpalReaction
                            if (hasBothDifferentReactions) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = reactions.mineReaction,
                                            penpalReaction = null
                                        ),
                                        color = Blue,
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ReactionWidget(
                                        reactions = MessageReactions(
                                            mineReaction = null,
                                            penpalReaction = reactions.penpalReaction
                                        ),
                                        color = Color(0xFFE1F1FF),
                                        onReactionClick = {
                                            messageCallbacks.onReactionClick()
                                        },
                                        mineAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                    )
                                }
                            } else {
                                ReactionWidget(
                                    reactions = reactions,
                                    color = Blue,
                                    onReactionClick = {
                                        messageCallbacks.onReactionClick()
                                    },
                                    mineAvatar = if (mineReactionEmoji != null) messageData.mineAvatar else null,
                                    penpalAvatar = if (penpalReactionEmoji != null) messageData.penpalAvatar else null,
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 2.dp)
                            .align(
                                Alignment.BottomEnd
                            ),
                    ) {
                        if (message.isEdited) {
                            EditedText()
                            Spacer(modifier = Modifier.width(5.dp))
                        }
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


data class MessageReactions(
    val mineReaction: String?,
    val penpalReaction: String?,
)