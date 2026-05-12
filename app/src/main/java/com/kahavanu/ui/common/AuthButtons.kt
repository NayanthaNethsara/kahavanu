package com.kahavanu.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.ButtonTokens
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.Primary

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonTokens.height),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(Spacing.small))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
fun AuthOutlinedButton(
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonTokens.height),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, RawColors.Slate.Slate200.copy(alpha = 0.8f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.7f),
            contentColor = RawColors.Slate.Slate900,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = RawColors.Slate.Slate700,
                )
                Spacer(modifier = Modifier.width(Spacing.small))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = RawColors.Slate.Slate900,
            )
        }
    }
}
