package com.example.hydrogram.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hydrogram.R
import com.example.hydrogram.presentation.navigation.NavigationData
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.BottomNavItem
import com.example.hydrogram.ui.theme.SelectedItem
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun BottomBar(
    currentRoute: String,
) {
    val buttons = listOf(
        NavigationData(
            title = "Контакты",
            icon = R.drawable.ic_contacts,
            route = "Contacts",
        ),
        NavigationData(
            title = "Чаты",
            icon = R.drawable.ic_chats,
            route = "Chats"
        ),
        NavigationData(
            title = "Настройки",
            icon = R.drawable.ic_settings,
            route = "Settings"
        )
    )


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .height(62.dp)
                .weight(1f)
                .background(
                    color = Color.White.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = CircleShape
                )
                .padding(all = 5.dp)
        ) {
            buttons.forEach { item ->
                val isSelected = item.route == currentRoute

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .background(
                            color = if(isSelected) SelectedItem else Color.Transparent,
                            shape = CircleShape,
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = null,
                            tint = if(isSelected) Blue else BottomNavItem,
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.title,
                            fontFamily = SfProText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if(isSelected) Blue else BottomNavItem,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        BottomSearch()
    }
}


@Composable
@Preview(showBackground = true)
fun BottomBarPreview() {

    BottomBar(
        currentRoute = "Chats"
    )

}

@Composable
private fun BottomSearch() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(62.dp)
            .background(
                color = Color.White.copy(alpha = 0.6f),
                shape = CircleShape
            )

    ) {
        Icon(
            painter = painterResource(R.drawable.ic_bottom_search),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
}