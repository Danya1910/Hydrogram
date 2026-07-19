package com.example.hydrogram.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.util.MenuRowItem
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Gray
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.SfProText

@Composable
fun ChangeUserDataScreen() {
}


@Composable
private fun ChangeAvatar(
    user: User?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
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
            .heightIn(min = 42.dp)
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
                fontSize = 18.sp,
                color = Color.Black,
                letterSpacing = (-0.43).sp,
            ),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = hintText,
                                fontFamily = SfProText,
                                fontSize = 18.sp,
                                color = Color.Gray.copy(alpha = 0.8f)
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
            .heightIn(min = 42.dp)
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
            fontSize = 18.sp,
            color = LightBlack,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "1 янв",
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            color = Color.Gray.copy(alpha = 0.8f),
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ChangeUserDataScreenPreview() {

    val name by remember { mutableStateOf("D") }
    val aboutMe by remember { mutableStateOf("") }

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
            onValueChange = {},
            hintText = "",
        )
        Spacer(modifier = Modifier.height(16.dp))
        BirthdayInput(
            value = name,
            onValueChange = {},
            hintText = "",
        )
    }
}

