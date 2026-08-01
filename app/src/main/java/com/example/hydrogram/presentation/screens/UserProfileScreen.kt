package com.example.hydrogram.presentation.screens

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.model.UserPresence
import com.example.hydrogram.presentation.states.UserState
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.util.UserInfoRowItem
import com.example.hydrogram.presentation.util.formatLastSeen
import com.example.hydrogram.presentation.util.formatPhoneNumber
import com.example.hydrogram.presentation.viewModel.UserViewModel
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.SfProText
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank
import kotlin.text.startsWith
import kotlin.text.substringAfter


@Composable
fun UserProfileScreen(
    userViewModel: UserViewModel,
    navController: NavController,
    userId: String,
) {
    Scaffold { paddingValues ->
        Content(
            userViewModel = userViewModel,
            navController = navController,
            userId = userId,
            paddingValues = paddingValues,
        )
    }
}

@Composable
private fun Content(
    userViewModel: UserViewModel,
    navController: NavController,
    userId: String,
    paddingValues: PaddingValues,
) {
    val userData by userViewModel.userState.collectAsStateWithLifecycle()
    val presenceState by userViewModel.opponentPresenceState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        userViewModel.setTargetUserId(uid = userId)
    }

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    /*
     * Расстояние растягивания фотографии.
     *
     * 0f   = обычное состояние
     * 400f = полностью раскрытая фотография
     */
    val overScrollAnim = remember {
        Animatable(0f)
    }

    val overScrollY = overScrollAnim.value

    /*
     * Состояние жеста.
     *
     * Нужно для того, чтобы не запускать несколько
     * анимаций одновременно.
     */
    var isDragging by remember {
        mutableStateOf(false)
    }

    /*
     * NestedScroll отвечает за Pull-To-Expand.
     */
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {

                /*
                 * Только когда LazyColumn находится
                 * в самом верху.
                 */
                val atTop =
                    scrollState.firstVisibleItemIndex == 0 &&
                            scrollState.firstVisibleItemScrollOffset == 0

                /*
                 * Свайп вниз.
                 */
                if (atTop && available.y > 0f) {

                    isDragging = true

                    coroutineScope.launch {
                        /*
                         * Коэффициент 0.4 делает растягивание
                         * немного "тяжелее" пальца.
                         *
                         * Если хочешь 1:1 с пальцем:
                         * поставь 1f.
                         */
                        val newValue =
                            (overScrollAnim.value + available.y * 0.45f)
                                .coerceIn(0f, 400f)

                        overScrollAnim.snapTo(newValue)
                    }

                    return Offset(
                        x = 0f,
                        y = available.y,
                    )
                }

                /*
                 * Если фотография уже растянута,
                 * свайп вверх уменьшает её.
                 */
                if (overScrollAnim.value > 0f && available.y < 0f) {

                    isDragging = true

                    coroutineScope.launch {

                        val newValue =
                            (overScrollAnim.value + available.y)
                                .coerceAtLeast(0f)

                        overScrollAnim.snapTo(newValue)
                    }

                    return Offset(
                        x = 0f,
                        y = available.y,
                    )
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {

                if (!isDragging) {
                    return super.onPostFling(
                        consumed,
                        available
                    )
                }

                isDragging = false

                /*
                 * Если пользователь потянул достаточно далеко,
                 * раскрываем фотографию.
                 */
                if (overScrollAnim.value > 120f) {

                    overScrollAnim.animateTo(
                        targetValue = 400f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    )

                } else {

                    /*
                     * Иначе возвращаем фотографию назад.
                     */
                    overScrollAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                }

                return super.onPostFling(
                    consumed,
                    available
                )
            }
        }
    }

    /*
     * Закрытие раскрытой фотографии свайпом вверх.
     *
     * Это дополнительно обрабатывает случай,
     * когда фотография уже раскрыта.
     */
    val collapseFraction by remember {
        derivedStateOf {

            if (scrollState.firstVisibleItemIndex == 0) {

                (
                        scrollState.firstVisibleItemScrollOffset
                            .toFloat() / 240f
                        )
                    .coerceIn(0f, 1f)

            } else {
                1f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .nestedScroll(nestedScrollConnection)
    ) {

        when (val state = userData) {

            is UserState.Loading -> {

                UserInfoHat(
                    user = User(
                        name = "Loading...",
                    ),
                    navController = navController,
                    presenceState = presenceState,
                    collapseFraction = collapseFraction,
                    overScrollY = overScrollY,
                    onCloseExpanded = {
                        coroutineScope.launch {
                            overScrollAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                            )
                        }
                    },
                )
            }

            is UserState.Error -> {

                UserInfoHat(
                    user = User(
                        name = "Error...",
                    ),
                    navController = navController,
                    presenceState = presenceState,
                    collapseFraction = collapseFraction,
                    overScrollY = overScrollY,
                    onCloseExpanded = {
                        coroutineScope.launch {
                            overScrollAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                            )
                        }
                    },
                )
            }

            is UserState.Success -> {

                val user = state.user

                val aboutUser = user?.aboutUser
                val birthday = user?.birthdayDate
                val userName = user?.userName

                val items = listOfNotNull(

                    UserInfoRowItem(
                        title = "мобильный",
                        text = formatPhoneNumber(
                            rawInput = user?.phone ?: ""
                        ),
                        textColor = Blue,
                        onClick = {},
                    ),

                    if (!userName.isNullOrEmpty()) {

                        UserInfoRowItem(
                            title = "имя пользователя",
                            text = "@$userName",
                            textColor = Blue,
                            onClick = {},
                        )

                    } else {
                        null
                    },

                    if (!birthday.isNullOrEmpty()) {

                        UserInfoRowItem(
                            title = "день рождения",
                            text = birthday,
                            textColor = LightBlack,
                            onClick = {},
                        )

                    } else {
                        null
                    },

                    if (!aboutUser.isNullOrEmpty()) {

                        UserInfoRowItem(
                            title = "о себе",
                            text = aboutUser,
                            textColor = LightBlack,
                            onClick = {},
                        )

                    } else {
                        null
                    },
                )

                Log.d(
                    "UserProfileScreen",
                    "данные пользователя: $user"
                )

                /*
                 * Основной контент.
                 */
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = LightGrayBackground
                        )
                ) {

                    /*
                     * Место под шапку.
                     */
                    item {

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(275.dp)
                                .background(Color.White)
                        )
                    }

                    item {
                        MenuRow(items = items)
                    }

                    item {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        ChatDataRow()
                    }

                    item {

                        Spacer(
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }

                /*
                 * Шапка поверх LazyColumn.
                 */
                UserInfoHat(
                    user = user,
                    navController = navController,
                    presenceState = presenceState,
                    collapseFraction = collapseFraction,
                    overScrollY = overScrollY,
                    onCloseExpanded = {

                        coroutineScope.launch {

                            overScrollAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                            )
                        }
                    },
                )
            }
        }
    }
}


