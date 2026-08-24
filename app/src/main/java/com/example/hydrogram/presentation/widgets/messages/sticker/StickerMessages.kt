package com.example.hydrogram.presentation.widgets.messages.sticker

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.model.ReplyData
import com.example.hydrogram.presentation.screens.PlaceholderContent
import com.example.hydrogram.presentation.screens.decodeBase64Image
import com.example.hydrogram.ui.theme.MineMessageTimeColor
import com.example.hydrogram.ui.theme.SfProText
import java.util.Date
import kotlin.math.roundToInt
import kotlin.text.startsWith

@Composable
fun PenpalStickerMessage(
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
fun StickerPenpalTime(
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
fun MineStickerMessage(
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
fun MineStickerReplyMessage(
    sticker: Message.Sticker,
    context: Context,
    gifImageLoader: ImageLoader,
    replyName: String,
    onReply: (Message.Sticker) -> Unit,
    onReplyMessageClick: (String) -> Unit,
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
        contentAlignment = Alignment.TopEnd
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .width(IntrinsicSize.Min)
        ) {
            MineReplyMessageHelper(
                replyName = replyName,
                replyData = sticker.replyData,
                modifier = Modifier.wrapContentWidth(),
                onReplyMessageClick = {
                    onReplyMessageClick(sticker.replyData?.messageId ?: "")
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(192.dp)
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
}

@Composable
fun PenpalStickerReplyMessage(
    sticker: Message.Sticker,
    context: Context,
    gifImageLoader: ImageLoader,
    replyName: String,
    onReply: (Message.Sticker) -> Unit,
    onReplyMessageClick: (String) -> Unit,
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
        contentAlignment = Alignment.TopStart
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .width(IntrinsicSize.Min)
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(192.dp)
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
            Spacer(modifier = Modifier.weight(1f))
            MineReplyMessageHelper(
                replyName = replyName,
                replyData = sticker.replyData,
                modifier = Modifier.wrapContentWidth(),
                onReplyMessageClick = {
                    onReplyMessageClick(sticker.replyData?.messageId ?: "")
                }
            )
        }
    }
}

@Composable
fun MineReplyMessageHelper(
    replyName: String,
    replyData: ReplyData?,
    modifier: Modifier,
    onReplyMessageClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(41.dp)
            .clip(
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = Color(0xFFE2F7CA)
            )
            .clickable {
                onReplyMessageClick()
            }
            .padding(end = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(
                        color = Color(0xFF9EDB4E),
                    )
            )
            Spacer(modifier = Modifier.width(7.dp))
            if (replyData?.type == "sticker") {
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
            } else if (replyData?.type == "text") {
                replyData?.content.let {
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
                    val replyContent = replyData?.content
                    val isBase64 = remember(replyContent) {
                        !replyContent.isNullOrBlank() && replyContent.startsWith("data:image/jpeg;base64,")
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(
                                shape = RoundedCornerShape(4.dp)
                            )
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
    }
}

@Composable
fun StickerMineTime(
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