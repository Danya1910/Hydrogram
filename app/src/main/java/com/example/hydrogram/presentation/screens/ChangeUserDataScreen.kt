package com.example.hydrogram.presentation.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.util.BlueGlassBackground
import com.example.hydrogram.presentation.util.BlueGlassBorder
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.util.formatPhoneNumber
import com.example.hydrogram.presentation.viewModel.UserViewModel
import com.example.hydrogram.presentation.widgets.ChangeUserDataTopAppBar
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.Red
import com.example.hydrogram.ui.theme.SfProText

@Composable
fun ChangeUserDataScreen(
    userViewModel: UserViewModel,
    navController: NavController,
) {
    val uiState by userViewModel.userState.collectAsStateWithLifecycle()
    val mineId by userViewModel.currentId.collectAsStateWithLifecycle()

    val saveResult by userViewModel.saveResult.collectAsStateWithLifecycle()

    var isUserNameWidgetVisible by remember { mutableStateOf(false) }

    val isSaveUserNameSuccess by userViewModel.isSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        userViewModel.getCurrentUserId()
    }

    LaunchedEffect(mineId) {
        userViewModel.observeUser(
            uid = mineId,
        )
    }

    var name by remember { mutableStateOf("") }
    var aboutMe by remember { mutableStateOf("") }
    var birthdayDate by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is UserState.Success) {
            val user = (uiState as UserState.Success).user
            if (name.isEmpty() && user?.name != null) name = user.name
            if (aboutMe.isEmpty() && user?.aboutUser != null) aboutMe = user.aboutUser
            if (birthdayDate.isEmpty() && user?.birthdayDate != null) birthdayDate =
                user.birthdayDate
            if (userName.isEmpty() && user?.userName != null) userName = user.userName

        }
    }

    LaunchedEffect(saveResult) {
        saveResult?.let { result ->
            if (result.isSuccess) {
                userViewModel.resetSaveResult()
                navController.navigate(Screen.Settings.route)
            } else {
                TODO()
                //Надо сделать тост
                userViewModel.resetSaveResult()
            }
        }
    }

    LaunchedEffect(isSaveUserNameSuccess) {
        if(isSaveUserNameSuccess) {
            isUserNameWidgetVisible = false
        }
    }

    Scaffold(
        containerColor = LightGrayBackground,
        topBar = {
            if (!isUserNameWidgetVisible) {
                ChangeUserDataTopAppBar(
                    onCancelClick = {
                        navController.popBackStack()
                    },
                    onDoneClick = {
                        val currentUser = (uiState as? UserState.Success)?.user

                        userViewModel.saveProfile(
                            uid = mineId,
                            name = name,
                            avatarUrl = currentUser?.avatarUrl ?: "",
                            email = currentUser?.email ?: "",
                            isOnline = currentUser?.isOnline ?: true,
                            createdAt = 0L,
                            aboutUser = aboutMe,
                            birthdayDate = birthdayDate,
                            userName = currentUser?.userName ?: "",
                            phone = currentUser?.phone ?: "",
                        )
                    },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is UserState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UserState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message, color = Color.Red)
                    }
                }

                is UserState.Success -> {
                    val user = state.user
                    Log.d("ChangeData", "user data: $user")

                    Content(
                        user = user,
                        navController = navController,
                        paddingValues = paddingValues,
                        name = name,
                        onNameChange = { name = it },
                        aboutMe = aboutMe,
                        onAboutMeChange = { aboutMe = it },
                        onUserNameClick = { isUserNameWidgetVisible = true }
                    )
                }
            }
            AnimatedVisibility(
                visible = isUserNameWidgetVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 100)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isUserNameWidgetVisible = false }
                )
            }

            AnimatedVisibility(
                visible = isUserNameWidgetVisible,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 400)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()

                ) {
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isUserNameWidgetVisible = false }
                    )

                    ChangeUserNameWidget(
                        onCloseClick = { isUserNameWidgetVisible = false },
                        onDoneClick = {
                            userViewModel.saveUserName(
                                uid = mineId,
                                userName = userName,
                            )
                        },
                        userName = userName,
                        onValueChange = {
                            userName = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    user: User?,
    navController: NavController,
    paddingValues: PaddingValues,
    name: String,
    onNameChange: (String) -> Unit,
    aboutMe: String,
    onAboutMeChange: (String) -> Unit,
    onUserNameClick: () -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = LightGrayBackground,
            )
            .padding(
                horizontal = 16.dp
            )
    ) {
        ChangeAvatar(
            user = user,
        )
        Spacer(modifier = Modifier.height(16.dp))
        InputDataField(
            value = name,
            onValueChange =
                onNameChange,
            hintText = "",
        )
        Spacer(modifier = Modifier.height(8.dp))
        FieldHint(
            text = "Укажите имя и, если хотите, добавьте фотографию для Вашего профиля."
        )
        Spacer(modifier = Modifier.height(16.dp))
        InputDataField(
            value = aboutMe,
            onValueChange = onAboutMeChange,
            hintText = "О себе",
        )
        Spacer(modifier = Modifier.height(8.dp))
        FieldHint(
            text = "Напишите несколько строк о себе."
        )
        Spacer(modifier = Modifier.height(16.dp))
        BirthdayInput(
            value = name,
            onValueChange = {},
            hintText = "",
        )
        Spacer(modifier = Modifier.height(16.dp))
        ConnectionData(
            user = user,
            onUserNameClick = onUserNameClick,
        )
        Spacer(modifier = Modifier.height(16.dp))
        AccountButton(
            text = "Сменить аккаунт",
            textColor = Blue,
            onClick = {},
        )
        Spacer(modifier = Modifier.height(16.dp))
        AccountButton(
            text = "Выйти",
            textColor = Red,
            onClick = {},
        )
    }

}


