package com.example.tailtrail.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tailtrail.data.model.QuizAnswer
import com.example.tailtrail.data.model.QuizSubmissionRequest
import com.example.tailtrail.data.model.UserDetails
import com.example.tailtrail.data.model.UserResponse
import com.example.tailtrail.data.repository.AuthRepository
import com.example.tailtrail.data.storage.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AuthViewModel(private val context: Context) : ViewModel() {
    private val repository = AuthRepository()
    private val userPreferences = UserPreferences(context)
    private val TAG = "AuthViewModel"

    var loginState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var signupState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var quizSubmissionState by mutableStateOf<QuizState>(QuizState.Idle)
        private set

    var currentUser by mutableStateOf<UserResponse?>(null)
        private set

    var userDetails by mutableStateOf<UserDetails?>(null)
        private set

    init {
        // Check for stored user data when ViewModel is initialized
        viewModelScope.launch {
            val userId = userPreferences.userId.first()
            val userName = userPreferences.userName.first()

            if (userId != null && userName != null) {
                currentUser = UserResponse(userId, userName, 0) // Default isQuiz to 0 when restoring from preferences
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
                        Log.d(TAG, "Login successful: $user")
                        currentUser = user
                        // Save user data to persistent storage
                        userPreferences.saveUserInfo(user.userId, user.name)
                        loginState = AuthState.Success(user)
                        callback(true, "Login successful")
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
                        Log.d(TAG, "Signup successful: $user")
                        currentUser = user
                        // Save user data to persistent storage
                        userPreferences.saveUserInfo(user.userId, user.name)
                        signupState = AuthState.Success(user)
                        callback(true, "Signup successful")
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

    fun submitQuiz(answers: List<QuizAnswer>, callback: (Boolean, String) -> Unit) {
        val userId = currentUser?.userId
        if (userId == null) {
            Log.e(TAG, "Cannot submit quiz: No user logged in")
            callback(false, "No user logged in")
            return
        }

        Log.d(TAG, "Submitting quiz for user: $userId")
        quizSubmissionState = QuizState.Loading

        viewModelScope.launch {
            try {
                val request = QuizSubmissionRequest(userId, answers)
                Log.d(TAG, "Making quiz submission API call...")
                val result = repository.submitQuiz(request)
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Quiz submission successful: ${response.message}")
                        quizSubmissionState = QuizState.Success(response.message)
                        callback(true, response.message)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Quiz submission failed: ${exception.message}", exception)
                        quizSubmissionState = QuizState.Error(exception.message ?: "Unknown error occurred")
                        callback(false, exception.message ?: "Quiz submission failed")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Quiz submission exception: ${e.message}", e)
                quizSubmissionState = QuizState.Error(e.message ?: "Unknown error occurred")
                callback(false, e.message ?: "Quiz submission failed")
            }
        }
    }

    fun resetStates() {
        Log.d(TAG, "Resetting auth states")
        loginState = AuthState.Idle
        signupState = AuthState.Idle
    }

    fun resetQuizState() {
        Log.d(TAG, "Resetting quiz state")
        quizSubmissionState = QuizState.Idle
    }

    fun signOut() {
        Log.d(TAG, "Signing out user")
        viewModelScope.launch {
            userPreferences.clearUserData()
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

sealed class QuizState {
    object Idle : QuizState()
    object Loading : QuizState()
    data class Success(val message: String) : QuizState()
    data class Error(val message: String) : QuizState()
}
