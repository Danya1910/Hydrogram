package com.example.hydrogram.presentation.widgets.messages.image

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.hydrogram.presentation.widgets.messages.text.MessageReactions
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.SfProText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.math.roundToInt
import kotlin.text.startsWith

@Composable
fun PenpalImageMessage(
    message: Message.Image,
    onReply: (Message.Image) -> Unit,
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

    val isBase64 = remember(message.image) {
        !message.image.isNullOrBlank() && message.image.startsWith("data:image/jpeg;base64,")
    }

    var imageSize by remember { mutableStateOf<Pair<Int?, Int?>?>(null) }

    LaunchedEffect(message.image) {
        if (isBase64) {
            val bitmap = withContext(Dispatchers.IO) {
                decodeBase64Image(message.image)
            }
            imageSize = if (bitmap != null) {
                bitmap.width to bitmap.height
            } else {
                null to null
            }
        } else {
            imageSize = null to null
        }
    }


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
        if (entry.key == mineId) {
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
    }


    val (imageWidth, imageHeight) = imageSize ?: (null to null)

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
        Column {
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
                            .align(Alignment.BottomEnd)
                            .padding(end = 7.dp, bottom = 7.dp)
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
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(
                visible = haveReaction,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val hasBothDifferentReactions = reactions?.mineReaction != null &&
                        reactions.penpalReaction != null &&
                        reactions.mineReaction != reactions.penpalReaction
                Box(
                    modifier = Modifier.padding(start = 10.dp)
                )
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
                                onReactionClick()
                            },
                            mineAvatar = if (mineReactionEmoji != null) mineAvatar else null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ReactionWidget(
                            reactions = MessageReactions(
                                mineReaction = null,
                                penpalReaction = reactions.penpalReaction
                            ),
                            color = Color(0xFFE1F1FF),
                            onReactionClick = {
                                onReactionClick()
                            },
                            mineAvatar = if (penpalReactionEmoji != null) penpalAvatar else null,
                        )
                    }
                } else {
                    ReactionWidget(
                        reactions = reactions,
                        color = Blue,
                        onReactionClick = {
                            onReactionClick()
                        },
                        mineAvatar = if (mineReactionEmoji != null) mineAvatar else null,
                        penpalAvatar = if (penpalReactionEmoji != null) penpalAvatar else null,
                    )
                }
            }
        }
    }
}

