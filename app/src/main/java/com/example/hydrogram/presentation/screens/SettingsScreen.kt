package com.example.hydrogram.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.internal.composableLambdaInstance
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.util.MenuRowItem
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.SfProText


@Composable
@Preview(showBackground = true)
fun SettingsScreen() {
    SettingsScreenPreview()
}

@Composable
private fun SettingsScreenPreview() {

    val user = User(
        name = "Dinosaur"
    )

    val profileList = listOf(
        MenuRowItem(
            title = "Set Emoji Status",
            icon = R.drawable.ic_bottom_search,
            onClick = {},
        ),
        MenuRowItem(
            title = "Set Emoji Status",
            icon = R.drawable.ic_settings,
            onClick = {},
        ),
        MenuRowItem(
            title = "Set Emoji Status",
            icon = R.drawable.ic_contacts,
            onClick = {},
        ),
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.White,
            )
    ) {
        UserInfoHat(user = user)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = LightGrayBackground,
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        all = 16.dp
                    )
            ) {
                MenuRow(
                    items = profileList
                )
            }
        }
    }
}

@Composable
private fun UserInfoHat(
    user: User?
) {
    Box(
        modifier = Modifier
            .background(
                color = Blue
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
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
                letterSpacing = 0.38.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "+888 888 888 • @dino",
                fontFamily = SfProText,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
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

    val glassBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.25f),
        )
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.65f),
            Color.White.copy(alpha = 0.10f),
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 44.dp)
            .background(
                brush = glassBrush,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                shape = CircleShape,
                brush = borderBrush,
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
    items: List<MenuRowItem>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                shape = RoundedCornerShape(26.dp)
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
    item: MenuRowItem,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .background(
                color = Color.White,
            )
            .padding(horizontal = 20.dp)
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            color = Color.Black,
            letterSpacing = (-0.43).sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
