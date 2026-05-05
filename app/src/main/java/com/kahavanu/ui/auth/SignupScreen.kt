package com.kahavanu.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kahavanu.R
import com.kahavanu.di.AppContainer
import com.kahavanu.ui.common.AppDecorativeGradientOverlay
import com.kahavanu.ui.common.AuthOutlinedButton
import com.kahavanu.ui.common.AuthPrimaryButton
import com.kahavanu.ui.common.AuthTextField
import com.kahavanu.ui.theme.OnboardingTokens
import com.kahavanu.ui.theme.Spacing

@Composable
fun SignupScreen(
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AppContainer.authRepository),
    ),
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val isAuthenticated = viewModel.isAuthenticated.collectAsStateWithLifecycle().value

    val googleLauncher = rememberGoogleSignInLauncher(
        onIdToken = { token -> viewModel.signInWithGoogle(token) },
        onError = { message -> viewModel.setError(message) },
    )

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onAuthSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppDecorativeGradientOverlay(
                modifier = Modifier.offset(
                    x = OnboardingTokens.gradientLeftOffset,
                    y = OnboardingTokens.gradientTopOffset,
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
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(text = "Back")
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kahavanu_logo),
                        contentDescription = "Kahavanu logo",
                        modifier = Modifier.height(OnboardingTokens.headerHeight),
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.huge))

                Text(
                    text = "Create your account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Start building wealth with discipline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(Spacing.huge))

                AuthOutlinedButton(
                    text = "Continue with Google",
                    leadingIcon = Icons.Outlined.GTranslate,
                    onClick = googleLauncher.launch,
                )

                Spacer(modifier = Modifier.height(Spacing.large))

                AuthTextField(
                    label = "Full name",
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Kavindu Perera",
                    leadingIcon = Icons.Outlined.AccountCircle,
                )

                Spacer(modifier = Modifier.height(Spacing.large))

                AuthTextField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "you@example.com",
                    leadingIcon = Icons.Outlined.MailOutline,
                )

                Spacer(modifier = Modifier.height(Spacing.large))

                AuthTextField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "••••••••",
                    leadingIcon = Icons.Outlined.Lock,
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = null,
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) {
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
                        if (fullName.isBlank() || email.isBlank() || password.length < 8) {
                            viewModel.setError("Enter your name, email, and a stronger password")
                            return@AuthPrimaryButton
                        }
                        viewModel.signup(fullName, email, password)
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

                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
}
