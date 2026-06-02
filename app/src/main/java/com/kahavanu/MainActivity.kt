package com.kahavanu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.kahavanu.ui.auth.AuthViewModel
import com.kahavanu.ui.navigation.AppDestination
import com.kahavanu.ui.navigation.AppNavGraph
import com.kahavanu.ui.theme.KahavanuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* user choice respected */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        setContent {
            KahavanuTheme {
                val navController = rememberNavController()
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

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
