package com.example.hydrogram.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.hydrogram.presentation.viewModel.AuthViewModel
import com.example.hydrogram.presentation.screens.PhoneRegistrationScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.hydrogram.presentation.screens.ChatListScreen
import com.example.hydrogram.presentation.screens.ChatScreen
import com.example.hydrogram.presentation.screens.ContactsScreen
import com.example.hydrogram.presentation.screens.EmailRegistrationScreen
import com.example.hydrogram.presentation.screens.SettingsScreen
import com.example.hydrogram.presentation.screens.UserProfileScreen
import com.example.hydrogram.presentation.viewModel.ChatViewModel
import com.example.hydrogram.presentation.viewModel.InboxViewModel
import com.example.hydrogram.presentation.viewModel.SearchViewModel
import com.example.hydrogram.presentation.viewModel.UserViewModel

@RequiresApi(Build.VERSION_CODES.S)
fun NavGraphBuilder.MainNavGraph(
    navController: NavController,
) {
    composable(route = Screen.ContactsScreen.route) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("main_graph")
        }
        val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)

        ContactsScreen(
            searchViewModel = searchViewModel,
            navController = navController,
        )
    }

    composable(
        route = Screen.Chat.route,
        arguments = listOf(
            navArgument("id") {type = NavType.StringType}
        )
    ) { backStackEntry ->
        val chatViewModel: ChatViewModel = hiltViewModel()
        val userViewModel: UserViewModel = hiltViewModel()
        val penpalId = backStackEntry.arguments?.getString("id") ?: ""

        ChatScreen(
            chatViewModel = chatViewModel,
            userViewModel = userViewModel,
            navController = navController,
            penpalId = penpalId,
        )
    }

    composable(route = Screen.ChatList.route) { backStackEntry ->
        val inboxViewModel: InboxViewModel = hiltViewModel()

        ChatListScreen(
            inboxViewModel = inboxViewModel,
            navController = navController,
        )
    }

    composable(route = Screen.Settings.route) { backStackEntry ->
        val userViewModel: UserViewModel = hiltViewModel()

        SettingsScreen(
            userViewModel = userViewModel,
            navController = navController,
        )
    }

    composable(
        route = Screen.UserProfile.route,
        arguments = listOf(
            navArgument("id") {type = NavType.StringType}
        )
    ) { backStackEntry ->
        val userViewModel: UserViewModel = hiltViewModel(backStackEntry)
        val userId = backStackEntry.arguments?.getString("id") ?: ""

        UserProfileScreen(
            userViewModel = userViewModel,
            navController = navController,
            userId = userId,
        )
    }

}