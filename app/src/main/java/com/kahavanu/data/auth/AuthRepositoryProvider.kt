package com.kahavanu.data.auth

import com.google.firebase.auth.FirebaseAuth

object AuthRepositoryProvider {
    val repository: AuthRepository by lazy {
        DefaultAuthRepository(FirebaseAuth.getInstance())
    }
}
