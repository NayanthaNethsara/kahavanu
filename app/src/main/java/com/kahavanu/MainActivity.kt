package com.kahavanu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kahavanu.di.AppContainer
import com.kahavanu.ui.auth.AuthViewModel
import com.kahavanu.ui.auth.AuthViewModelFactory
import com.kahavanu.ui.navigation.AppDestination
import com.kahavanu.ui.navigation.AppNavGraph
import com.kahavanu.ui.theme.KahavanuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KahavanuTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(AppContainer.authRepository),
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
                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        startDestination = startDestination,
                    )
                }
            }
        }
    }
}
