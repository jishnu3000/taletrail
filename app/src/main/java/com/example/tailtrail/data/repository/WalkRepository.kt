package com.example.tailtrail.data.repository

import com.example.tailtrail.data.api.WalkApi
import com.example.tailtrail.data.model.AddWalkRequest
import com.example.tailtrail.data.model.Walk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalkRepository(private val walkApi: WalkApi) {
    
    suspend fun addWalk(request: AddWalkRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = walkApi.addWalk(request)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Walk added successfully")
            } else {
                Result.failure(Exception("Failed to add walk: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserWalks(userId: Int): Result<List<Walk>> = withContext(Dispatchers.IO) {
        try {
            val response = walkApi.getUserWalks(userId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get walks: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 