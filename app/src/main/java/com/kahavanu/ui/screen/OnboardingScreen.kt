package com.kahavanu.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kahavanu.R
import com.kahavanu.ui.component.AppDecorativeGradientOverlay
import com.kahavanu.ui.component.AppPrimaryButton
import com.kahavanu.ui.theme.OnboardingTokens
import com.kahavanu.ui.theme.Spacing

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppDecorativeGradientOverlay(
                modifier = Modifier.offset(
                    x = OnboardingTokens.gradientLeftOffset,
                    y = OnboardingTokens.gradientTopOffset,
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.large, vertical = Spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(Spacing.extraSmall))

                Image(
                    painter = painterResource(id = R.drawable.kahavanu_logo),
                    contentDescription = "Kahavanu logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .height(60.dp),
                )

                Spacer(modifier = Modifier.height(OnboardingTokens.headerBottomSpacing))

                Image(
                    painter = painterResource(id = R.drawable.money_tree),
                    contentDescription = "Money Tree",
                    modifier = Modifier
                        .fillMaxSize()
                        .height(400.dp),
                )

                Spacer(modifier = Modifier.height(Spacing.extraLarge))

                Text(
                    text = "Discipline builds wealth",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Automate your finances, achieve your goals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(Spacing.extraLarge))

                Spacer(modifier = Modifier.weight(1f))

                AppPrimaryButton(
                    text = "Get started",
                    onClick = onGetStarted,
                )

                Spacer(modifier = Modifier.height(OnboardingTokens.buttonBottomSpacing))
            }
        }
    }
}