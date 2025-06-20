package com.example.tailtrail.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tailtrail.data.model.UserResponse
import com.example.tailtrail.data.repository.AuthRepository
import com.example.tailtrail.data.storage.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(private val context: Context) : ViewModel() {
    private val repository = AuthRepository()
    private val userPreferences = UserPreferences(context)

    var loginState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var signupState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var currentUser by mutableStateOf<UserResponse?>(null)
        private set

    init {
        // Check for stored user data when ViewModel is initialized
        viewModelScope.launch {
            val userId = userPreferences.userId.first()
            val userName = userPreferences.userName.first()

            if (userId != null && userName != null) {
                currentUser = UserResponse(userId, userName)
            }
        }
    }

    fun login(phoneNumber: String, password: String, callback: (Boolean, String) -> Unit) {
        loginState = AuthState.Loading

        viewModelScope.launch {
            try {
                val result = repository.login(phoneNumber, password)
                result.fold(
                    onSuccess = { user ->
                        currentUser = user
                        // Save user data to persistent storage
                        userPreferences.saveUserInfo(user.userId, user.name)
                        loginState = AuthState.Success(user)
                        callback(true, "Login successful")
                    },
                    onFailure = { exception ->
                        loginState = AuthState.Error(exception.message ?: "Unknown error occurred")
                        callback(false, exception.message ?: "Login failed")
                    }
                )
            } catch (e: Exception) {
                loginState = AuthState.Error(e.message ?: "Unknown error occurred")
                callback(false, e.message ?: "Login failed")
            }
        }
    }

    fun signUp(name: String, phoneNumber: String, email: String, password: String, pincode: String, callback: (Boolean, String) -> Unit) {
        signupState = AuthState.Loading

        viewModelScope.launch {
            try {
                val result = repository.signup(name, phoneNumber, email, password, pincode)
                result.fold(
                    onSuccess = { user ->
                        currentUser = user
                        // Save user data to persistent storage
                        userPreferences.saveUserInfo(user.userId, user.name)
                        signupState = AuthState.Success(user)
                        callback(true, "Signup successful")
                    },
                    onFailure = { exception ->
                        signupState = AuthState.Error(exception.message ?: "Unknown error occurred")
                        callback(false, exception.message ?: "Signup failed")
                    }
                )
            } catch (e: Exception) {
                signupState = AuthState.Error(e.message ?: "Unknown error occurred")
                callback(false, e.message ?: "Signup failed")
            }
        }
    }

    fun resetStates() {
        loginState = AuthState.Idle
        signupState = AuthState.Idle
    }

    fun signOut() {
        viewModelScope.launch {
            userPreferences.clearUserData()
            currentUser = null
            resetStates()
        }
    }

    // Factory to create AuthViewModel with context
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}
