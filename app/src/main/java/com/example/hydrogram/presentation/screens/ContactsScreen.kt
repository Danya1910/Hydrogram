package com.example.hydrogram.presentation.screens

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.RegisteredContact
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.SearchState
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.util.formatLastSeen
import com.example.hydrogram.presentation.viewModel.SearchViewModel
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.LightGrayBackground
import com.example.hydrogram.ui.theme.SelectedItem
import com.example.hydrogram.ui.theme.SfProText
import kotlinx.coroutines.delay


@Composable
fun ContactsScreen(
    searchViewModel: SearchViewModel,
    navController: NavController,
) {

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(
                navController = navController,
            )
        }
    ) { paddingValues ->
        Content(
            searchViewModel = searchViewModel,
            navController = navController,
            paddingValues = paddingValues,
        )
    }
}

@Composable
private fun Content(
    searchViewModel: SearchViewModel,
    navController: NavController,
    paddingValues: PaddingValues,
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val contacts by searchViewModel.registeredContact.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            searchViewModel.syncContacts()
        }
    }

    var filteredContacts = remember(query, contacts) {
        if (query.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.contactName.contains(query, ignoreCase = true) ||
                        contact.user.phone.contains(query)
            }
        }
    }

    LaunchedEffect(filteredContacts) {
        Log.d("ContactsScreen", "filtered contacts: $filteredContacts")
    }

    LaunchedEffect(contacts) {
        if (contacts.isNotEmpty()) {
            Log.d("ContactsScreen", "Всего контактов загружено: ${contacts.size}")

            contacts.forEachIndexed { index, contact ->
                Log.d("ContactsScreen", "contact[$index] user = : ${contact.user.name}")
                Log.d("ContactsScreen", "contact[$index].isOnline: ${contact.user.isOnline}")
                Log.d("ContactsScreen", "contact[$index].lastSeen: ${contact.user.lastSeen}")
            }
        } else {
            Log.d("ContactsScreen", "Список контактов пока пуст (загрузка...)")
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            searchViewModel.syncContacts()
        } else {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }

    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            delay(400L)
            searchViewModel.searchByPhoneOrUserName(
                query = query,
            )
        } else {
            searchViewModel.resetSearch()
        }
    }

    val foundUserState by searchViewModel.searchState.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(
                paddingValues = paddingValues
            )
    ) {
        SearchField(
            value = query,
            onValueChange = {
                query = it
            }
        )
        Spacer(modifier = Modifier.height(10.dp))

        val globalUsers = (foundUserState as? SearchState.Success)?.users ?: emptyList()

        if ((filteredContacts.isEmpty() && globalUsers.isEmpty()) || query.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_friend),
                    contentDescription = null,
                    tint = Blue,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Пригласить",
                    fontFamily = SfProText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = Blue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.43).sp
                )
            }
            SeparatorLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 82.dp,
                        end = 16.dp,
                    )
            )
            ContactsList(
                contacts = contacts,
                navController = navController,
            )
        } else {
            Spacer(modifier = Modifier.height(10.dp))
            if (filteredContacts.isNotEmpty() && query.isNotEmpty()) {
                ContactsMatchingList(
                    contacts = filteredContacts,
                    navController = navController,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            when (val state = foundUserState) {
                is SearchState.Loading -> {
                    CircularProgressIndicator()
                }

                is SearchState.Error -> {
                    Text(text = state.message, color = Color.Red)
                }

                is SearchState.Success -> {
                    val users = state.users

                    if (users.isNotEmpty()) {
                        GlobalSearchedList(
                            users = users,
                            navController = navController,
                        )
                    }
                }
            }
        }

    }
}


