package com.example.hydrogram.presentation.navigation

sealed interface Screen {

    val route: String

    data object PhoneRegistration : Screen {
        override val route = "PhoneRegistration"
    }

    data object EmailRegistration : Screen {
        override val route = "EmailRegistration"
    }

    data object NameInput : Screen {
        override val route = "NameInput"
    }

    data object PasswordInput : Screen {
        override val route = "PasswordInput"
    }

    data object ChatList : Screen {
        override val route = "ChatList"
    }

    data object Settings : Screen {
        override val route = "Settings"
    }

    data object Chat : Screen {
        override val route = "Chat"
    }


}