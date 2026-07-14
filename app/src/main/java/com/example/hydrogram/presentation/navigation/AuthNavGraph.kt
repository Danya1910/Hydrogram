package com.example.hydrogram.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.hydrogram.presentation.screens.EmailRegistrationScreen
import com.example.hydrogram.presentation.screens.NameInputScreen
import com.example.hydrogram.presentation.screens.PasswordInputScreen
import com.example.hydrogram.presentation.screens.PhoneRegistrationScreen
import com.example.hydrogram.presentation.viewModel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.S)
fun NavGraphBuilder.AuthNavGraph(
    navController: NavController,
) {
    composable(route = Screen.PhoneRegistration.route) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("auth_graph")
        }
        val authViewModel: AuthViewModel = hiltViewModel(parentEntry)

        PhoneRegistrationScreen(
            authViewModel = authViewModel,
            navController = navController,
        )
    }

    composable(route = Screen.EmailRegistration.route) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("auth_graph")
        }
        val authViewModel: AuthViewModel = hiltViewModel(parentEntry)

        EmailRegistrationScreen(
            authViewModel = authViewModel,
            navController = navController,
        )
    }

    composable(route = Screen.NameInput.route) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("auth_graph")
        }
        val authViewModel: AuthViewModel = hiltViewModel(parentEntry)

        NameInputScreen(
            authViewModel = authViewModel,
            navController = navController,
        )
    }

    composable(route = Screen.PasswordInput.route) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("auth_graph")
        }
        val authViewModel: AuthViewModel = hiltViewModel(parentEntry)

        PasswordInputScreen(
            authViewModel = authViewModel,
            navController = navController,
        )
    }
}