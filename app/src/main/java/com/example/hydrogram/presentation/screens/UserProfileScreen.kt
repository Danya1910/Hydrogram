package com.example.hydrogram.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.util.UserInfoRowItem
import com.example.hydrogram.presentation.util.formatPhoneNumber
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun UserProfileScreen() {
    Scaffold { paddingValues ->
        Content(
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun Content(
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        UserInfoHat(
            user = User(
                name = "Didi"
            )
        )
//        MenuRow(
//            items =
//        )

    }
}

@Composable
private fun ContentPreview(
    user: User,
    items: List<UserInfoRowItem>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        UserInfoHat(
            user = user
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = LightGrayBackground
                )
        ) {
            MenuRow(
                items = items
            )
        }

    }
}
@Composable
private fun UserInfoHat(
    user: User?
) {

    val phoneNumber = formatPhoneNumber(
        rawInput = user?.phone ?: ""
    )

    Box(
        modifier = Modifier
            .background(
                color = Blue
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(
                    horizontal = 16.dp,
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                GlassButton(icon = R.drawable.ic_qr)
                Spacer(modifier = Modifier.weight(1f))
                GlassButton(text = "Edit")
            }
            Icon(
                painter = painterResource(R.drawable.ic_avatar),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(104.dp)
                    .clip(shape = CircleShape)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = user?.name ?: "Unknown",
                fontFamily = SfProText,
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.38.sp,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "был(а) недавно",
                fontFamily = SfProText,
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.25).sp,
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun GlassButton(
    icon: Int? = null,
    text: String? = null,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 44.dp)
            .background(
                brush = GlassBackground,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                shape = CircleShape,
                brush = GlassBorder,
            )
            .padding(horizontal = 10.dp)
    ) {
        if (text != null) {
            Text(
                text = text,
                fontFamily = SfProText,
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (icon != null) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color.Black,
            )
        }
    }
}

@Composable
private fun MenuRow(
    items: List<UserInfoRowItem>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 16.dp,
                horizontal = 16.dp,
            )
            .clip(
                shape = RoundedCornerShape(26.dp)
            )
            .background(
                color = Color.White,
            )
    ) {
        items.forEachIndexed { index, item ->
            MenuRowItem(item = item)
            if (index != items.size - 1) {
                SeparatorLine(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuRowItem(
    item: UserInfoRowItem,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(68.dp)
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
            )
    ) {
        Text(
            text = item.title,
            fontFamily = SfProText,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = LightBlack,
            letterSpacing = (-0.23).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = item.text,
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = item.textColor,
            letterSpacing = (-0.43).sp,
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun UserProfileScreenPreview() {

    val user = User(
        name = "User Name",
        phone = "9279434335",
    )

    val items = listOf(
        UserInfoRowItem(
            title = "мобильный",
            text = formatPhoneNumber(
                rawInput = user.phone
            ),
            textColor = Blue,
        ),
        UserInfoRowItem(
            title = "имя пользователя",
            text = "@cat",
            textColor = Blue,
        ),
        UserInfoRowItem(
            title = "день рождения",
            text = "6 июля",
            textColor = LightBlack,
        ),
        UserInfoRowItem(
            title = "о себе",
            text = "EYP",
            textColor = LightBlack,
        ),
    )

    ContentPreview(
        user = user,
        items = items,
    )
}