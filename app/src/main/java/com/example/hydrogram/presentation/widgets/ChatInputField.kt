package com.example.hydrogram.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Gray
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun ChatInputField(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
) {

    val isTextMessage = inputText.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        AttachButton(
            onAttachClick = {}
        )
        Spacer(modifier = Modifier.width(6.dp))
        MessageInputField(
            inputText = inputText,
            onValueChange = onValueChange,
            onSendClick = onSendClick,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        SendButton(
            onSendClick = {},
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
            .background(
                brush = GlassBackground,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape
            )
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
                ),
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
    modifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(42.dp)
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
                                onSendClick()
                            }

                    )
                }
            }

        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF7D5260)
private fun ChatInputFieldPreview() {

    var textState by remember { mutableStateOf("") }

    ChatInputField(
        inputText = textState,
        onValueChange = { newValue ->
            textState = newValue
        },
        onSendClick = {
            println("Отправлено: $textState")
            textState = ""
        },
        onAttachClick = {
            println("Нажата скрепка")
        }
    )

}
