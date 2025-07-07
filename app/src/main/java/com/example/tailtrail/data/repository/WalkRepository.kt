package com.example.tailtrail.data.repository

import com.example.tailtrail.data.api.WalkApiService
import com.example.tailtrail.data.api.WalkRequest
import com.example.tailtrail.data.api.WalkResponse
import com.example.tailtrail.data.api.AddWalkResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WalkRepository {
    private val api: WalkApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://taletrails-backend.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(WalkApiService::class.java)
    }

    suspend fun getUserWalks(userId: Int): Response<List<WalkResponse>> = api.getUserWalks(userId)
    suspend fun addWalk(walkRequest: WalkRequest): Response<AddWalkResponse> = api.addWalk(walkRequest)
} 