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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.presentation.screens.PlaceholderContent
import com.example.hydrogram.presentation.screens.decodeBase64Image
import com.example.hydrogram.presentation.widgets.messages.ReactionWidget
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
    onReply: (Message.Text) -> Unit,
    onDoubleClick: (Boolean) -> Unit,
    onLongClick: (Boolean) -> Unit,
    onReactionClick: () -> Unit,
    mineId: String,
    mineAvatar: String,
    penpalAvatar: String,
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
        if(entry.key == mineId) {
            mineReactionId = entry.key
            mineReactionEmoji = entry.value

        }else {
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
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        onDoubleClick(
                            message.reactions?.get(mineId) != null
                        )
                    },
                    onLongClick = {
                        onLongClick(
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
                                    end = 68.dp,
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
                            Log.d("MineTextMessage", "mineAvatar: $mineAvatar")
                            ReactionWidget(
                                reactions = reactions,
                                color = Green,
                                onReactionClick = {
                                    onReactionClick()
                                },
                                mineAvatar = mineAvatar,
                                penpalAvatar = "data:image/jpeg;base64,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",
                            )
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
                    Column {
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
                        AnimatedVisibility(
                            visible = haveReaction,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {

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
fun MineReplyTextMessage(
    message: Message.Text,
    replyName: String,
    onReply: (Message.Text) -> Unit,
    onReplyMessageClick: (String) -> Unit,
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
                        .clickable {
                            onReplyMessageClick(
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
                                text = replyName,
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
                                        text = replyName,
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
                                    text = replyName,
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
fun PenpalReplyTextMessage(
    message: Message.Text,
    replyName: String,
    onReply: (Message.Text) -> Unit,
    onReplyMessageClick: (String) -> Unit,
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
                        .clickable {
                            onReplyMessageClick(
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
                                    text = replyName,
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
                                            text = replyName,
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
                                        text = replyName,
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
fun PenpalTextMessage(
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


data class MessageReactions(
    val mineReaction: String?,
    val penpalReaction: String?,
)