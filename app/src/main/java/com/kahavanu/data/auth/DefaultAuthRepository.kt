package com.kahavanu.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DefaultAuthRepository(
    private val auth: FirebaseAuth,
) : AuthRepository {
    override val authState: Flow<UserSession?> = callbackFlow {
        trySend(auth.currentUser?.toSession())
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toSession())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return auth.signInWithEmailAndPassword(email, password)
            .awaitUnitResult()
    }

    override suspend fun signUpWithEmail(
        fullName: String,
        email: String,
        password: String,
    ): Result<Unit> {
        val createResult = auth.createUserWithEmailAndPassword(email, password)
            .awaitResult()
        if (createResult.isFailure) {
            return Result.failure(createResult.exceptionOrNull()!!)
        }

        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()
        return auth.currentUser?.updateProfile(profileUpdate)
            ?.awaitUnitResult()
            ?: Result.failure(IllegalStateException("User not available"))
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).awaitUnitResult()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return auth.sendPasswordResetEmail(email).awaitUnitResult()
    }

    override fun signOut() {
        auth.signOut()
    }
}

private fun com.google.firebase.auth.FirebaseUser.toSession(): UserSession = UserSession(
    uid = uid,
    displayName = displayName,
    email = email,
)

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(Result.success(task.result))
            } else {
                continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
            }
        }
    }
}

private suspend fun com.google.android.gms.tasks.Task<Void>.awaitUnitResult(): Result<Unit> {
    return awaitResult<Void>().map { Unit }
}
