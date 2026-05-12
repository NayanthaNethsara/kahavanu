package com.kahavanu.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = RawColors.Slate.Slate900,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(Spacing.small))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RawColors.Slate.Slate400,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                )
            },
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                focusedBorderColor = RawColors.Slate.Slate200,
                unfocusedBorderColor = RawColors.Slate.Slate200,
                focusedLeadingIconColor = RawColors.Slate.Slate600,
                unfocusedLeadingIconColor = RawColors.Slate.Slate600,
                focusedTextColor = RawColors.Slate.Slate900,
                unfocusedTextColor = RawColors.Slate.Slate900,
            ),
            singleLine = true,
        )
    }
}
