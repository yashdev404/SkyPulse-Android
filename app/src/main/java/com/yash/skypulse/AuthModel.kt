package com.yash.skypulse

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val dateOfBirth: String,
    val bio: String = "Space Explorer"
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
    val email: String
)

data class ChangePasswordRequest(
    val username: String,
    val oldPassword: String,
    val newPassword: String
)
