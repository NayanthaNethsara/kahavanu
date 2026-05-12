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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.R
import com.kahavanu.ui.common.AppDecorativeGradientOverlay
import com.kahavanu.ui.common.AppPrimaryButton
import com.kahavanu.ui.theme.ButtonTokens
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = RawColors.Slate.Slate50,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative Blobs
            AppDecorativeGradientOverlay(
                modifier = Modifier
                    .offset(x = (-40).dp, y = (-20).dp)
                    .size(300.dp),
            )
            
            AppDecorativeGradientOverlay(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 60.dp, y = 40.dp)
                    .size(350.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = Spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1.2f))

                Image(
                    painter = painterResource(id = R.drawable.kahavanu_logo),
                    contentDescription = "Kahavanu logo",
                    modifier = Modifier
                        .width(128.dp)
                        .height(64.dp),
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.money_tree),
                        contentDescription = "Money Tree",
                        modifier = Modifier
                            .width(300.dp)
                            .height(400.dp),
                    )

                    Spacer(modifier = Modifier.height(Spacing.massive))

                    Text(
                        text = "Discipline builds wealth",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(Spacing.small))

                    Text(
                        text = "Automate your finances and achieve your goals with precision",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                    )
                }

                Spacer(modifier = Modifier.weight(1.5f))

                AppPrimaryButton(
                    text = "Get started",
                    onClick = onGetStarted,
                )

                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
}
