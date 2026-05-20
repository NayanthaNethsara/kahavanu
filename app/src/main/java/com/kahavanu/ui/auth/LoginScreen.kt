package com.kahavanu.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.kahavanu.ui.common.AppSnackbarHost
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.text.font.FontWeight
import com.kahavanu.ui.common.AuthScaffold
import com.kahavanu.ui.common.AuthOutlinedButton
import com.kahavanu.ui.common.AuthPrimaryButton
import com.kahavanu.ui.common.AuthTextField
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val googleSignInRequest = rememberGoogleSignInRequest()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthEvent.Error -> snackbarHostState.showSnackbar(event.message)
                AuthEvent.ResetPasswordEmailSent -> snackbarHostState.showSnackbar("Password reset email sent")
            }
        }
    }

    AuthScaffold(
        title = "Welcome back",
        subtitle = "Log in to continue your wealth journey",
        onBack = onBack,
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) }
    ) {
        AuthOutlinedButton(
            text = "Continue with Google",
            leadingIcon = Icons.Outlined.GTranslate,
            onClick = { viewModel.startGoogleSignIn(googleSignInRequest) },
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = {
                    if (uiState.email.isBlank()) {
                        viewModel.setError("Enter your email first")
                    } else {
                        viewModel.sendPasswordReset(email = uiState.email)
                    }
                },
            ) {
                Text(
                    text = "Forgot password?",
                    color = RawColors.Emerald.Emerald600,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.medium))

        AuthPrimaryButton(
            text = if (uiState.isLoading) "Signing in..." else "Log in",
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = {
                if (uiState.email.isBlank() || uiState.password.length < 8) {
                    viewModel.setError("Enter a valid email and password")
                    return@AuthPrimaryButton
                }
                viewModel.login(uiState.email, uiState.password)
            },
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = uiState.errorMessage ?: "",
                color = RawColors.Red.Red600,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
