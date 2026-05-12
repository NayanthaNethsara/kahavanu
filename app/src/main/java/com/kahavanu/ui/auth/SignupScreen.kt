package com.kahavanu.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.GTranslate
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kahavanu.ui.common.AuthScaffold
import com.kahavanu.ui.common.AuthOutlinedButton
import com.kahavanu.ui.common.AuthPrimaryButton
import com.kahavanu.ui.common.AuthTextField
import com.kahavanu.ui.theme.Spacing

@Composable
fun SignupScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val googleSignInRequest = rememberGoogleSignInRequest()

    AuthScaffold(
        title = "Create your account",
        subtitle = "Start building wealth with discipline",
        onBack = onBack,
    ) {
        AuthOutlinedButton(
            text = "Continue with Google",
            leadingIcon = Icons.Outlined.GTranslate,
            onClick = { viewModel.startGoogleSignIn(googleSignInRequest) },
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        AuthTextField(
            label = "Full name",
            value = uiState.fullName,
            onValueChange = viewModel::onFullNameChange,
            placeholder = "Kavindu Perera",
            leadingIcon = Icons.Outlined.AccountCircle,
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        AuthTextField(
            label = "Email",
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "you@example.com",
            leadingIcon = Icons.Outlined.MailOutline,
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        AuthTextField(
            label = "Password",
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "••••••••",
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = null,
                    )
                }
            },
            visualTransformation = if (uiState.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
        )

        Text(
            text = "Must be at least 8 characters",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.medium))

        AuthPrimaryButton(
            text = if (uiState.isLoading) "Creating account..." else "Create account",
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = {
                if (uiState.fullName.isBlank() || uiState.email.isBlank() || uiState.password.length < 8) {
                    viewModel.setError("Enter your name, email, and a stronger password")
                    return@AuthPrimaryButton
                }
                viewModel.signup(uiState.fullName, uiState.email, uiState.password)
            },
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
