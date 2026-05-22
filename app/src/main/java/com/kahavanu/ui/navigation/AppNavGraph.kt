package com.kahavanu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kahavanu.ui.auth.AuthChoiceScreen
import com.kahavanu.ui.auth.AuthViewModel
import com.kahavanu.ui.auth.LoginScreen
import com.kahavanu.ui.auth.SignupScreen
import com.kahavanu.ui.onboarding.OnboardingScreen
import com.kahavanu.ui.main.MainTabsScreen
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String,
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Single source of truth for auth-state-driven navigation.
    LaunchedEffect(isAuthenticated) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (isAuthenticated && currentRoute != AppDestination.Home.route) {
            navController.navigate(AppDestination.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!isAuthenticated && currentRoute == AppDestination.Home.route) {
            navController.navigate(AppDestination.AuthChoice.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(animationSpec = tween(500)) { offset -> offset } + fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = tween(500)) { offset -> -offset } + fadeOut(animationSpec = tween(500))
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = tween(500)) { offset -> -offset } + fadeIn(animationSpec = tween(500))
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = tween(500)) { offset -> offset } + fadeOut(animationSpec = tween(500))
        }
    ) {
        composable(AppDestination.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(AppDestination.AuthChoice.route) {
                        popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppDestination.AuthChoice.route) {
            AuthChoiceScreen(
                onCreateAccount = { navController.navigate(AppDestination.Signup.route) },
                onLogin = { navController.navigate(AppDestination.Login.route) },
                viewModel = authViewModel,
            )
        }
        composable(AppDestination.Login.route) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                viewModel = authViewModel,
            )
        }
        composable(AppDestination.Signup.route) {
            SignupScreen(
                onBack = { navController.popBackStack() },
                viewModel = authViewModel,
            )
        }
        composable(AppDestination.Home.route) {
            MainTabsScreen(
                navController = rememberNavController(),
                currentSession = currentUser,
                onSignOut = { authViewModel.signOut() },
            )
        }
    }
}
