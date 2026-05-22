package com.kahavanu.ui.common

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(Spacing.medium),
        snackbar = { snackbarData ->
            AppSnackbar(snackbarData)
        }
    )
}

@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        modifier = modifier
            .padding(Spacing.small)
            .shadow(
                elevation = 20.dp,
                spotColor = MaterialTheme.extendedColors.textTertiary,
                ambientColor = MaterialTheme.extendedColors.iconMuted,
                shape = RoundedCornerShape(Spacing.medium),
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(Spacing.medium),
            ),
        action = snackbarData.visuals.actionLabel?.let { actionLabel ->
            {
                TextButton(
                    onClick = { snackbarData.performAction() },
                ) {
                    Text(
                        text = actionLabel,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        containerColor = Color.White.copy(alpha = 0.85f),
        contentColor = TextPrimary,
        shape = RoundedCornerShape(Spacing.medium),
    ) {
        Text(
            text = snackbarData.visuals.message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
        )
    }
}

