package com.kahavanu.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import com.kahavanu.R
import com.kahavanu.ui.common.AppDecorativeGradientOverlay
import com.kahavanu.ui.common.AuthOutlinedButton
import com.kahavanu.ui.common.AuthPrimaryButton
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.material3.SnackbarHostState
import com.kahavanu.ui.common.AppSnackbarHost
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthChoiceScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val googleSignInRequest = rememberGoogleSignInRequest()
    var visible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        visible = true
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                AuthEvent.ResetPasswordEmailSent -> {
                    snackbarHostState.showSnackbar("Password reset email sent")
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = RawColors.Slate.Slate50,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppDecorativeGradientOverlay(
                modifier = Modifier
                    .offset(x = 120.dp, y = (-20).dp)
                    .size(300.dp),
            )
            
            AppDecorativeGradientOverlay(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-60).dp, y = 40.dp)
                    .size(350.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = Spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(Spacing.massive))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -20 }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kahavanu_logo),
                        contentDescription = "Kahavanu logo",
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = Spacing.huge),
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.massive))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(1000, 300)) + slideInVertically(tween(1000, 300)) { 20 }
                ) {
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
                }

                Spacer(modifier = Modifier.height(Spacing.huge))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(1000, 600)) + slideInVertically(tween(1000, 600)) { 40 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                        AuthOutlinedButton(
                            text = "Continue with Google",
                            leadingIcon = Icons.Outlined.GTranslate,
                            onClick = { viewModel.startGoogleSignIn(googleSignInRequest) },
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(RawColors.Slate.Slate200))
                            Text(
                                text = "or",
                                style = MaterialTheme.typography.labelMedium,
                                color = RawColors.Slate.Slate400,
                                fontSize = 13.sp,
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(RawColors.Slate.Slate200))
                        }

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
                }

                Spacer(modifier = Modifier.weight(1f))
                
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(1000, 900))
                ) {
                    TermsFooter()
                }
                
                Spacer(modifier = Modifier.height(Spacing.large))
            }

            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun TermsFooter() {
    val uriHandler = LocalUriHandler.current
    val termsOfServiceUrl = stringResource(R.string.terms_of_service_url)
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    
    val text = buildAnnotatedString {
        append("By continuing, you agree to our ")
        pushStringAnnotation(tag = "URL", annotation = termsOfServiceUrl)
        withStyle(SpanStyle(color = RawColors.Emerald.Emerald600, fontWeight = FontWeight.Medium)) {
            append("Terms of Service")
        }
        pop()
        append(" and ")
        pushStringAnnotation(tag = "URL", annotation = privacyPolicyUrl)
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
