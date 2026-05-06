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
import com.kahavanu.ui.goals.GoalsScreen
import com.kahavanu.ui.home.HomeScreen
import com.kahavanu.ui.navigation.AppDestination
import com.kahavanu.ui.pipeline.PipelineScreen
import com.kahavanu.ui.profile.ProfileScreen

@Composable
fun MainTabsScreen(
    navController: NavHostController,
    currentSession: UserSession?,
    onSignOut: () -> Unit,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppHeader(
                currentSession = currentSession,
            )
        },
        bottomBar = {
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
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.Home.route) {
                HomeScreen(
                    currentSession = currentSession,
                )
            }
            composable(AppDestination.Pipeline.route) {
                PipelineScreen()
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
