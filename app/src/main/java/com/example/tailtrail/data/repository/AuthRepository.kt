package com.example.tailtrail.data.repository

import android.util.Log
import com.example.tailtrail.data.api.RetrofitClient
import com.example.tailtrail.data.model.LoginRequest
import com.example.tailtrail.data.model.QuizAnswersResponse
import com.example.tailtrail.data.model.QuizSubmissionRequest
import com.example.tailtrail.data.model.QuizSubmissionResponse
import com.example.tailtrail.data.model.SignupRequest
import com.example.tailtrail.data.model.UserResponse
import retrofit2.Response

class AuthRepository {
    private val authApi = RetrofitClient.authApi
    private val TAG = "AuthRepository"

    // Test function to verify API connectivity
    suspend fun testApiConnection(): Result<String> {
        return try {
            Log.d(TAG, "Testing API connection...")
            val response = authApi.healthCheck()
            Log.d(TAG, "Health check response code: ${response.code()}")
            Log.d(TAG, "Health check response body: ${response.body()}")
            
            if (response.isSuccessful) {
                Log.d(TAG, "API connection test successful")
                Result.success("API is accessible and responding")
            } else {
                Log.e(TAG, "API connection test failed with code: ${response.code()}")
                Result.failure(Exception("API responded with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "API connection test failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun login(phoneNumber: String, password: String): Result<UserResponse> {
        return try {
            Log.d(TAG, "Attempting login for phone: $phoneNumber")
            
            val request = LoginRequest(phoneNumber, password)
            Log.d(TAG, "Login request: $request")
            
            val response = authApi.login(request)
            Log.d(TAG, "Login response code: ${response.code()}")
            Log.d(TAG, "Login response message: ${response.message()}")
            Log.d(TAG, "Login response body: ${response.body()}")
            Log.d(TAG, "Login response error body: ${response.errorBody()?.string()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Login successful: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorMessage = when {
                    response.code() == 401 -> "Invalid phone number or password"
                    response.code() == 404 -> "User not found"
                    response.code() == 500 -> "Server error. Please try again later"
                    response.errorBody() != null -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error body: $errorBody")
                        "Login failed: ${response.message()}"
                    }
                    else -> "Login failed: ${response.message()}"
                }
                Log.e(TAG, "Login failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login exception: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("timeout") == true -> "Request timeout. Please check your internet connection."
                e.message?.contains("Unable to resolve host") == true -> "No internet connection. Please check your network."
                else -> "Login failed: ${e.message ?: "Unknown error occurred"}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signup(name: String, phoneNumber: String, email: String, password: String, pincode: String): Result<UserResponse> {
        return try {
            Log.d(TAG, "Attempting signup for phone: $phoneNumber")
            
            val request = SignupRequest(name, phoneNumber, email, password, pincode)
            Log.d(TAG, "Signup request: $request")
            
            val response = authApi.signup(request)
            Log.d(TAG, "Signup response code: ${response.code()}")
            Log.d(TAG, "Signup response message: ${response.message()}")
            Log.d(TAG, "Signup response body: ${response.body()}")
            Log.d(TAG, "Signup response error body: ${response.errorBody()?.string()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Signup successful: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorMessage = when {
                    response.code() == 409 -> "User already exists with this phone number"
                    response.code() == 400 -> "Invalid input data"
                    response.code() == 500 -> "Server error. Please try again later"
                    response.errorBody() != null -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error body: $errorBody")
                        "Signup failed: ${response.message()}"
                    }
                    else -> "Signup failed: ${response.message()}"
                }
                Log.e(TAG, "Signup failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Signup exception: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("timeout") == true -> "Request timeout. Please check your internet connection."
                e.message?.contains("Unable to resolve host") == true -> "No internet connection. Please check your network."
                else -> "Signup failed: ${e.message ?: "Unknown error occurred"}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun submitQuiz(quizRequest: QuizSubmissionRequest): Result<QuizSubmissionResponse> {
        return try {
            Log.d(TAG, "Submitting quiz for user: ${quizRequest.userId}")
            Log.d(TAG, "Quiz submission request: $quizRequest")
            Log.d(TAG, "Starting quiz submission with 60-second timeout...")

            val startTime = System.currentTimeMillis()
            val response = authApi.submitQuiz(quizRequest)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            Log.d(TAG, "Quiz submission response received in ${duration}ms")
            Log.d(TAG, "Quiz submission response code: ${response.code()}")
            Log.d(TAG, "Quiz submission response body: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Quiz submission successful: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorMessage = when {
                    response.code() == 404 -> "User not found"
                    response.code() == 400 -> "Invalid quiz data"
                    response.code() == 500 -> "Server error. Please try again later"
                    response.errorBody() != null -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Quiz submission error body: $errorBody")
                        "Quiz submission failed: ${response.message()}"
                    }
                    else -> "Quiz submission failed: ${response.message()}"
                }
                Log.e(TAG, "Quiz submission failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Quiz submission exception: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("timeout") == true -> "Quiz submission timed out after 60 seconds. Please check your internet connection and try again."
                e.message?.contains("Unable to resolve host") == true -> "No internet connection. Please check your network."
                else -> "Quiz submission failed: ${e.message ?: "Unknown error occurred"}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun getQuizAnswers(userId: Int): Result<QuizAnswersResponse> {
        return try {
            Log.d(TAG, "Fetching quiz answers for user: $userId")
            
            val response = authApi.getQuizAnswers(userId)
            Log.d(TAG, "Quiz answers response code: ${response.code()}")
            Log.d(TAG, "Quiz answers response body: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Quiz answers fetch successful: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorMessage = when {
                    response.code() == 404 -> "User not found"
                    response.code() == 400 -> "Invalid user ID or no quiz answers found"
                    response.code() == 500 -> "Server error. Please try again later"
                    response.errorBody() != null -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Quiz answers error body: $errorBody")
                        "Failed to fetch quiz answers: ${response.message()}"
                    }
                    else -> "Failed to fetch quiz answers: ${response.message()}"
                }
                Log.e(TAG, "Quiz answers fetch failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Quiz answers fetch exception: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("timeout") == true -> "Request timeout. Please check your internet connection."
                e.message?.contains("Unable to resolve host") == true -> "No internet connection. Please check your network."
                else -> "Failed to fetch quiz answers: ${e.message ?: "Unknown error occurred"}"
            }
            Result.failure(Exception(errorMessage))
        }
    }
}
