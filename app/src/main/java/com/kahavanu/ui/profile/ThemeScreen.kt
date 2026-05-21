package com.kahavanu.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kahavanu.ui.theme.Spacing

@Composable
fun ThemeScreen(
    onBack: () -> Unit,
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.extraLarge),
        ) {
            ProfileSubScreenHeader(
                title = "Theme",
                onBack = onBack,
            )
        }
    }
}
