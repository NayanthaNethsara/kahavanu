package com.kahavanu.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.kahavanu.domain.model.UserSession
import com.kahavanu.ui.common.BottomNavBar
import com.kahavanu.ui.common.TopAppHeader
import com.kahavanu.ui.expenses.ExpenseGraphScreen
import com.kahavanu.ui.expenses.ExpenseHistoryScreen
import com.kahavanu.ui.expenses.ExpenseLogScreen
import com.kahavanu.ui.expenses.ExpensesScreen
import com.kahavanu.ui.goals.GoalsScreen
import com.kahavanu.ui.goals.GoalSetupScreen
import com.kahavanu.ui.goals.GoalStatsScreen
import com.kahavanu.ui.home.HomeScreen
import com.kahavanu.ui.income.IncomeLogScreen
import com.kahavanu.ui.income.IncomeHistoryScreen
import com.kahavanu.ui.income.IncomeScreen
import com.kahavanu.ui.income.RecurringManagerScreen
import com.kahavanu.ui.income.IncomeSourcesScreen
import com.kahavanu.ui.income.HistoryFilter
import com.kahavanu.ui.navigation.AppDestination
import com.kahavanu.ui.profile.ProfileScreen

@Composable
fun MainTabsScreen(
    navController: NavHostController,
    currentSession: UserSession?,
    onSignOut: () -> Unit,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf(
        AppDestination.Home.route,
        AppDestination.Income.route,
        AppDestination.Expenses.route,
        AppDestination.Goals.route,
        AppDestination.Profile.route,
    )
    val showTopBar = currentRoute in setOf(
        AppDestination.Home.route,
        AppDestination.Income.route,
        AppDestination.Expenses.route,
        AppDestination.Goals.route,
        AppDestination.Profile.route,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showTopBar) {
                TopAppHeader(
                    currentSession = currentSession,
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(AppDestination.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(AppDestination.Home.route) {
                HomeScreen(
                    currentSession = currentSession,
                )
            }
            composable(AppDestination.Income.route) {
                IncomeScreen(
                    onLogIncome = { navController.navigate(AppDestination.IncomeLog.route) },
                    onViewRecurrents = { navController.navigate(AppDestination.IncomeRecurrents.route) },
                    onViewPersistence = { navController.navigate(AppDestination.IncomeHistory.createRoute("PENDING")) },
                    onViewHistory = { navController.navigate(AppDestination.IncomeHistory.createRoute("ALL")) }
                )
            }
            composable(AppDestination.IncomeRecurrents.route) {
                RecurringManagerScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppDestination.IncomeLog.route) {
                IncomeLogScreen(
                    onBack = { navController.popBackStack() },
                    onLogged = { navController.popBackStack() },
                    onManageSources = { navController.navigate(AppDestination.IncomeSources.route) },
                )
            }
            composable(AppDestination.IncomeSources.route) {
                IncomeSourcesScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.Expenses.route) {
                ExpensesScreen(
                    onLogExpense = { navController.navigate(AppDestination.ExpenseLog.route) },
                    onViewBills = { navController.navigate(AppDestination.ExpenseHistory.route) },
                    onViewBudgets = { navController.navigate(AppDestination.ExpenseGraph.route) },
                    onViewHistory = { navController.navigate(AppDestination.ExpenseHistory.route) },
                )
            }
            composable(AppDestination.ExpenseLog.route) {
                ExpenseLogScreen(
                    onBack = { navController.popBackStack() },
                    onLogged = { navController.popBackStack() },
                )
            }
            composable(AppDestination.ExpenseHistory.route) {
                ExpenseHistoryScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.ExpenseGraph.route) {
                ExpenseGraphScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.Goals.route) {
                GoalsScreen(
                    onAddGoal = { navController.navigate(AppDestination.GoalSetup.route) },
                    onViewStats = { navController.navigate(AppDestination.GoalStats.route) },
                )
            }
            composable(AppDestination.GoalSetup.route) {
                GoalSetupScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(AppDestination.GoalStats.route) {
                GoalStatsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.Profile.route) {
                ProfileScreen()
            }
            composable(
                route = AppDestination.IncomeHistory.route,
                arguments = listOf(navArgument("filter") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val filterStr = backStackEntry.arguments?.getString("filter")
                val filter = try {
                    if (filterStr != null) HistoryFilter.valueOf(filterStr) else HistoryFilter.ALL
                } catch (e: Exception) {
                    HistoryFilter.ALL
                }
                IncomeHistoryScreen(
                    onBack = { navController.popBackStack() },
                    initialFilter = filter
                )
            }
        }
    }
}
