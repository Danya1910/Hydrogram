package com.example.hydrogram.presentation.screens

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
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
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.ChatUiState
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.util.formatHeaderDate
import com.example.hydrogram.presentation.util.generateChatId
import com.example.hydrogram.presentation.util.getStartOfDay
import com.example.hydrogram.presentation.viewModel.ChatViewModel
import com.example.hydrogram.presentation.viewModel.UserViewModel
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
    userViewModel: UserViewModel,
    penpalId: String?,
) {

    var textState by remember { mutableStateOf("") }

    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val mineId by chatViewModel.currentId.collectAsStateWithLifecycle()

    val chatId = remember(mineId, penpalId) {
        if (mineId.isNotEmpty() && !penpalId.isNullOrEmpty()) {
            generateChatId(userId1 = mineId, userId2 = penpalId)
        } else ""
    }

    LaunchedEffect(penpalId) {
        userViewModel.observeUser(
            uid = penpalId ?: ""
        )
    }

    val penpalData by userViewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        chatViewModel.getCurrentUserId()
        Log.d("ChatScreen", "mineId: $mineId, penpalId: $penpalId")
    }

    LaunchedEffect(mineId) {
        Log.d("ChatScreen", "mineId: $mineId")
    }

    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            chatViewModel.observeChatHistory(chatId = chatId)
            Log.d("ChatScreen", "Успешный старт чата по ID: $chatId")
        }
    }


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
                when (val state = penpalData) {
                    is UserState.Loading -> {
                        TopChatBar(
                            user = User(
                                name = "Loading"
                            ),
                            onUserClick = {},
                            onBackClick = {
                                navController.popBackStack()
                            },
                        )
                    }

                    is UserState.Error -> {
                        TopChatBar(
                            user = User(
                                name = "Error"
                            ),
                            onUserClick = {},
                            onBackClick = {
                                navController.popBackStack()
                            },
                        )
                    }

                    is UserState.Success -> {
                        val user = state.user
                        Log.d("ChatScreen", "данные собеседника: $user")

                        TopChatBar(
                            user = user ?: User(),
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onUserClick = {
                                navController.navigate(Screen.UserProfile.createRoute(id = penpalId ?: ""))
                            },
                        )
                    }
                }

            },
            bottomBar = {
                ChatInputField(
                    inputText = textState,
                    onValueChange = { newValue ->
                        textState = newValue
                    },
                    onSendClick = {
                        if(textState.isNotBlank()) {
                            val messageText = textState
                            textState = ""
                            chatViewModel.sendMessage(
                                senderId = mineId,
                                chatId = chatId,
                                text = messageText,
                                type = "text",
                            )
                        }

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
                        Log.d("ChatScreen", "messages: $messages")

                        Content(
                            messages = messages,
                            paddingValues = paddingValues,
                            mineId = mineId,
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
    mineId: String,
) {

    val groupedMessages = remember(messages) {
        messages.groupBy { message -> getStartOfDay(message.timestamp) }
    }

    LazyColumn(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
            .padding(all = 16.dp)
    ) {
        groupedMessages.forEach { (dayTimestamp, dayMessages) ->

            // 1. Рисуем заголовок даты для конкретного дня
            item(key = "date_$dayTimestamp") {
                DateSeparator(text = formatHeaderDate(dayTimestamp))
            }

            // 2. Рисуем сообщения этого дня
            items(
                items = dayMessages,
                key = { message -> message.messageId }
            ) { message ->
                if (message.senderId == mineId) {
                    MineTextMessage(message = message)
                } else {
                    PenpalTextMessage(message = message)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DateSeparator(
    text: String,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(24.dp)
            .background(
                color = Color.Black.copy(alpha = 0.3f),
                shape = CircleShape,
            )
            .padding(
                horizontal = 7.dp,
            )
    ) {
        Text(
            text = text,
            fontFamily = SfProText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = (-0.08).sp,
        )
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
}
