package com.example.hydrogram.presentation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hydrogram.R
import com.example.hydrogram.presentation.viewModel.AuthViewModel
import com.example.hydrogram.presentation.widgets.SeparatorLine
import com.example.hydrogram.ui.theme.Blue
import com.example.hydrogram.ui.theme.Separator
import com.example.hydrogram.ui.theme.SfProText


@Composable
fun PasswordInputScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
) {
    Scaffold(
        topBar = {},
    ) { paddingValues ->
        Content(
            authViewModel = authViewModel,
            navController = navController,
            paddingValues = paddingValues,
        )
    }
}

@Composable
private fun Content(
    authViewModel: AuthViewModel,
    navController: NavController,
    paddingValues: PaddingValues,
) {

    var password by remember { mutableStateOf("") }

    val isAvailable = password.length >= 8

    LaunchedEffect(Unit) {
        Log.d(
            "Password Input screen", "phone: ${authViewModel.authData.value.phone}," +
                    " email ${authViewModel.authData.value.email}"
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
            .padding(
                horizontal = 16.dp,
                vertical = 26.dp,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.blue_key),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(200.dp),
            )
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "Пароль",
                fontWeight = FontWeight.Bold,
                fontFamily = SfProText,
                fontSize = 26.sp,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Введите пароль, по которому вы будете заходить в аккаунт.",
                fontWeight = FontWeight.Normal,
                fontFamily = SfProText,
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            InputPasswordField(
                password = password,
                onValueChange = {
                    password = it
                },
            )
        }

        AcceptButton(
            isAvailable = false,
            onClick = {
                authViewModel.signUp(
                    email = authViewModel.authData.value.email,
                    password = password,
                    name = authViewModel.authData.value.name,
                    phone = authViewModel.authData.value.phone,
                )
            },
        )
    }
}

@Composable
private fun InputPasswordField(
    password: String,
    onValueChange: (String) -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = password,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = SfProText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    if (password.isEmpty()) {
                        Text(
                            text = "Введите пароль",
                            fontFamily = SfProText,
                            fontSize = 17.sp,
                            color = Color.Gray.copy(alpha = 0.8f)
                        )
                    }
                    innerTextField()
                }
            }
        )
        SeparatorLine(
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun AcceptButton(
    isAvailable: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                color = if (isAvailable) Blue else Separator,
                shape = CircleShape
            )
            .border(
                color = if (isAvailable) Blue else Separator,
                width = 1.dp,
                shape = CircleShape,
            )
    ) {
        Text(
            text = "Применить",
            fontFamily = SfProText,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = if (isAvailable) Color.White else Color.Black,
        )
    }

}

@Composable
@Preview(showBackground = true)
private fun PasswordInputScreenPreview() {
}