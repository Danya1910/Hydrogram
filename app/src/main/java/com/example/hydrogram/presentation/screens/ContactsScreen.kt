package com.example.hydrogram.presentation.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.hydrogram.R
import com.example.hydrogram.domain.model.User
import com.example.hydrogram.presentation.states.SearchState
import com.example.hydrogram.presentation.util.GlassBackground
import com.example.hydrogram.presentation.util.GlassBorder
import com.example.hydrogram.presentation.viewModel.SearchViewModel
import com.example.hydrogram.presentation.widgets.BottomBar
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun ContactsScreen(
    searchViewModel: SearchViewModel,
    navController: NavController,
) {
    Scaffold(
        bottomBar = {
            BottomBar(
                navController = navController,
                currentRoute = "Contacts"
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
    var phoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(phoneNumber) {
        if (phoneNumber.length == 10) {
            searchViewModel.searchByPhone(
                phone = phoneNumber,
            )
        }
    }

    val foundUserState by searchViewModel.searchState.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        SearchField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
            }
        )
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
                        onUserClick = {}
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