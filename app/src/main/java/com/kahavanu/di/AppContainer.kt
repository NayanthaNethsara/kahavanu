package com.kahavanu.di

import com.google.firebase.auth.FirebaseAuth
import com.kahavanu.data.auth.DefaultAuthRepository
import com.kahavanu.domain.repository.AuthRepository

object AppContainer {
    val authRepository: AuthRepository by lazy {
        DefaultAuthRepository(FirebaseAuth.getInstance())
    }
}
