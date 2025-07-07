package com.example.tailtrail.data.model

data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val pincode: String
)

data class UserResponse(
    val userId: Int,
    val name: String,
    val isQuiz: Int
)
