package com.yash.skypulse

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("skypulse_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }

    fun saveAuthResponse(response: AuthResponse) {
        prefs.edit().apply {
            putString(KEY_JWT_TOKEN, response.token)
            putString(KEY_USER_ID, response.userId)
            putString(KEY_USERNAME, response.username)
            putString(KEY_EMAIL, response.email)
            apply()
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun getUserDetails(): AuthResponse? {
        val token = getAuthToken() ?: return null
        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        return AuthResponse(token, userId, username, email)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