@Composable
private fun ChangeAvatar(
    user: User?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 74.dp)
    ) {
        AsyncImage(
            model = user?.avatarUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_avatar),
            error = painterResource(R.drawable.ic_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(104.dp)
                .clip(shape = CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Изменить фотографию",
            fontFamily = SfProText,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Blue,
        )
    }
}


@Composable
private fun InputDataField(
    value: String,
    onValueChange: (String) -> Unit,
    hintText: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = 52.dp)
            .fillMaxWidth()
            .clip(
                shape = CircleShape
            )
            .background(
                color = Color.White,
            )
            .padding(horizontal = 10.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = SfProText,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                color = Color.Black,
                letterSpacing = (-0.43).sp,
            ),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = hintText,
                                fontFamily = SfProText,
                                fontSize = 17.sp,
                                color = Color.Gray.copy(alpha = 0.8f),
                                letterSpacing = (-0.43).sp,
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
private fun BirthdayInput(
    value: String,
    onValueChange: (String) -> Unit,
    hintText: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = 52.dp)
            .fillMaxWidth()
            .clip(
                shape = CircleShape
            )
            .background(
                color = Color.White,
            )
            .padding(horizontal = 10.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_avatar),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "День рождения",
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = LightBlack,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "1 янв",
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = Color.Gray.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun FieldHint(
    text: String,
) {
    Text(
        text = text,
        fontFamily = SfProText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Gray.copy(alpha = 0.8f),
        letterSpacing = (-0.23).sp,
        modifier = Modifier.padding(horizontal = 10.dp),
    )
}

