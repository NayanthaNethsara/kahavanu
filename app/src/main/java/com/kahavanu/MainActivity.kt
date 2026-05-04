package com.kahavanu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kahavanu.ui.navigation.AppDestination
import com.kahavanu.ui.screen.HomeScreen
import com.kahavanu.ui.screen.OnboardingScreen
import com.kahavanu.ui.theme.KahavanuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KahavanuTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = AppDestination.Onboarding.route,
                    ) {
                        composable(AppDestination.Onboarding.route) {
                            OnboardingScreen(
                                onGetStarted = {
                                    navController.navigate(AppDestination.Home.route) {
                                        popUpTo(AppDestination.Onboarding.route) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                        composable(AppDestination.Home.route) {
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
}

