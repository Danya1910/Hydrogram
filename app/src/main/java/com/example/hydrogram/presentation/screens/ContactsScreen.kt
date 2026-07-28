package com.example.hydrogram.presentation.screens

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.RegisteredContact
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.domain.usecase.GetPhoneContactsUseCase
import com.example.hydrogram.presentation.navigation.Screen
import com.example.hydrogram.presentation.states.SearchState
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.viewModel.SearchViewModel
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.LightBlack
import com.example.hydrogram.ui.theme.SfProText


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
    val context = LocalContext.current

    val contacts by searchViewModel.registeredContact.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            searchViewModel.syncContacts()
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
        if (query.length >= 5) {
            searchViewModel.searchByPhoneOrUserName(
                query = query,
            )
        }
    }

    val foundUserState by searchViewModel.searchState.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
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

        ContactsList(
            contacts = contacts,
            navController = navController,
        )

        Spacer(modifier = Modifier.height(10.dp))
        when (val state = foundUserState) {
            is SearchState.Loading -> {
                CircularProgressIndicator()
            }

            is SearchState.Error -> {
                Text(text = state.message, color = Color.Red)
            }

            is SearchState.Success -> {
                val user = state.user

                if (user != null) {
                    UserCard(
                        user = user,
                        onUserClick = {
                            navController.navigate(Screen.Chat.createRoute(id = user.uid))
                        }
                    )
                } else {
                    Text("Пользователь не найден")
                }
            }
        }

    }
}

@Composable
private fun UserCard(
    user: User?,
    onUserClick: () -> Unit,
) {

    val isOnline = user?.isOnline?.let {
        if (user.isOnline) "онлайн" else "был(а) недавно"
    } ?: "был(а) недавно"

    val onlineTextColor = user?.isOnline?.let {
        if (user.isOnline) Blue else Color(0xFF3C3C43).copy(alpha = 0.6f)
    } ?: Color(0xFF3C3C43).copy(alpha = 0.6f)


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
        AsyncImage(
            model = user?.avatarUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_avatar),
            error = painterResource(R.drawable.ic_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(42.dp)
                .clip(shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(11.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = user?.name ?: "Unknown",
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
private fun ContactsList(
    contacts: List<RegisteredContact>,
    navController: NavController,
) {
    LazyColumn() {
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

    val isOnline = contact?.user?.isOnline?.let {
        if (contact.user.isOnline) "онлайн" else "был(а) недавно"
    } ?: "был(а) недавно"

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
    Box(
        modifier = Modifier
            .height(42.dp)
            .fillMaxWidth()
            .background(
                brush = GlassBackground,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                shape = CircleShape,
                brush = GlassBorder,
            )
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
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Поиск",
                                fontFamily = SfProText,
                                fontSize = 17.sp,
                                color = Color.Gray.copy(alpha = 0.8f)
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
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
private fun SortButton(
    title: String,
    onClick: () -> Unit,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .width(67.dp)
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