package com.example.hydrogram.presentation.widgets

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Gray
import com.example.hydrogram.ui.theme.SfProDisplay
import com.example.hydrogram.ui.theme.SfProText
import java.util.Date
import java.util.UUID


@Composable
fun ChatItem(
    chat: Chat,
    user: User?,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(chat.lastMessageTimestamp)
    ).toString()

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .height(78.dp)
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 16.dp,
            )
            .padding(vertical = 8.dp)
    ) {
        AsyncImage(
            model = user?.avatarUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_avatar),
            error =  painterResource(R.drawable.ic_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(62.dp)
                .clip(shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(top = 3.dp)
                .weight(1f)
        ) {
            Text(
                text = user?.name ?: "неизвестно",
                fontFamily = SfProDisplay,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = chat.lastMessage,
                fontFamily = SfProText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 3.dp, bottom = 5.dp)
                .weight(0.15f)
        ) {
            Text(
                text = formattedTime,
                fontFamily = SfProText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Gray,
                maxLines = 1,
            )
            if(chat.unreadCount != 0) {
                UnreadMessageWidget(
                    count = chat.unreadCount.toString()
                )
            }
        }
    }
}

@Composable
fun UnreadMessageWidget(
    count: String,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(20.dp)
            .widthIn(min = 20.dp)
            .background(
                color = Blue,
                shape = CircleShape
            )
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = count,
            fontFamily = SfProText,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ChatItemPreview(
) {

    val sampleChat = Chat(
        senderId = "user_12345",                        // ID собеседника
        chatId = UUID.randomUUID().toString(),          // Уникальный ID самого чата
        lastMessage = "Привет! Как продвигается копия ТГ?", // Текст последнего сообщения
        lastMessageType = "TEXT",                       // Тип (TEXT, IMAGE, VOICE и т.д.)
        lastMessageSenderId = "user_12345",             // Кто отправил последнее сообщение
        lastMessageTimestamp = System.currentTimeMillis(),// Время отправки в миллисекундах
        unreadCount = 2
    )

    val sampleUser = User(
        uid = "s123",
        name = "Pepe Shnele",
        avatarUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT000ICVOKtrsxkvyZ7Ujc5zQNcS-x0YJJ97x7jJxKc_9xqJL07OEju7Fs1&s=10",
        email = "user@example.com",
        isOnline = false,
        createdAt = System.currentTimeMillis()
    )

    ChatItem(
        chat = sampleChat,
        user = sampleUser,
    )
}