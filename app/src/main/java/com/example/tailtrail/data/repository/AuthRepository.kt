package com.example.tailtrail.data.repository

import android.util.Log
import com.example.tailtrail.data.api.RetrofitClient
import com.example.tailtrail.data.model.LoginRequest
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
}
