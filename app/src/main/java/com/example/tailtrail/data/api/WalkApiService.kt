package com.example.tailtrail.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Data models for API

data class AddWalkResponse(val message: String)

data class WalkResponse(
    val walkId: Int,
    val genre: String,
    val noOfStops: Int,
    val stopDist: Int
)

data class RouteStop(
    val order: Int,
    val latitude: Double,
    val longitude: Double
)

data class WalkRequest(
    val userId: Int,
    val genre: String,
    val stopDist: Int,
    val noOfStops: Int,
    val route: List<RouteStop>
)

interface WalkApiService {
    @GET("walks/user-walks")
    suspend fun getUserWalks(@Query("userId") userId: Int): Response<List<WalkResponse>>

    @POST("walks/add-walk")
    suspend fun addWalk(@Body walkRequest: WalkRequest): Response<AddWalkResponse>
} 