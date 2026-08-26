package com.example.hydrogram.presentation.widgets.messages.image

import android.text.format.DateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.hydrogram.ui.theme.SfProText
import java.util.Date
import kotlin.math.roundToInt
import kotlin.text.startsWith

@Composable
fun PenpalImageMessage(
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
    }
}

@Composable
fun PenpalReplyImageMessage(
    message: Message.Image,
    onReply: (Message.Image) -> Unit,
    replyName: String,
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

    val isBase64 = remember(message.image) {
        !message.image.isNullOrBlank() && message.image.startsWith("data:image/jpeg;base64,")
    }

    val imageSize = remember(message.image) {
        if (isBase64) {
            val bitmap = decodeBase64Image(message.image)
            if (bitmap != null) bitmap.width to bitmap.height else null to null
        } else {
            null to null
        }
    }

    val (imageWidth, imageHeight) = imageSize
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
                modifier = Modifier.fillMaxWidth()
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
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(finalHeight)
                        .clickable { /* Открыть в полном размере */ }
                ) {
                    if (isBase64) {
                        val bitmap = remember(message.image) { decodeBase64Image(message.image) }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            PlaceholderContent()
                        }
                    } else {
                        AsyncImage(
                            model = message.image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_avatar),
                            error = painterResource(R.drawable.ic_avatar),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

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
    }
}

@Composable
fun MineImageMessage(
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

@Composable
fun MineReplyImageMessage(
    message: Message.Image,
    onReply: (Message.Image) -> Unit,
    replyName: String,
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

    val isBase64 = remember(message.image) {
        !message.image.isNullOrBlank() && message.image.startsWith("data:image/jpeg;base64,")
    }

    val imageSize = remember(message.image) {
        if (isBase64) {
            val bitmap = decodeBase64Image(message.image)
            if (bitmap != null) bitmap.width to bitmap.height else null to null
        } else {
            null to null
        }
    }

    val (imageWidth, imageHeight) = imageSize
    val maxWidth = 250.dp // Оптимальная ширина для сообщений с картинками в чате
    val maxHeight = 330.dp

    // 1. ВЫЧИСЛЯЕМ ТОЧНЫЕ РАЗМЕРЫ НА ОСНОВЕ КАРТИНКИ
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
            maxWidth to maxWidth // Дефолтный квадратный размер, если габариты еще не загрузились
        }
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
                modifier = Modifier.fillMaxWidth()
            ) {

                // Блок ответа (показывается сверху, если есть replyData)
                message.replyData?.let { reply ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                            // РЕШЕНИЕ: Плашка растягивается ровно по ширине фотографии, заданной у Card
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
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(finalHeight) // Высота картинки
                        .clickable { /* Открыть в полном размере */ }
                ) {
                    if (isBase64) {
                        val bitmap = remember(message.image) { decodeBase64Image(message.image) }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            PlaceholderContent()
                        }
                    } else {
                        AsyncImage(
                            model = message.image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_avatar),
                            error = painterResource(R.drawable.ic_avatar),
                            modifier = Modifier.fillMaxSize()
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
}