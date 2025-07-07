package com.example.tailtrail.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tailtrail.data.UserPreferences
import com.example.tailtrail.data.model.UserDetails
import com.example.tailtrail.data.model.UserResponse
import com.example.tailtrail.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AuthViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val repository = AuthRepository()
    private val userPreferences = UserPreferences(appContext)
    private val TAG = "AuthViewModel"

    var loginState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var signupState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var currentUser by mutableStateOf<UserResponse?>(null)
        private set

    var userDetails by mutableStateOf<UserDetails?>(null)
        private set

    init {
        viewModelScope.launch {
            val userId = userPreferences.userIdFlow.first()
            if (userId != null) {
                currentUser = UserResponse(userId, "", 0) // Name not stored, set as empty
                Log.d(TAG, "Restored user from preferences: $currentUser")
            }
        }
    }

    fun login(phoneNumber: String, password: String, callback: (Boolean, String) -> Unit) {
        Log.d(TAG, "Login called with phone: $phoneNumber")
        loginState = AuthState.Loading

        viewModelScope.launch {
            try {
                Log.d(TAG, "Making login API call...")
                val result = repository.login(phoneNumber, password)
                result.fold(
                    onSuccess = { user ->
                        viewModelScope.launch {
                            userPreferences.saveUserId(user.userId)
                        }
                        currentUser = user
                        loginState = AuthState.Success(user)
                        callback(true, "Login successful!")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Login failed: ${exception.message}", exception)
                        loginState = AuthState.Error(exception.message ?: "Unknown error occurred")
                        callback(false, exception.message ?: "Login failed")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Login exception: ${e.message}", e)
                loginState = AuthState.Error(e.message ?: "Unknown error occurred")
                callback(false, e.message ?: "Login failed")
            }
        }
    }

    fun signUp(name: String, phoneNumber: String, email: String, password: String, pincode: String, callback: (Boolean, String) -> Unit) {
        Log.d(TAG, "SignUp called with phone: $phoneNumber")
        signupState = AuthState.Loading

        viewModelScope.launch {
            try {
                Log.d(TAG, "Making signup API call...")
                val result = repository.signup(name, phoneNumber, email, password, pincode)
                result.fold(
                    onSuccess = { user ->
                        viewModelScope.launch {
                            userPreferences.saveUserId(user.userId)
                        }
                        currentUser = user
                        signupState = AuthState.Success(user)
                        callback(true, "Signup successful!")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Signup failed: ${exception.message}", exception)
                        signupState = AuthState.Error(exception.message ?: "Unknown error occurred")
                        callback(false, exception.message ?: "Signup failed")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Signup exception: ${e.message}", e)
                signupState = AuthState.Error(e.message ?: "Unknown error occurred")
                callback(false, e.message ?: "Signup failed")
            }
        }
    }

    fun resetStates() {
        Log.d(TAG, "Resetting auth states")
        loginState = AuthState.Idle
        signupState = AuthState.Idle
    }

    fun signOut() {
        Log.d(TAG, "Signing out user")
        viewModelScope.launch {
            userPreferences.clearUserId()
            currentUser = null
            resetStates()
        }
    }

    fun testApiConnection(callback: (Boolean, String) -> Unit) {
        Log.d(TAG, "Testing API connection...")
        viewModelScope.launch {
            try {
                val result = repository.testApiConnection()
                result.fold(
                    onSuccess = { message ->
                        Log.d(TAG, "API connection test successful: $message")
                        callback(true, message)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "API connection test failed: ${exception.message}", exception)
                        callback(false, exception.message ?: "API connection test failed")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "API connection test exception: ${e.message}", e)
                callback(false, e.message ?: "API connection test failed")
            }
        }
    }

    fun fetchUserDetails() {
        val userId = currentUser?.userId
        if (userId == null) return
        viewModelScope.launch {
            try {
                val details = getUserDetailsFromApi(userId)
                userDetails = details
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch user details", e)
            }
        }
    }

    private suspend fun getUserDetailsFromApi(userId: Int): UserDetails? = withContext(Dispatchers.IO) {
        val url = URL("https://taletrails-backend.onrender.com/users/details?userId=$userId")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        return@withContext try {
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                UserDetails(
                    name = json.getString("name"),
                    phoneNumber = json.getString("phoneNumber"),
                    pincode = json.getString("pincode"),
                    email = json.getString("email"),
                    quizTaken = json.getBoolean("quizTaken")
                )
            } else null
        } finally {
            connection.disconnect()
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
