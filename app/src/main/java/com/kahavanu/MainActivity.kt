package com.kahavanu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kahavanu.data.auth.AuthRepositoryProvider
import com.kahavanu.ui.auth.AuthViewModel
import com.kahavanu.ui.auth.AuthViewModelFactory
import com.kahavanu.ui.navigation.AppDestination
import com.kahavanu.ui.screen.AuthChoiceScreen
import com.kahavanu.ui.screen.HomeScreen
import com.kahavanu.ui.screen.LoginScreen
import com.kahavanu.ui.screen.OnboardingScreen
import com.kahavanu.ui.screen.SignupScreen
import com.kahavanu.ui.theme.KahavanuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KahavanuTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(AuthRepositoryProvider.repository),
                )
                val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()

                val startDestination = if (isAuthenticated) {
                    AppDestination.Home.route
                } else {
                    AppDestination.Onboarding.route
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                ) {
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
            }
        }
    }
}

