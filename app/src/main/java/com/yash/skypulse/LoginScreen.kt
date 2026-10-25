package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthResponse?>(null)
    val authState: StateFlow<AuthResponse?> = _authState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var tokenManager: TokenManager? = null

    fun initSession(context: android.content.Context) {
        BackendRetrofitInstance.initialize(context) // Attach AuthInterceptor to Retrofit!
        if (tokenManager == null) {
            tokenManager = TokenManager(context)
            // Auto-login if token already exists in SharedPreferences!
            val savedUser = tokenManager?.getUserDetails()
            if (savedUser != null) {
                _authState.value = savedUser
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = BackendRetrofitInstance.api.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    tokenManager?.saveAuthResponse(authData) // Save JWT securely!
                    _authState.value = authData
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Invalid credentials!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error: ${e.message}"
            }
        }
    }

    fun register(username: String, email: String, password: String, dob: String) {
        viewModelScope.launch {
            try {
                val response = BackendRetrofitInstance.api.register(RegisterRequest(username, email, password, dob))
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    tokenManager?.saveAuthResponse(authData) // Save JWT securely!
                    _authState.value = authData
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Registration failed! Username or email taken."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error: ${e.message}"
            }
        }
    }

    fun logout() {
        tokenManager?.clear() // Wipe JWT on logout
        _authState.value = null
    }
}

@Composable
fun LoginScreen(viewModel: AuthViewModel = viewModel(), onAuthSuccess: () -> Unit) {
    var isRegisterMode by remember { mutableStateOf(false) }
    
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(authState) {
        if (authState != null) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A47)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🚀", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRegisterMode) "Create Station Account" else "Welcome Back, Explorer",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth (YYYY-MM-DD)", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isRegisterMode) {
                            viewModel.register(username, email, password, dob)
                        } else {
                            viewModel.login(username, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isRegisterMode) "Register" else "Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Register",
                    color = Color(0xFF4A90E2),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { isRegisterMode = !isRegisterMode }
                )
            }
        }
    }
}
