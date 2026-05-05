package com.kahavanu.data.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentSession: UserSession?
    val authState: Flow<UserSession?>

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(
        fullName: String,
        email: String,
        password: String,
    ): Result<Unit>

    suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun signOut()
}
