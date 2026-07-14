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
import com.example.hydrogram.presentation.screens.ContactsScreen
import com.example.hydrogram.presentation.screens.EmailRegistrationScreen
import com.example.hydrogram.presentation.viewModel.SearchViewModel

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
}