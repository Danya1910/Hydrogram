package com.example.hydrogram.presentation.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.ui.theme.Green
import com.example.hydrogram.ui.theme.LightGreen
import com.example.hydrogram.ui.theme.SfProText
import java.util.Date


@Composable
fun ChatScreen() {

}

@Composable
private fun Content() {

}

@Composable
private fun MineTextMessage(
    message: Message,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    BoxWithConstraints(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val maxBubbleWidth = maxWidth * 0.85f

        Box(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .widthIn(max = maxBubbleWidth)
                .clip(
                    shape = RoundedCornerShape(
                        bottomEnd = 0.dp,
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                    )
                )
                .background(
                    color = LightGreen,
                )
        ) {
            if(message.text.length <= 20) {
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
                            end = 62.dp,
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
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_status_read),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            } else {
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
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_status_read),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ChatScreenPreview() {

    val myMessage = Message(
        messageId = "msg_849201",
        senderId = "213", // Ваше сообщение
        text = "Короткое сообщение",
        type = "text",
        timestamp = System.currentTimeMillis(), // Текущее время в миллисекундах
        status = "sent"
    )

    val myMessageLong = Message(
        messageId = "msg_849201",
        senderId = "213", // Ваше сообщение
        text = "Короткое но длинное сообщение",
        type = "text",
        timestamp = System.currentTimeMillis(), // Текущее время в миллисекундах
        status = "sent"
    )

    Column (
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Green,
            )
            .padding(horizontal = 16.dp)
    ) {
        MineTextMessage(
            message = myMessage
        )
        Spacer(modifier = Modifier.height(10.dp))
        MineTextMessage(
            message = myMessageLong
        )
    }

}

