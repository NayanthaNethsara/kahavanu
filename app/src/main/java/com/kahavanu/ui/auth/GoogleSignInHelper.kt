package com.kahavanu.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kahavanu.R

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
    val googleSignInClient = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            onError("Google sign-in cancelled")
            return@rememberLauncherForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token == null) {
                onError("Missing Google ID token")
            } else {
                onIdToken(token)
            }
        } catch (ex: ApiException) {
            onError("Google sign-in failed")
        }
    }

    return remember(googleSignInClient, launcher) {
        GoogleSignInLauncher(launch = { launcher.launch(googleSignInClient.signInIntent) })
    }
}
