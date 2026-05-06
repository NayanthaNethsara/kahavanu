package com.kahavanu.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.kahavanu.R
import com.kahavanu.ui.common.AppDecorativeGradientOverlay
import com.kahavanu.ui.common.AppPrimaryButton
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
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painter = painterResource(id = R.drawable.kahavanu_logo),
                    contentDescription = "Kahavanu logo",
                    modifier = Modifier
                        .width(OnboardingTokens.headerWidth)
                        .height(OnboardingTokens.headerHeight),
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.money_tree),
                        contentDescription = "Money Tree",
                        modifier = Modifier
                            .width(OnboardingTokens.imageWidth)
                            .height(OnboardingTokens.imageHeight),
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

                    Spacer(modifier = Modifier.height(Spacing.small))

                    Text(
                        text = "Automate your finances, achieve your goals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                AppPrimaryButton(
                    text = "Get started",
                    onClick = onGetStarted,
                )

                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
}
