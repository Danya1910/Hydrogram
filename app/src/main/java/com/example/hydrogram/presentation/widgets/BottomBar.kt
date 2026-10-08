package com.example.hydrogram.presentation.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hydrogram.R
import com.example.hydrogram.presentation.navigation.NavigationData
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.BottomNavItem
import com.example.hydrogram.ui.theme.Red
import com.example.hydrogram.ui.theme.SelectedItem
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun BottomBar(
    navController: NavController,
    unreadCount: String? = null,
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
        ),
    )



    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.65f),
            Color.White.copy(alpha = 0.25f),
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rawRoute = navBackStackEntry?.destination?.route

    var lastRoute by remember { mutableStateOf<String?>(null) }
    val currentRoute = rawRoute ?: lastRoute

    LaunchedEffect(rawRoute) {
        if (rawRoute != null) lastRoute = rawRoute
    }

    DisposableEffect(Unit) {
        println(">>> BottomBar CREATED")
        onDispose { println(">>> BottomBar DISPOSED") }
    }



    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .height(62.dp)
                .weight(1f)
                .shadow(4.dp, CircleShape, clip = true)
                .background(Color.White, CircleShape)
                .border(1.dp, borderBrush, CircleShape)
                .padding(5.dp)
        ) {
            buttons.forEach { item ->
                BottomBarItem(
                    item = item,
                    isSelected = item.route == currentRoute,
                    unreadCount = unreadCount,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        BottomSearch()
    }
}

@Composable
private fun BottomBarItem(
    item: NavigationData,
    isSelected: Boolean,
    unreadCount: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Blue else BottomNavItem,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "iconColor",
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) SelectedItem else Color.Transparent,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "bgColor",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .background(bgColor, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = iconColor,
                )
                val showBadge = !unreadCount.isNullOrBlank() && unreadCount != "0"
                if (showBadge && item.route == "Chats") {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(start = 10.dp)
                            .height(16.dp)
                            .widthIn(min = 12.dp)
                            .clip(CircleShape)
                            .background(Red, CircleShape)
                            .padding(horizontal = 4.dp),
                    ) {
                        Text(
                            text = unreadCount,
                            fontFamily = SfProText,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = Color.White,
                            letterSpacing = -(0.23).sp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.title,
                fontFamily = SfProText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor,
            )
        }
    }
}


@Composable
@Preview(showBackground = true)
fun BottomBarPreview() {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "GLKFKGDKFJGKLJFD",
            fontFamily = SfProText,
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
        )

    }

}

@Composable
private fun BottomSearch() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(62.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                clip = true,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.4f),
            )
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