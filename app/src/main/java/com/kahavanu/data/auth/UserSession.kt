package com.kahavanu.data.auth

data class UserSession(
    val uid: String,
    val displayName: String?,
    val email: String?,
)
