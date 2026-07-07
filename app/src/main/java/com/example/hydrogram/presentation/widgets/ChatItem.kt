package com.example.hydrogram.presentation.widgets

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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Gray
import com.example.hydrogram.ui.theme.SfProDisplay
import com.example.hydrogram.ui.theme.SfProText
import java.nio.file.WatchEvent
import java.util.UUID


@Composable
fun ChatItem(
    chat: Chat,
) {
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
        Icon(
            painter = painterResource(R.drawable.ic_avatar),
            contentDescription = null,
            tint = Color.Unspecified,
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
                text = "Pepe",
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
                //text = chat.lastMessageTimestamp,
                text = "19:45",
                fontFamily = SfProText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Gray,
                maxLines = 1,
            )
            UnreadMessageWidget(
                count = "1"
            )
        }
    }
}

@Composable
private fun UnreadMessageWidget(
    count: String,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(20.dp)
            .widthIn(min = 20.dp)
            .background(
                color = Blue,
                shape = RoundedCornerShape(1000.dp)
            )
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = "123",
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
        lastMessageTimestamp = System.currentTimeMillis() // Время отправки в миллисекундах
    )

    ChatItem(
        chat = sampleChat
    )
}