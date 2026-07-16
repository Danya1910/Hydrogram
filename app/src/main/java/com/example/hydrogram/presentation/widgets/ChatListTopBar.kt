package com.example.hydrogram.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.SfProText


@Composable
@Preview(showBackground = true)
fun ChatListTopBar(

) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = LightGrayBackground,
            )
    ) {
        EditButton(
            title = "Edit",
            onClick = {},
        )
        Spacer(modifier = Modifier.width(40.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
        ) {
            ChatListHat()
        }
        RowEdit(
            onEditClick = {},
            onHistoryClick = {},
        )
    }
}

@Composable
private fun EditButton(
    title: String,
    onClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .width(52.dp)
            .background(
                brush = GlassBackground,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape,
            )
            .clickable{
                onClick()
            },

    ) {
        Text(
            text = title,
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = LightBlack,
        )
    }

}

@Composable
private fun ChatListHat() {
    Text(
        text = "Chats",
        fontFamily = SfProText,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.26).sp,
        color = LightBlack,
    )
}

@Composable
private fun RowEdit(
    onHistoryClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .width(92.dp)
            .background(
                brush = GlassBackground,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape,
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_stories),
                contentDescription = null,
                tint = LightBlack,
                modifier = Modifier
                    .clickable{
                        onHistoryClick()
                    }
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = null,
                tint = LightBlack,
                modifier = Modifier
                    .clickable{
                        onEditClick()
                    }
            )
        }
    }
}