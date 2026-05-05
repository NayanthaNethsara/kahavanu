package com.kahavanu.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kahavanu.R
import kotlinx.coroutines.launch

@Stable
class GoogleSignInLauncher(
    val launch: () -> Unit,
)

@Composable
fun rememberGoogleSignInLauncher(
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit,
): GoogleSignInLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val request = remember {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    return remember(credentialManager, request) {
        GoogleSignInLauncher(
            launch = {
                scope.launch {
                    try {
                        val result = credentialManager.getCredential(context, request)
                        handleCredentialResult(result, onIdToken, onError)
                    } catch (_: GetCredentialException) {
                        onError("Google sign-in is unavailable. Please try again.")
                    } catch (_: Exception) {
                        onError("Google sign-in failed. Please try again.")
                    }
                }
            },
        )
    }
}

private fun handleCredentialResult(
    result: GetCredentialResponse,
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit,
) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        if (idToken.isBlank()) {
            onError("Missing Google ID token")
        } else {
            onIdToken(idToken)
        }
        return
    }

    onError("Unsupported credential type")
}
