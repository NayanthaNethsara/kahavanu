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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.ButtonTokens
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.Primary
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import com.kahavanu.ui.theme.appButtonHighlightBrush

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val buttonShape = RoundedCornerShape(ButtonTokens.radius)
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonTokens.height)
            .shadow(
                elevation = 6.dp,
                shape = buttonShape,
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .background(
                color = Primary,
                shape = buttonShape,
            )
            .background(
                brush = appButtonHighlightBrush(),
                shape = buttonShape,
            ),
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
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
                fontWeight = FontWeight.Medium,
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(Spacing.small))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.width(ButtonTokens.iconSize)
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
    val buttonShape = RoundedCornerShape(ButtonTokens.radius)
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonTokens.height)
            .shadow(
                elevation = 4.dp,
                shape = buttonShape,
                clip = false,
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
        shape = buttonShape,
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
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
                    tint = Color.Unspecified,
                    modifier = Modifier.width(ButtonTokens.iconSize)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