@Composable
fun PenpalReplyImageMessage(
    message: Message.Image,
    onReply: (Message.Image) -> Unit,
    replyName: String,
    onReplyMessageClick: (String) -> Unit,
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

    val isBase64 = remember(message.image) {
        !message.image.isNullOrBlank() && message.image.startsWith("data:image/jpeg;base64,")
    }

    var imageSize by remember { mutableStateOf<Pair<Int?, Int?>?>(null) }

    LaunchedEffect(message.image) {
        if (isBase64) {
            val bitmap = withContext(Dispatchers.IO) {
                decodeBase64Image(message.image)
            }
            imageSize = if (bitmap != null) {
                bitmap.width to bitmap.height
            } else {
                null to null
            }
        } else {
            imageSize = null to null
        }
    }

    val (imageWidth, imageHeight) = imageSize ?: (null to null)
    val maxWidth = 250.dp
    val maxHeight = 330.dp

    val (finalWidth, finalHeight) = remember(imageWidth, imageHeight) {
        if (imageWidth != null && imageHeight != null && imageWidth > 0 && imageHeight > 0) {
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
            var w = maxWidth.value
            var h = maxWidth.value / aspectRatio

            if (h > maxHeight.value) {
                h = maxHeight.value
                w = maxHeight.value * aspectRatio
            }
            w.dp to h.dp
        } else {
            maxWidth to maxWidth
        }
    }

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
        if (entry.key == mineId) {
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
        Column {
            Card(
                modifier = Modifier
                    .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                    .width(finalWidth)
                    .clip(
                        shape = RoundedCornerShape(12.dp)
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onDoubleClick = {
                                onDoubleClick(
                                    message.reactions?.get(mineId) != null
                                )
                            },
                            onLongClick = {
                                onLongClick(false)
                            }
                        )
                ) {

                    message.replyData?.let { reply ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                                .fillMaxWidth()
                                .height(41.dp)
                                .clip(shape = RoundedCornerShape(4.dp))
                                .background(color = Color(0xFFFFEBD6))
                                .clickable {
                                    onReplyMessageClick(message.replyData.messageId ?: "")
                                }
                                .padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(41.dp)
                                    .background(color = Color(0xFFFDB86F))
                            )
                            Spacer(modifier = Modifier.width(7.dp))

                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
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
                                        ReplyImagePreview(
                                            content = message.replyData?.content
                                        )
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
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(finalHeight)
                    ) {
                        Base64Image(
                            imageData = message.image,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 8.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.45f),
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
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(
                visible = haveReaction,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val hasBothDifferentReactions = reactions?.mineReaction != null &&
                        reactions.penpalReaction != null &&
                        reactions.mineReaction != reactions.penpalReaction
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                ) {
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
                                    onReactionClick()
                                },
                                mineAvatar = if (mineReactionEmoji != null) mineAvatar else null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ReactionWidget(
                                reactions = MessageReactions(
                                    mineReaction = null,
                                    penpalReaction = reactions.penpalReaction
                                ),
                                color = Color(0xFFE1F1FF),
                                onReactionClick = {
                                    onReactionClick()
                                },
                                mineAvatar = if (penpalReactionEmoji != null) penpalAvatar else null,
                            )
                        }
                    } else {
                        ReactionWidget(
                            reactions = reactions,
                            color = Blue,
                            onReactionClick = {
                                onReactionClick()
                            },
                            mineAvatar = if (mineReactionEmoji != null) mineAvatar else null,
                            penpalAvatar = if (penpalReactionEmoji != null) penpalAvatar else null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MineImageMessage(
    message: Message.Image,
    onReply: (Message.Image) -> Unit,
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

    val isBase64 = remember(message.image) {
        !message.image.isNullOrBlank() && message.image.startsWith("data:image/jpeg;base64,")
    }

    var imageSize by remember { mutableStateOf<Pair<Int?, Int?>?>(null) }

    LaunchedEffect(message.image) {
        if (isBase64) {
            val bitmap = withContext(Dispatchers.IO) {
                decodeBase64Image(message.image)
            }
            imageSize = if (bitmap != null) {
                bitmap.width to bitmap.height
            } else {
                null to null
            }
        } else {
            imageSize = null to null
        }
    }

    val (imageWidth, imageHeight) = imageSize ?: (null to null)

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    val maxWidth = (screenWidthDp * 0.7f).coerceAtMost(300.dp)
    val maxHeight = 400.dp

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
        if (entry.key == mineId) {
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
    }

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
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
        ) {
            Card(
                modifier = containerModifier,
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            onClick = {},
                            onDoubleClick = {
                                onDoubleClick(
                                    message.reactions?.get(mineId) != null
                                )
                            },
                            onLongClick = {
                                onLongClick(false)
                            }
                        )
                ) {
                    Base64Image(
                        imageData = message.image,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

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
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(
                visible = haveReaction,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val hasBothDifferentReactions = reactions?.mineReaction != null &&
                        reactions.penpalReaction != null &&
                        reactions.mineReaction != reactions.penpalReaction
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                ) {
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
                                    onReactionClick()
                                },
                                mineAvatar = if (mineReactionEmoji != null) mineAvatar else null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ReactionWidget(
                                reactions = MessageReactions(
                                    mineReaction = null,
                                    penpalReaction = reactions.penpalReaction
                                ),
                                color = Green,
                                onReactionClick = {
                                    onReactionClick()
                                },
                                mineAvatar = if (penpalReactionEmoji != null) penpalAvatar else null,
                            )
                        }
                    } else {
                        ReactionWidget(
                            reactions = reactions,
                            color = Color(0xFF40C13B),
                            onReactionClick = {
                                onReactionClick()
                            },
                            mineAvatar = if (mineReactionEmoji != null) mineAvatar else null,
                            penpalAvatar = if (penpalReactionEmoji != null) penpalAvatar else null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MineReplyImageMessage(
    message: Message.Image,
    onReply: (Message.Image) -> Unit,
    replyName: String,
    onReplyMessageClick: (String) -> Unit,
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

    val isBase64 = remember(message.image) {
        !message.image.isNullOrBlank() && message.image.startsWith("data:image/jpeg;base64,")
    }

    var imageSize by remember { mutableStateOf<Pair<Int?, Int?>?>(null) }

    LaunchedEffect(message.image) {
        if (isBase64) {
            val bitmap = withContext(Dispatchers.IO) {
                decodeBase64Image(message.image)
            }
            imageSize = if (bitmap != null) {
                bitmap.width to bitmap.height
            } else {
                null to null
            }
        } else {
            imageSize = null to null
        }
    }

    val (imageWidth, imageHeight) = imageSize ?: (null to null)
    val maxWidth = 250.dp
    val maxHeight = 330.dp

    val (finalWidth, finalHeight) = remember(imageWidth, imageHeight) {
        if (imageWidth != null && imageHeight != null && imageWidth > 0 && imageHeight > 0) {
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
            var w = maxWidth.value
            var h = maxWidth.value / aspectRatio

            if (h > maxHeight.value) {
                h = maxHeight.value
                w = maxHeight.value * aspectRatio
            }
            w.dp to h.dp
        } else {
            maxWidth to maxWidth
        }
    }

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
        if (entry.key == mineId) {
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
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Card(
                modifier = Modifier
                    .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                    .width(finalWidth)
                    .clip(
                        shape = RoundedCornerShape(12.dp)
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onDoubleClick = {
                                onDoubleClick(
                                    message.reactions?.get(mineId) != null
                                )
                            },
                            onLongClick = {
                                onLongClick(false)
                            }
                        )
                ) {

                    message.replyData?.let { reply ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                                .fillMaxWidth()
                                .height(41.dp)
                                .clip(shape = RoundedCornerShape(4.dp))
                                .background(color = Color(0xFFFFEBD6))
                                .clickable {
                                    onReplyMessageClick(message.replyData.messageId)
                                }
                                .padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(41.dp)
                                    .background(color = Color(0xFFFDB86F))
                            )
                            Spacer(modifier = Modifier.width(7.dp))

                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
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
                                        ReplyImagePreview(
                                            content = message.replyData?.content
                                        )
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
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(finalHeight)
                    ) {
                        Base64Image(
                            imageData = message.image,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

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
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(
                visible = haveReaction,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val hasBothDifferentReactions = reactions?.mineReaction != null &&
                        reactions.penpalReaction != null &&
                        reactions.mineReaction != reactions.penpalReaction
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                ) {
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
                                    onReactionClick()
                                },
                                mineAvatar = if (mineReactionEmoji != null) mineAvatar else null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ReactionWidget(
                                reactions = MessageReactions(
                                    mineReaction = null,
                                    penpalReaction = reactions.penpalReaction
                                ),
                                color = Green,
                                onReactionClick = {
                                    onReactionClick()
                                },
                                mineAvatar = if (penpalReactionEmoji != null) penpalAvatar else null,
                            )
                        }
                    } else {
                        ReactionWidget(
                            reactions = reactions,
                            color = Color(0xFF40C13B),
                            onReactionClick = {
                                onReactionClick()
                            },
                            mineAvatar = if (mineReactionEmoji != null) mineAvatar else null,
                            penpalAvatar = if (penpalReactionEmoji != null) penpalAvatar else null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyImagePreview(content: String?) {
    val isBase64 = remember(content) {
        !content.isNullOrBlank() && content.startsWith("data:image/jpeg;base64,")
    }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(content) {
        isLoading = true
        if (isBase64) {
            bitmap = withContext(Dispatchers.IO) {
                decodeBase64Image(content)
            }
        } else {
            bitmap = null
        }
        isLoading = false
    }

    Box(
        modifier = Modifier.size(36.dp)
    ) {
        if (isBase64) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Превью изображения в ответе",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (isLoading) {
                // Показываем плейсхолдер загрузки
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                )
            } else {
                PlaceholderContent()
            }
        } else {
            AsyncImage(
                model = content,
                contentDescription = "Превью изображения в ответе",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(R.drawable.ic_avatar),
                error = painterResource(R.drawable.ic_avatar),
            )
        }
    }
}

@Composable
fun Base64Image(
    imageData: String?,
    contentScale: ContentScale = ContentScale.FillBounds,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
) {
    val isBase64 = remember(imageData) {
        !imageData.isNullOrBlank() && imageData.startsWith("data:image/jpeg;base64,")
    }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(imageData) {
        isLoading = true
        if (isBase64) {
            bitmap = withContext(Dispatchers.IO) {
                decodeBase64Image(imageData)
            }
        } else {
            bitmap = null
        }
        isLoading = false
    }

    if (isBase64) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = modifier
            )
        } else if (isLoading && placeholder != null) {
            placeholder()
        } else if (placeholder != null) {
            placeholder()
        } else {
            PlaceholderContent()
        }
    } else {
        AsyncImage(
            model = imageData,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier,
            placeholder = painterResource(R.drawable.ic_avatar),
            error = painterResource(R.drawable.ic_avatar),
        )
    }
}