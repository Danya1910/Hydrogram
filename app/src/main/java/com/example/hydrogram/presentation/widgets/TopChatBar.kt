package com.example.hydrogram.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.OfflineStatusColor
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun TopChatBar(
    user: User,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        BackButton(
            onBackClick = {}
        )
        Spacer(modifier = Modifier.width(30.dp))
        UserName(
            onUserClick = {},
            user = user,
        )
        Spacer(modifier = Modifier.width(30.dp))
        UserIcon(
            user = user,
            onIconClick = {},
        )
    }
}

@Composable
private fun BackButton(
    onBackClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
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
            painter = painterResource(R.drawable.ic_arrow_left),
            contentDescription = null,
            tint = Color.Black,
        )
    }
}

@Composable
private fun UserIcon(
    user: User?,
    onIconClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
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
        AsyncImage(
            model = user?.avatarUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_avatar),
            error =  painterResource(R.drawable.ic_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(38.dp)
                .clip(shape = CircleShape)
        )
    }
}

@Composable
private fun UserName(
    onUserClick: () -> Unit,
    user: User,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(44.dp)
            .background(
                brush = GlassBackground,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape
            )
            .padding(horizontal = 17.dp, vertical = 5.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Text(
                text = user.name,
                fontFamily = SfProText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = (-0.23).sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (user.isOnline) "В сети" else "Был(а) недавно",
                fontFamily = SfProText,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = if (user.isOnline) Blue else OfflineStatusColor,
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF7D5260)
private fun TopChatBarPreview(
) {

    val user = User(
        name = "Dog"
    )

    TopChatBar(
        user = user,
    )
}
