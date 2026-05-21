package com.kahavanu.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.kahavanu.domain.model.UserSession
import com.kahavanu.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DefaultAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthRepository {
    private val authStateFlow = MutableStateFlow(auth.currentUser?.toSession())

    override val currentSession: UserSession?
        get() = authStateFlow.value

    override val authState: Flow<UserSession?> = authStateFlow.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        authStateFlow.value = firebaseAuth.currentUser?.toSession()
    }

    init {
        auth.addAuthStateListener(authStateListener)
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
        val result = auth.currentUser?.updateProfile(profileUpdate)
            ?.awaitUnitResult()
            ?: Result.failure(IllegalStateException("User not available"))
        if (result.isSuccess) {
            publishCurrentSession()
        }
        return result
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).awaitUnitResult()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return auth.sendPasswordResetEmail(email).awaitUnitResult()
    }

    override fun signOut() {
        authStateFlow.value = null
        auth.signOut()
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> {
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        val result = auth.currentUser?.updateProfile(profileUpdate)
            ?.awaitUnitResult()
            ?: Result.failure(IllegalStateException("No user logged in"))
        if (result.isSuccess) {
            auth.currentUser?.reload()?.awaitUnitResult()
            publishCurrentSession()
        }
        return result
    }

    private fun publishCurrentSession() {
        authStateFlow.value = auth.currentUser?.toSession()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return auth.currentUser?.delete()?.awaitUnitResult()
            ?: Result.failure(IllegalStateException("No user logged in"))
    }

    fun cleanup() {
        auth.removeAuthStateListener(authStateListener)
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
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resumeWith(Result.success(Result.success(task.result)))
            } else {
                continuation.resumeWith(Result.success(Result.failure(task.exception ?: Exception("Unknown error"))))
            }
        }
    }
}

private suspend fun com.google.android.gms.tasks.Task<*>.awaitUnitResult(): Result<Unit> {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resumeWith(Result.success(Result.success(Unit)))
            } else {
                continuation.resumeWith(Result.success(Result.failure(task.exception ?: Exception("Unknown error"))))
            }
        }
    }
}
