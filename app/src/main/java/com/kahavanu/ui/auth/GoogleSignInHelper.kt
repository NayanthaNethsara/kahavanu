package com.kahavanu.ui.auth

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

    // Interactive fallback launcher for devices/emulators without saved credentials
    val interactiveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { activityResult ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                onIdToken(idToken)
            } else {
                onError("Missing idToken from interactive Google sign-in")
            }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Interactive Google sign-in failed")
        }
    }

    // Prepare interactive sign-in intent (used as fallback)
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val interactiveSignInIntent: Intent = googleSignInClient.signInIntent

    return remember(credentialManager, request) {
        GoogleSignInLauncher(
            launch = {
                scope.launch {
                    try {
                        val result = credentialManager.getCredential(context, request)
                        try {
                            handleCredentialResult(result, onIdToken, onError)
                        } catch (ex: Exception) {
                            // If processing fails, fall back to interactive sign-in
                            interactiveLauncher.launch(interactiveSignInIntent)
                        }
                    } catch (ex: GetCredentialException) {
                        // No saved credential available — fall back to interactive sign-in
                        interactiveLauncher.launch(interactiveSignInIntent)
                    } catch (ex: Exception) {
                        // Unexpected error — try interactive sign-in as fallback
                        interactiveLauncher.launch(interactiveSignInIntent)
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
