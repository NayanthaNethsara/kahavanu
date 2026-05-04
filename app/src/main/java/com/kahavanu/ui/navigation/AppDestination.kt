package com.kahavanu.ui.navigation

sealed class AppDestination(val route: String) {
    data object Onboarding : AppDestination("onboarding")
    data object Home : AppDestination("home")
    data object Login : AppDestination("login")
}
