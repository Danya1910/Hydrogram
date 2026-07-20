package com.example.hydrogram.presentation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.util.MenuRowItem
import com.example.hydrogram.presentation.util.formatPhoneNumber
import com.example.hydrogram.presentation.viewModel.UserViewModel
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    navController: NavController,
) {
    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
            )
        },
        bottomBar = {
            BottomBar(
                navController = navController,
            )
        },
    ) { paddingValues ->
        Content(
            userViewModel = userViewModel,
            navController = navController,
            paddingValues = paddingValues,
        )
    }
}

@Composable
private fun Content(
    userViewModel: UserViewModel,
    navController: NavController,
    paddingValues: PaddingValues,
) {

    val mineId by userViewModel.currentId.collectAsStateWithLifecycle()
    val mineData by userViewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        userViewModel.getCurrentUserId()
    }

    LaunchedEffect(mineId) {
        userViewModel.observeUser(uid = mineId)
    }

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
            .padding(paddingValues = paddingValues)
    ) {
        when (val state = mineData) {
            is UserState.Loading -> {
                UserInfoHat(
                    user = User(
                        name = "Loading..."
                    )
                )
            }

            is UserState.Error -> {
                UserInfoHat(
                    user = User(
                        name = "Error..."
                    )
                )
            }

            is UserState.Success -> {
                val user = state.user
                Log.d("ChatScreen", "мои данные: $user")
                UserInfoHat(user = user)
            }
        }

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
                letterSpacing = 0.38.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "$phoneNumber • @username",
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
    onClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 44.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                clip = true,
                ambientColor = Color.Black.copy(alpha = 0.9f),
            )
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

@Composable
private fun TopBar(
    navController: NavController,
) {

    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.8f),
            Color.White.copy(alpha = 0.6f),
            Color.White.copy(alpha = 0.4f),
            Color.Transparent,
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(54.dp)
            .fillMaxWidth()
            .background(
                brush = glassBrush,
            )
            .padding(horizontal = 16.dp)
    ) {
        GlassButton(
            icon = R.drawable.ic_qr,
            onClick = {},
        )
        Spacer(modifier = Modifier.weight(1f))
        GlassButton(
            text = "Edit",
            onClick = {},
        )
    }
}