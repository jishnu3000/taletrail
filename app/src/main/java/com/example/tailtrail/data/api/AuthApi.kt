package com.example.tailtrail.data.api

import com.example.tailtrail.data.model.LoginRequest
import com.example.tailtrail.data.model.SignupRequest
import com.example.tailtrail.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("users/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<UserResponse>

    @POST("users/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<UserResponse>

    // Simple health check endpoint (if available on your backend)
    @GET("health")
    suspend fun healthCheck(): Response<String>
}
