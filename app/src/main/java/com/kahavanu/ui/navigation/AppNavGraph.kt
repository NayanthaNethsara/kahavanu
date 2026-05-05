package com.kahavanu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kahavanu.ui.auth.AuthChoiceScreen
import com.kahavanu.ui.auth.AuthViewModel
import com.kahavanu.ui.auth.LoginScreen
import com.kahavanu.ui.auth.SignupScreen
import com.kahavanu.ui.home.HomeScreen
import com.kahavanu.ui.onboarding.OnboardingScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String,
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(AppDestination.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(AppDestination.AuthChoice.route) {
                        popUpTo(AppDestination.Onboarding.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppDestination.AuthChoice.route) {
            AuthChoiceScreen(
                onCreateAccount = {
                    navController.navigate(AppDestination.Signup.route)
                },
                onLogin = {
                    navController.navigate(AppDestination.Login.route)
                },
                onAuthSuccess = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.AuthChoice.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                viewModel = authViewModel,
            )
        }
        composable(AppDestination.Login.route) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onAuthSuccess = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.AuthChoice.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                viewModel = authViewModel,
            )
        }
        composable(AppDestination.Signup.route) {
            SignupScreen(
                onBack = { navController.popBackStack() },
                onAuthSuccess = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.AuthChoice.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                viewModel = authViewModel,
            )
        }
        composable(AppDestination.Home.route) {
            if (isAuthenticated) {
                HomeScreen(
                    onLogout = {
                        navController.navigate(AppDestination.AuthChoice.route) {
                            popUpTo(AppDestination.Home.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(AppDestination.AuthChoice.route) {
                        popUpTo(AppDestination.Home.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}
