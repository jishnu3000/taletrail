package com.example.tailtrail.data.repository

import com.example.tailtrail.data.api.RetrofitClient
import com.example.tailtrail.data.model.LoginRequest
import com.example.tailtrail.data.model.SignupRequest
import com.example.tailtrail.data.model.UserResponse
import retrofit2.Response

class AuthRepository {
    private val authApi = RetrofitClient.authApi

    suspend fun login(phoneNumber: String, password: String): Result<UserResponse> {
        return try {
            val request = LoginRequest(phoneNumber, password)
            val response = authApi.login(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(name: String, phoneNumber: String, email: String, password: String, pincode: String): Result<UserResponse> {
        return try {
            val request = SignupRequest(name, phoneNumber, email, password, pincode)
            val response = authApi.signup(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Signup failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
