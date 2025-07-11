package com.example.tailtrail.data.api

import com.example.tailtrail.data.model.LoginRequest
import com.example.tailtrail.data.model.QuizAnswersResponse
import com.example.tailtrail.data.model.QuizSubmissionRequest
import com.example.tailtrail.data.model.QuizSubmissionResponse
import com.example.tailtrail.data.model.SignupRequest
import com.example.tailtrail.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("users/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<UserResponse>

    @POST("users/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<UserResponse>

    @POST("users/submit-quiz")
    suspend fun submitQuiz(@Body quizRequest: QuizSubmissionRequest): Response<QuizSubmissionResponse>

    @GET("users/quiz-answers")
    suspend fun getQuizAnswers(@Query("userId") userId: Int): Response<QuizAnswersResponse>

    // Simple health check endpoint (if available on your backend)
    @GET("health")
    suspend fun healthCheck(): Response<String>
}
