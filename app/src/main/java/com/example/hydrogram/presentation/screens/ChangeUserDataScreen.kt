package com.example.hydrogram.presentation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

    LaunchedEffect(uiState) {
        if (uiState is UserState.Success) {
            val user = (uiState as UserState.Success).user
            if (name.isEmpty() && user?.name != null) name = user.name
            if (aboutMe.isEmpty() && user?.aboutUser != null) aboutMe = user.aboutUser
            if (birthdayDate.isEmpty() && user?.birthdayDate != null) birthdayDate = user.birthdayDate
        }
    }

    LaunchedEffect(saveResult) {
        saveResult?.let { result ->
            if(result.isSuccess) {
                userViewModel.resetSaveResult()
                navController.navigate(Screen.Settings.route)
            } else {
                TODO()
                //Надо сделать тост
                userViewModel.resetSaveResult()
            }
        }
    }

    Scaffold(
        containerColor = LightGrayBackground,
        topBar = {
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
        },
    ) { paddingValues ->
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
                    onAboutMeChange = { aboutMe = it }
                )
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
            onClick = {},
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

