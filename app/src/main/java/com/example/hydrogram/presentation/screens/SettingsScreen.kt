package com.example.hydrogram.presentation.screens

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.util.MenuRowItem
import com.example.hydrogram.presentation.util.formatPhoneNumber
import com.example.hydrogram.presentation.viewModel.UserViewModel
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    navController: NavController,
) {
        Scaffold(
            containerColor = LightGrayBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
        userViewModel.setTargetUserId(uid = mineId)
    }

    val profileList = listOf(
        MenuRowItem(
            title = "Поставить эмодзи статус",
            icon = R.drawable.ic_outline_emoji,
            onClick = {},
        ),
        MenuRowItem(
            title = "Изменить фотографию",
            icon = R.drawable.ic_outline_camera,
            onClick = {},
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = LightGrayBackground,
            )
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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 16.dp
                    )
                    .padding(
                        top = 16.dp
                    )
            ) {
                MenuRow(
                    items = profileList
                )
                Spacer(modifier = Modifier.height(24.dp))
                FakeMenuRow(
                    items = listOf(
                        MenuRowItem(
                            title = "Мой профиль",
                            icon = R.drawable.ic_profile,
                            onClick = {
                                navController.navigate(
                                    Screen.UserProfile.createRoute(id = mineId)
                                )
                            },
                            gradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF2D55).copy(alpha = 0.7f),
                                    Color(0xFFF2234B).copy(alpha = 0.9f),
                                    Color(0xFFD2042C).copy(alpha = 1f),
                                )
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                FakeMenuRow(
                    items = listOf(
                        MenuRowItem(
                            title = "Кошелёк",
                            icon = R.drawable.ic_wallet,
                            onClick = {},
                            gradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF189CFF).copy(alpha = 0.7f),
                                    Color(0xFF0D7FF4).copy(alpha = 0.9f),
                                    Color(0xFF025DD1).copy(alpha = 1f),
                                )
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                FakeMenuRow(
                    items = listOf(
                        MenuRowItem(
                            title = "Избранное",
                            icon = R.drawable.ic_saved_messages,
                            onClick = {},
                            gradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF189CFF).copy(alpha = 0.7f),
                                    Color(0xFF0D7FF4).copy(alpha = 0.9f),
                                    Color(0xFF025DD1).copy(alpha = 1f),
                                )
                            )
                        ),
                        MenuRowItem(
                            title = "Недавние звонки",
                            icon = R.drawable.ic_recent_calls,
                            onClick = {},
                            gradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF48DB6D).copy(alpha = 0.7f),
                                    Color(0xFF2BBE50).copy(alpha = 0.9f),
                                    Color(0xFF06992B).copy(alpha = 1f),
                                )
                            )
                        ),
                        MenuRowItem(
                            title = "Устройства",
                            icon = R.drawable.ic_devices,
                            onClick = {},
                            gradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFA33E).copy(alpha = 0.7f),
                                    Color(0xFFFF9C37).copy(alpha = 0.9f),
                                    Color(0xFFFF902B).copy(alpha = 1f),
                                )
                            )
                        ),
                        MenuRowItem(
                            title = "Папки с чатами",
                            icon = R.drawable.ic_chat_folders,
                            onClick = {},
                            gradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF17D4FC).copy(alpha = 0.7f),
                                    Color(0xFF0CB5DD).copy(alpha = 0.9f),
                                    Color(0xFF0295BD).copy(alpha = 1f),
                                )
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Spacer(
                    modifier = Modifier.windowInsetsBottomHeight(
                        WindowInsets.navigationBars.add(WindowInsets(bottom = 74.dp))
                    )
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

    val userName = user?.userName

    val avatarBitmap = remember(user?.avatarUrl) {
        val url = user?.avatarUrl
        if (!url.isNullOrBlank() && url.startsWith("data:image/jpeg;base64,")) {
            try {
                val base64String = url.substringAfter("base64,")
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 74.dp)
            .padding(
                horizontal = 16.dp,
            )
    ) {
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(104.dp)
                    .clip(shape = CircleShape)
            )
        } else {
            AsyncImage(
                model = null,
                contentDescription = null,
                placeholder = painterResource(R.drawable.ic_avatar),
                error = painterResource(R.drawable.ic_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(104.dp)
                    .clip(shape = CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = user?.name ?: "Unknown",
            fontFamily = SfProText,
            fontSize = 28.sp,
            color = LightBlack,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.38.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "$phoneNumber • @$userName",
            fontFamily = SfProText,
            fontSize = 20.sp,
            color = LightBlack,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(5.dp))
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
            .clip(
                shape = CircleShape
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
            .clickable {
                onClick()
            }
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
            MenuRowItem(
                item = item,
            )
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
private fun FakeMenuRow(
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
            FakeItem(
                item = item,
            )
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
            tint = Blue,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
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
private fun FakeItem(
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
            .clickable{
                item.onClick()
            }
            .padding(horizontal = 20.dp)
    ) {
        item.gradient?.let {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        brush = it
                    )
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.width(17.dp))
        Text(
            text = item.title,
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
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

fun Modifier.gradientTint(brush: Brush): Modifier = this
    .graphicsLayer(alpha = 0.99f)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = brush,
            blendMode = BlendMode.SrcIn
        )
    }

@Composable
private fun TopBar(
    navController: NavController,
) {

    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            LightGrayBackground.copy(alpha = 0.8f),
            LightGrayBackground.copy(alpha = 0.6f),
            LightGrayBackground.copy(alpha = 0.4f),
            Color.Transparent,
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .statusBarsPadding()
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
            text = "Изм.",
            onClick = {
                navController.navigate(Screen.ChangeUserData.route)
            },
        )
    }
}