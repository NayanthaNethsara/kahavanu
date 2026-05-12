package com.kahavanu.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.kahavanu.R
import androidx.compose.ui.text.font.FontWeight
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import androidx.compose.ui.unit.dp

@Composable
fun AuthScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = RawColors.Slate.Slate50,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppDecorativeGradientOverlay(
                modifier = Modifier.offset(
                    x = 96.dp,
                    y = 80.dp,
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.extraLarge),
                horizontalAlignment = Alignment.Start,
            ) {
                Spacer(modifier = Modifier.height(Spacing.huge))

                TextButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = RawColors.Slate.Slate600,
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Back",
                        color = RawColors.Slate.Slate600,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kahavanu_logo),
                        contentDescription = "Kahavanu logo",
                        modifier = Modifier.height(64.dp),
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.huge))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = RawColors.Slate.Slate900,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RawColors.Slate.Slate600,
                )

                Spacer(modifier = Modifier.height(Spacing.huge))

                content()

                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
}
