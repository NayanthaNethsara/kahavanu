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
import com.kahavanu.domain.model.UserSession
import com.kahavanu.ui.common.BottomNavBar
import com.kahavanu.ui.common.TopAppHeader
import com.kahavanu.ui.expenses.ExpensesScreen
import com.kahavanu.ui.goals.GoalsScreen
import com.kahavanu.ui.home.HomeScreen
import com.kahavanu.ui.income.IncomeLogScreen
import com.kahavanu.ui.income.IncomeScreen
import com.kahavanu.ui.income.IncomeSourcesScreen
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
    val showTopBar = showBottomBar

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
                ExpensesScreen()
            }
            composable(AppDestination.Goals.route) {
                GoalsScreen()
            }
            composable(AppDestination.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
