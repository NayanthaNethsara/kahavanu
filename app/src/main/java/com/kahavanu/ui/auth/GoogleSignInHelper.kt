package com.kahavanu.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kahavanu.R
@Composable
fun rememberGoogleSignInRequest(): suspend () -> Result<String> {
    val context = LocalContext.current
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

    return remember(credentialManager, request, context) {
        suspend {
            try {
                val result = credentialManager.getCredential(context, request)
                extractIdToken(result)
            } catch (error: GetCredentialException) {
                Result.failure(error)
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }
}

private fun extractIdToken(
    result: GetCredentialResponse,
): Result<String> {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        return if (idToken.isBlank()) {
            Result.failure(IllegalStateException("Missing Google ID token"))
        } else {
            Result.success(idToken)
        }
    }

    return Result.failure(IllegalStateException("Unsupported credential type"))
}
