package com.kahavanu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.theme.AmbientGlowPrimary
import com.kahavanu.ui.theme.AmbientGlowSecondary
import com.kahavanu.ui.theme.AmbientGlowTertiary
import com.kahavanu.ui.theme.ScreenBackground
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

/**
 * Sub-screen scaffold for form / detail pages. Same ambient backdrop as
 * [KahavanuScreen], but with a back-button row + label/title header at the top
 * and a generic content slot (so callers can use Column + verticalScroll,
 * LazyColumn, or anything else — typical for forms with IME / keyboard
 * handling).
 */
@Composable
fun KahavanuSubScreen(
    label: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        AmbientGlow(
            color = AmbientGlowPrimary,
            size = 360.dp,
            modifier = Modifier.offset(x = (-96).dp, y = (-128).dp),
        )
        AmbientGlow(
            color = AmbientGlowSecondary,
            size = 320.dp,
            modifier = Modifier.offset(x = 170.dp, y = 284.dp),
        )
        AmbientGlow(
            color = AmbientGlowTertiary,
            size = 300.dp,
            modifier = Modifier.offset(x = 98.dp, y = 648.dp),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(
                        horizontal = Spacing.extraLarge,
                        vertical = Spacing.large,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    MorphingBackButton(onClick = onBack)
                    Column {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.72.sp,
                            color = TextSecondary,
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = TextSize.xl,
                            color = TextPrimary,
                            letterSpacing = (-0.8).sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                trailing?.invoke()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                content = content,
            )
        }
    }
}
