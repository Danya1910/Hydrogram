package com.example.hydrogram.presentation.screens

import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.repository.ChatRepository
import com.example.hydrogram.presentation.states.ChatUiState
import com.example.hydrogram.presentation.viewModel.ChatViewModel
import com.example.hydrogram.presentation.widgets.ChatInputField
import com.example.hydrogram.presentation.widgets.TopChatBar
import com.example.hydrogram.ui.theme.LightGreen
import com.example.hydrogram.ui.theme.MineMessageTimeColor
import com.example.hydrogram.ui.theme.PenpalMessageTimeColor
import com.example.hydrogram.ui.theme.SfProText
import java.util.Date


@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel,
) {

    var textState by remember { mutableStateOf("") }

    val uiState = chatViewModel.uiState.collectAsStateWithLifecycle()

    val mineId = chatViewModel.currentId.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.light_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopChatBar(
                    user = User(
                        name = "Dinosaur",
                    )
                )
            },
            bottomBar = {
                ChatInputField(
                    inputText = textState,
                    onValueChange = { newValue ->
                        textState = newValue
                    },
                    onSendClick = {
                        chatViewModel.sendMessage(
                            senderId = mineId,
                            chatId = TODO(),
                            text = textState,
                            type = "text",
                        )
                    },
                    onAttachClick = {
                        println("Нажата скрепка")
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val state = uiState) {
                    is ChatUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is ChatUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.message, color = Color.Red)
                        }
                    }

                    is ChatUiState.Success -> {
                        val messages = state.messages

                        // Список сообщений на экране
                        Content(
                            messages = messages,
                            paddingValues = paddingValues
                        )
                    }
                }
            }
        }

    }

}

@Composable
private fun Content(
    messages: List<Message>,
    paddingValues: PaddingValues,
) {

    val currentUserId = "1"
    val penpalId = "2"


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
            .padding(all = 16.dp)
    ) {
        items(
            items = messages,
            key = { message -> message.messageId }
        ) { message ->
            if (message.senderId == "1") {
                MineTextMessage(message = message)
            } else {
                PenpalTextMessage(message = message)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
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
            if (message.text.length <= 20) {
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SfProText,
                        color = MineMessageTimeColor,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_status_read),
                        contentDescription = null,
                        tint = MineMessageTimeColor,
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SfProText,
                        color = MineMessageTimeColor,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_status_read),
                        contentDescription = null,
                        tint = MineMessageTimeColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun PenpalTextMessage(
    message: Message,
) {

    val formattedTime = DateFormat.format(
        "HH:mm", Date(message.timestamp)
    ).toString()

    BoxWithConstraints(
        contentAlignment = Alignment.CenterStart,
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
            if (message.text.length <= 20) {
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
                        color = PenpalMessageTimeColor,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_status_read),
                        contentDescription = null,
                        tint = PenpalMessageTimeColor,
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
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_status_read),
                        contentDescription = null,
                        tint = PenpalMessageTimeColor,
                    )
                }
            }
        }
    }

}

@Composable
@Preview(showBackground = true)
private fun ChatScreenPreview() {


    ChatScreen()

}
