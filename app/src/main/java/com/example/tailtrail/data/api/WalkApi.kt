package com.example.tailtrail.data.api

import com.example.tailtrail.data.model.AddWalkRequest
import com.example.tailtrail.data.model.AddWalkResponse
import com.example.tailtrail.data.model.Walk
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WalkApi {
    @POST("walks/add-walk")
    suspend fun addWalk(@Body request: AddWalkRequest): Response<AddWalkResponse>
    
    @GET("walks/user-walks")
    suspend fun getUserWalks(@Query("userId") userId: Int): Response<List<Walk>>
} 