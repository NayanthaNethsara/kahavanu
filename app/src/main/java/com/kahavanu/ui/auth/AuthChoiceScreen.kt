package com.kahavanu.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.GTranslate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kahavanu.R
import com.kahavanu.core.config.AppConfig
import com.kahavanu.ui.common.AppDecorativeGradientOverlay
import com.kahavanu.ui.common.AuthOutlinedButton
import com.kahavanu.ui.common.AuthPrimaryButton
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import androidx.compose.ui.unit.dp

@Composable
fun AuthChoiceScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val googleSignInRequest = rememberGoogleSignInRequest()

    Surface(
        modifier = Modifier.fillMaxSize(),
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
                    .padding(horizontal = Spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(Spacing.massive))

                Image(
                    painter = painterResource(id = R.drawable.kahavanu_logo),
                    contentDescription = "Kahavanu logo",
                    modifier = Modifier
                        .height(64.dp)
                        .padding(horizontal = Spacing.huge),
                )

                Spacer(modifier = Modifier.height(Spacing.massive))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Text(
                        text = "Welcome to Kahavanu",
                        style = MaterialTheme.typography.headlineMedium,
                        color = RawColors.Slate.Slate900,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Your journey to financial discipline starts here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RawColors.Slate.Slate600,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.huge))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                    AuthOutlinedButton(
                        text = "Continue with Google",
                        leadingIcon = Icons.Outlined.GTranslate,
                        onClick = { viewModel.startGoogleSignIn(googleSignInRequest) },
                    )

                    Text(
                        text = "or",
                        style = MaterialTheme.typography.labelLarge,
                        color = RawColors.Slate.Slate400,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )

                    AuthPrimaryButton(
                        text = "Create account",
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onCreateAccount,
                    )

                    AuthOutlinedButton(
                        text = "Already have an account? Log in",
                        onClick = onLogin,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = RawColors.Red.Red600,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                }

                Spacer(modifier = Modifier.weight(1f))
                TermsFooter()
                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
}

@Composable
private fun TermsFooter() {
    val uriHandler = LocalUriHandler.current
    
    val text = buildAnnotatedString {
        append("By continuing, you agree to our ")
        pushStringAnnotation(tag = "URL", annotation = AppConfig.termsOfServiceUrl)
        withStyle(SpanStyle(color = RawColors.Emerald.Emerald600, fontWeight = FontWeight.Medium)) {
            append("Terms of Service")
        }
        pop()
        append(" and ")
        pushStringAnnotation(tag = "URL", annotation = AppConfig.privacyPolicyUrl)
        withStyle(SpanStyle(color = RawColors.Emerald.Emerald600, fontWeight = FontWeight.Medium)) {
            append("Privacy Policy")
        }
        pop()
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            color = RawColors.Slate.Slate500,
            textAlign = TextAlign.Center,
        ),
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                text.getStringAnnotations(tag = "URL", start = offset.x.toInt(), end = offset.x.toInt())
                    .firstOrNull()?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
            }
        },
    )
}
