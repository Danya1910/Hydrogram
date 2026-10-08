package com.example.hydrogram

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.hydrogram.presentation.navigation.RootNavGraph
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pendingChatId = mutableStateOf<String?>(null)

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val auth = Firebase.auth
        val currentUser = auth.currentUser
        val startDescription = if (currentUser != null) "main_graph" else "auth_graph"

        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            RequestNotificationPermission()

            val navController = rememberNavController()

            RootNavGraph(
                startDescription = startDescription,
                navController = navController,
                pendingChatId = pendingChatId.value,
                onPendingChatNavigated = { pendingChatId.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.hasExtra("CHAT_ID")) {
                val chatId = it.getStringExtra("CHAT_ID")

                Log.d("FCM_RAW_CHECK", "СЫРОЙ ID ИЗ УВЕДОМЛЕНИЯ: '$chatId'")

                if (!chatId.isNullOrBlank()) {
                    pendingChatId.value = chatId

                    it.removeExtra("CHAT_ID")
                }
            }
        }
    }
}


@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current

    // Создаем лаунчер для системного диалога запроса разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Пользователь разрешил уведомления
        } else {
            // Пользователь отказал в доступе
        }
    }

    // Запускаем проверку один раз при старте экрана
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
