package com.example.taletrail.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("location/check")
    suspend fun checkLocation(
        @Query("userLat") userLat: Double,
        @Query("userLng") userLng: Double,
        @Query("checkpointLat") checkpointLat: Double,
        @Query("checkpointLng") checkpointLng: Double
    ): ApiResponse
}

data class ApiResponse(val message: String)

object RetrofitClient {
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request + response body
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://taletrails-backend.onrender.com/") // Note the trailing slash
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