@Composable
private fun ContactsList(
    contacts: List<RegisteredContact>,
    navController: NavController,
) {
    LazyColumn {
        itemsIndexed(
            items = contacts,
            key = { _, state -> state.user.uid }
        ) { index, contact ->
            ContactUserCard(
                contact = contact,
                onUserClick = {
                    navController.navigate(Screen.Chat.createRoute(id = contact.user.uid))
                }
            )
            if (index != contacts.size - 1) {
                SeparatorLine(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 82.dp,
                            end = 16.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun ContactUserCard(
    contact: RegisteredContact?,
    onUserClick: () -> Unit,
) {

    var tick by remember { mutableStateOf(0) }

    if (contact?.user?.isOnline == false) {
        LaunchedEffect(contact.user.uid) {
            while (true) {
                delay(30_000L)
                tick++
            }
        }
    }

    val isOnline = remember(contact?.user?.isOnline, contact?.user?.lastSeen, tick) {
        contact?.user?.isOnline?.let { isOnline ->
            if (isOnline) {
                "онлайн"
            } else {
                formatLastSeen(lastSeenTimestamp = contact.user.lastSeen)
            }
        } ?: "был(а) недавно"
    }

    val onlineTextColor = contact?.user?.isOnline?.let {
        if (contact.user.isOnline) Blue else Color(0xFF3C3C43).copy(alpha = 0.6f)
    } ?: Color(0xFF3C3C43).copy(alpha = 0.6f)


    val avatarBitmap = remember(contact?.user?.avatarUrl) {
        val url = contact?.user?.avatarUrl
        if (url != null && url.isNotBlank() && url.startsWith("data:image/jpeg;base64,")) {
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                onUserClick()
            }
    ) {
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
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
                    .size(42.dp)
                    .clip(shape = CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(11.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = contact?.contactName ?: "Без имени",
                fontFamily = SfProText,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.43).sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = isOnline,
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = onlineTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.23).sp
            )
        }
    }
}


@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {

    var isTextFieldActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current


    val textFieldAnimation by animateDpAsState(
        targetValue = if (isTextFieldActive) 58.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val textFieldAlpha by animateFloatAsState(
        targetValue = if (isTextFieldActive) 0f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    val textAlignment by animateFloatAsState(
        targetValue = if (isTextFieldActive) -1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    val shadowAnimation by animateDpAsState(
        targetValue = if (isTextFieldActive) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(end = textFieldAnimation)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = SfProText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isTextFieldActive = focusState.isFocused
                    }
                    .shadow(
                        elevation = shadowAnimation,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.9f),
                        spotColor = Color.Black.copy(alpha = 0.2f),
                    )
                    .clip(
                        shape = CircleShape,
                    )
                    .background(
                        color = LightGrayBackground,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        shape = CircleShape,
                        brush = GlassBorder,
                    ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .wrapContentWidth()
                                .fillMaxHeight()
                                .align(
                                    BiasAlignment(
                                        horizontalBias = textAlignment,
                                        verticalBias = 0f
                                    )
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_text_field_search),
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .wrapContentSize()
                            ) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = "Поиск",
                                        fontFamily = SfProText,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray.copy(alpha = 0.8f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                }
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .graphicsLayer(alpha = textFieldAlpha)
                    .clip(
                        shape = CircleShape,
                    )
                    .background(
                        color = SelectedItem,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        shape = CircleShape,
                        color = SelectedItem,
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusRequester.requestFocus()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxHeight()
                        .align(
                            BiasAlignment(
                                horizontalBias = textAlignment,
                                verticalBias = 0f
                            )
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_text_field_search),
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                    ) {
                        Text(
                            text = "Поиск",
                            fontFamily = SfProText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = isTextFieldActive,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = fadeIn(animationSpec = tween(100)) + slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(100)) + slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(42.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.9f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .clip(
                        shape = CircleShape,
                    )
                    .background(
                        brush = GlassBackground,
                        shape = CircleShape,
                    )
                    .border(
                        width = 1.dp,
                        brush = GlassBorder,
                        shape = CircleShape,
                    )
                    .clickable {
                        onValueChange("")
                        focusManager.clearFocus()
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cross),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }
        }
    }
}

@Composable
private fun TopBar(
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .statusBarsPadding()
            .height(54.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SortButton(
            title = "Сорт.",
            onClick = {},
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
        ) {
            ContactsScreenHat()
        }
        Spacer(modifier = Modifier.width(23.dp))
        AddButton(
            icon = R.drawable.ic_plus,
            onClick = {},
        )
    }
}

@Composable
private fun ContactsMatchingList(
    contacts: List<RegisteredContact>,
    navController: NavController,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "КОНТАКТЫ",
            fontFamily = SfProText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.08).sp,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn {
            itemsIndexed(
                items = contacts,
                key = { _, state -> state.user.uid }
            ) { index, contact ->
                ContactUserCard(
                    contact = contact,
                    onUserClick = {
                        navController.navigate(Screen.Chat.createRoute(id = contact.user.uid))
                    }
                )
                if (index != contacts.size - 1) {
                    SeparatorLine(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 82.dp,
                                end = 16.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalSearchedList(
    users: List<User>,
    navController: NavController,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = "ГЛОБАЛЬНЫЙ ПОИСК",
            fontFamily = SfProText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.08).sp,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn {
            itemsIndexed(
                items = users,
                key = { _, state -> state.uid }
            ) { index, user ->
                GlobalUserCard(
                    user = user,
                    onUserClick = {
                        navController.navigate(Screen.Chat.createRoute(id = user.uid))
                    }
                )
                if (index != users.size - 1) {
                    SeparatorLine(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 82.dp,
                                end = 16.dp
                            )
                    )
                }
            }
        }
    }

}


@Composable
private fun GlobalUserCard(
    user: User?,
    onUserClick: () -> Unit,
) {

    val avatarBitmap = remember(user?.avatarUrl) {
        val url = user?.avatarUrl
        if (url != null && url.isNotBlank() && url.startsWith("data:image/jpeg;base64,")) {
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                onUserClick()
            }
    ) {
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
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
                    .size(42.dp)
                    .clip(shape = CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(11.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = user?.name ?: "Без имени",
                fontFamily = SfProText,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.43).sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (user?.userName?.isNotBlank() == true) "@${user.userName}" else "",
                fontFamily = SfProText,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = Blue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.23).sp
            )
        }
    }
}


@Composable
private fun SortButton(
    title: String,
    onClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .width(67.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                clip = true,
                ambientColor = Color.Black.copy(alpha = 0.9f),
            )
            .clip(
                shape = CircleShape,
            )
            .background(
                brush = GlassBackground,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape,
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp),

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
private fun ContactsScreenHat() {
    Text(
        text = "Контакты",
        fontFamily = SfProText,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.26).sp,
        color = LightBlack,
    )
}

@Composable
private fun AddButton(
    icon: Int,
    onClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .width(44.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                clip = true,
                ambientColor = Color.Black.copy(alpha = 0.9f),
            )
            .clip(
                shape = CircleShape,
            )
            .background(
                brush = GlassBackground,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                brush = GlassBorder,
                shape = CircleShape,
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp),

        ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = LightBlack,
        )
    }

}