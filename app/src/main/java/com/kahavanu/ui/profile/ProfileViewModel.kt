package com.kahavanu.ui.profile

import androidx.lifecycle.ViewModel
import com.kahavanu.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun logout() {
        authRepository.signOut()
    }
}
