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
    data object IncomeRecurrents : AppDestination("income-recurrents")
    data object Expenses : AppDestination("expenses")
    data object ExpenseLog : AppDestination("expense-log")
    data object ExpenseHistory : AppDestination("expense-history")
    data object ExpenseGraph : AppDestination("expense-graph")
    data object Goals : AppDestination("goals")
    data object GoalSetup : AppDestination("goal-setup")
    data object GoalStats : AppDestination("goal-stats")
    data object Profile : AppDestination("profile")
    data object SmsSenderSettings : AppDestination("sms-sender-settings")
    data object ManageSubscriptions : AppDestination("manage-subscriptions")
    data object HelpSupport : AppDestination("help-support")
    data object IncomeHistory : AppDestination("income-history?filter={filter}") {
        fun createRoute(filter: String? = null) = if (filter != null) "income-history?filter=$filter" else "income-history"
    }
}
