package com.kahavanu.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Full-width brand call-to-action with a trailing arrow. Thin wrapper over the canonical
 * [PrimaryActionButton] so all primary buttons share one implementation.
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryActionButton(
        text = text,
        enabled = true,
        onClick = onClick,
        modifier = modifier,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
    )
}