@Composable
private fun ConnectionData(
    user: User?,
    onUserNameClick: () -> Unit,
) {

    val phoneNumber = formatPhoneNumber(
        rawInput = user?.phone ?: "",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                shape = RoundedCornerShape(18.dp),
            )
            .background(
                color = Color.White,
            )
    ) {
        ConnectionItem(
            icon = R.drawable.ic_green_phone,
            text = "Номер",
            hint = phoneNumber,
            onClick = {},
        )
        SeparatorLine(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 62.dp, end = 10.dp)
        )
        ConnectionItem(
            icon = R.drawable.ic_green_phone,
            text = "Имя пользователя",
            hint = user?.userName ?: "",
            onClick = onUserNameClick,
        )
        SeparatorLine(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 62.dp, end = 10.dp)
        )
        ConnectionItem(
            icon = R.drawable.ic_green_phone,
            text = "Персональные цвета",
            hint = "",
            onClick = {},
        )
    }

}

@Composable
private fun ConnectionItem(
    icon: Int,
    text: String,
    hint: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .background(
                color = Color.White
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontFamily = SfProText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = LightBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = hint,
                fontFamily = SfProText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray.copy(alpha = 0.8f),
                letterSpacing = (-0.43).sp,
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun AccountButton(
    text: String,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .clip(
                shape = CircleShape,
            )
            .background(
                color = Color.White,
            )
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = text,
            fontFamily = SfProText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


@Composable
@Preview(showBackground = true)
private fun ChangeUserDataScreenPreview() {

    var name by remember { mutableStateOf("D") }
    var aboutMe by remember { mutableStateOf("") }

    val user = User(
        uid = "user_12345",
        name = "Константин",
        avatarUrl = "https://example.com",
        email = "konstantin@example.com",
        isOnline = true,
        createdAt = 1719834000000L, // Какая-то дата в формате Long
        phone = "+7 (999) 123-45-67",
        aboutUser = "Разрабатываю лучшее приложение на Jetpack Compose 🚀",
        birthdayDate = "6 июля",
        userName = "@cat"
    )
}


@Composable
private fun ChangeUserNameWidget(
    onCloseClick: () -> Unit,
    onDoneClick: () -> Unit,
    userName: String,
    onValueChange: (String) -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = LightGrayBackground,
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // убираем визуальный эффект пульсации
            ) {
                /* Оставляем пустым, чтобы просто поглотить клик */
            }
            .padding(
                all = 16.dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.Absolute.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                GlassButton(
                    icon = R.drawable.ic_cross,
                    color = Color.Black,
                    onClick = onCloseClick,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Имя пользователя",
                    fontFamily = SfProText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    letterSpacing = (-0.23).sp,
                )
                Spacer(modifier = Modifier.weight(1f))
                GlassButton(
                    icon = R.drawable.ic_tick,
                    color = Blue,
                    onClick = onDoneClick,
                )
            }
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "ИМЯ ПОЛЬЗОВАТЕЛЯ",
                fontFamily = SfProText,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            UserNameInputField(
                value = userName,
                onValueChange = onValueChange,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = buildAnnotatedString {
                    append("Вы можете выбрать публичное имя пользователя в ")

                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    ) { // Можно изменить цвет на более темный для акцента
                        append("Hydrogram")
                    }

                    append(
                        ". В этом случае другие люди смогут найти Вас по такому имени и связаться," +
                                " не зная Вашего номера."
                    )
                },
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 10.dp),
                lineHeight = 18.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("Можно использовать  ")

                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    ) {
                        append("a-z, 0-9")
                    }

                    append(" и _. Минимальная длина — ")

                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    ) {
                        append("5")
                    }

                    append(" символов.")
                },
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 10.dp),
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun GlassButton(
    icon: Int,
    color: Color,
    onClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .width(44.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                clip = true, // Чтобы тень не обрезалась границами компонента
                ambientColor = Color.Black.copy(alpha = 0.9f), // Мягкий рассеянный свет
            )
            .clip(
                shape = CircleShape
            )
            .background(
                brush = if (color == Blue) BlueGlassBackground else GlassBackground,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                brush = if (color == Blue) BlueGlassBorder else GlassBorder,
                shape = CircleShape,
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = if (color == Blue) Color.White else Color.Black,
        )
    }
}

@Composable
private fun UserNameInputField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = "Имя пользователя",
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = SfProText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}