@Composable
private fun UserInfoHat(
    user: User?,
    navController: NavController,
    presenceState: UserPresence,
    collapseFraction: Float,
    overScrollY: Float,
    onCloseExpanded: () -> Unit,
) {


    val stretchProgress =
        (overScrollY / 400f)
            .coerceIn(0f, 1f)



    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val targetScale =
        screenWidth.value / 104f

    val avatarScale =
        1f + (targetScale - 1f) * stretchProgress


    val avatarTranslationY =
        overScrollY * 0.35f


    val cornerRadius =
        52f * (1f - stretchProgress)


    val contentAlpha =
        (
                1f - stretchProgress * 2.2f
                ).coerceIn(0f, 1f)


    val avatarBitmap = remember(user?.avatarUrl) {

        val url = user?.avatarUrl

        if (
            url != null &&
            url.isNotBlank() &&
            url.startsWith(
                "data:image/jpeg;base64,"
            )
        ) {

            try {

                val base64String =
                    url.substringAfter("base64,")

                val imageBytes =
                    Base64.decode(
                        base64String,
                        Base64.DEFAULT
                    )

                BitmapFactory.decodeByteArray(
                    imageBytes,
                    0,
                    imageBytes.size
                )

            } catch (e: Exception) {

                e.printStackTrace()

                null
            }

        } else {
            null
        }
    }

    val formattedLastSeenTime =
        formatLastSeen(
            lastSeenTimestamp = presenceState.lastSeen
        )


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(275.dp)
    ) {


        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .graphicsLayer {

                    if (overScrollY == 0f) {


                        translationX =
                            collapseFraction * -140f

                        translationY =
                            collapseFraction * -48f

                        val collapseScale =
                            1f -
                                    collapseFraction * 0.6f

                        scaleX = collapseScale
                        scaleY = collapseScale

                    } else {


                        scaleX = avatarScale
                        scaleY = avatarScale

                        translationY =
                            avatarTranslationY
                    }
                }
        ) {

            val avatarModifier =
                Modifier
                    .size(104.dp)
                    .clip(
                        RoundedCornerShape(
                            cornerRadius.dp
                        )
                    )

            if (avatarBitmap != null) {

                Image(
                    bitmap = avatarBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = avatarModifier,
                )

            } else {

                AsyncImage(
                    model = null,
                    contentDescription = null,
                    placeholder = painterResource(
                        R.drawable.ic_avatar
                    ),
                    error = painterResource(
                        R.drawable.ic_avatar
                    ),
                    contentScale = ContentScale.Crop,
                    modifier = avatarModifier,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {

            GlassButton(
                icon = R.drawable.ic_arrow_left,
                onClick = {

                    if (overScrollY > 0f) {

                        onCloseExpanded()

                    } else {

                        navController.popBackStack()
                    }
                }
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            GlassButton(
                text = "Edit",
                onClick = {}
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                )
                .graphicsLayer {

                    alpha = contentAlpha
                }
        ) {
            Spacer(
                modifier = Modifier.height(104.dp)
            )
            Spacer(
                modifier = Modifier.height(10.dp)
            )
            Text(
                text = user?.name ?: "Unknown",
                fontFamily = SfProText,
                fontSize = 28.sp,
                color = LightBlack,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.38.sp,
                modifier = Modifier.graphicsLayer {

                    if (overScrollY == 0f) {

                        translationX =
                            collapseFraction * 60f

                        translationY =
                            if (collapseFraction > 0.5f) {

                                collapseFraction * -105f

                            } else {

                                collapseFraction * -10f
                            }

                    } else {

                        translationY =
                            overScrollY * 0.15f
                    }
                },
            )

            /*
             * ====================================================
             * LAST SEEN
             * ====================================================
             */

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = formattedLastSeenTime,
                fontFamily = SfProText,
                fontSize = 15.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.25).sp,
                modifier = Modifier.graphicsLayer {

                    if (overScrollY == 0f) {

                        translationX =
                            collapseFraction * 60f

                        translationY =
                            if (collapseFraction > 0.5f) {

                                collapseFraction * -105f

                            } else {

                                collapseFraction * -10f
                            }

                    } else {

                        translationY =
                            overScrollY * 0.15f
                    }
                },
            )

            /*
             * ====================================================
             * ACTION ROW
             * ====================================================
             */

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Box(
                modifier = Modifier.graphicsLayer {

                    alpha = contentAlpha

                    if (overScrollY > 0f) {

                        translationY =
                            overScrollY * 0.1f
                    }
                }
            ) {

                ActionRow()
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
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

//@Composable
//@Preview(showBackground = true)
//private fun UserProfileScreenPreview() {
//
//    val user = User(
//        name = "User Name",
//        phone = "9279434335",
//    )
//
//    val items = listOf(
//        UserInfoRowItem(
//            title = "мобильный",
//            text = formatPhoneNumber(
//                rawInput = user.phone
//            ),
//            textColor = Blue,
//        ),
//        UserInfoRowItem(
//            title = "имя пользователя",
//            text = "@cat",
//            textColor = Blue,
//        ),
//        UserInfoRowItem(
//            title = "день рождения",
//            text = "6 июля",
//            textColor = LightBlack,
//        ),
//        UserInfoRowItem(
//            title = "о себе",
//            text = "EYP",
//            textColor = LightBlack,
//        ),
//    )
//
//    ContentPreview(
//        user = user,
//        items = items,
//    )
//}

@Composable
private fun ActionRow() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ActionRowItem(
            icon = R.drawable.ic_phone,
            title = "звонок",
            onClick = {},
        )
        ActionRowItem(
            icon = R.drawable.ic_camera,
            title = "видео",
            onClick = {},
        )
        ActionRowItem(
            icon = R.drawable.ic_bell,
            title = "звук",
            onClick = {},
        )
        ActionRowItem(
            icon = R.drawable.ic_search_action_row,
            title = "поиск",
            onClick = {},
        )
        ActionRowItem(
            icon = R.drawable.ic_ellipsis,
            title = "ещё",
            onClick = {},
        )
    }
}


@Composable
private fun ActionRowItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(60.dp)
            .width(67.6.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.9f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = Color.White,
            )
            .clickable {
                onClick()
            }
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    vertical = 10.dp
                )
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Blue
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                fontFamily = SfProText,
                color = Blue,
            )
        }

    }
}

@Composable
private fun ChatDataRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(41.dp)
            .padding(horizontal = 16.dp)
            .clip(
                shape = CircleShape
            )
            .background(
                brush = GlassBackground,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape
            )
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 2.dp),
    ) {
        ChatDataItem(
            text = "Posts",
            isSelected = true,
        )
        ChatDataItem(
            text = "Gifts",
            isSelected = false,
        )
        ChatDataItem(
            text = "Media",
            isSelected = false,
        )
        ChatDataItem(
            text = "Files",
            isSelected = false,
        )
        ChatDataItem(
            text = "Music",
            isSelected = false,
        )
    }
}

@Composable
private fun ChatDataItem(
    text: String,
    isSelected: Boolean,
) {

    val boxColor = if (isSelected) LightGrayBackground else Color.Transparent

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(35.dp)
            .clip(
                shape = CircleShape,
            )
            .background(
                color = boxColor,
            )
            .padding(horizontal = 19.dp)
    ) {
        Text(
            text = text,
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = LightBlack,
            letterSpacing = (-0.08).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,

            )
    }
}