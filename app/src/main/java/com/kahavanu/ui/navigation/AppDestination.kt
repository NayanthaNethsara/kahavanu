package com.kahavanu.ui.navigation

sealed class AppDestination(val route: String) {
    data object Onboarding : AppDestination("onboarding")
    data object AuthChoice : AppDestination("auth-choice")
    data object Login : AppDestination("login")
    data object Signup : AppDestination("signup")
    data object Home : AppDestination("home")
    data object Income : AppDestination("income")
    data object IncomeLog : AppDestination("income-log")
    data object IncomeSources : AppDestination("income-sources")
    data object Expenses : AppDestination("expenses")
    data object Goals : AppDestination("goals")
    data object Profile : AppDestination("profile")
    data object PersistenceList : AppDestination("persistence-list")
}
