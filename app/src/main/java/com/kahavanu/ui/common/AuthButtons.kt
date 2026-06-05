package com.kahavanu.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Auth-flow primary button. Delegates to the canonical [PrimaryActionButton] so the brand
 * CTA looks identical across auth, onboarding, and the main app.
 */
@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    PrimaryActionButton(
        text = text,
        enabled = true,
        onClick = onClick,
        modifier = modifier,
        trailingIcon = trailingIcon,
    )
}

/**
 * Auth-flow secondary button (e.g. "Continue with Google"). Delegates to the canonical
 * [SecondaryActionButton]; the leading icon keeps its own colour (e.g. the Google logo).
 */
@Composable
fun AuthOutlinedButton(
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    SecondaryActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        tintIcon = false,
    )
}
