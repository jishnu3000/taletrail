package com.example.tailtrail.data.repository

import com.example.tailtrail.data.api.WalkApi
import com.example.tailtrail.data.model.AddWalkRequest
import com.example.tailtrail.data.model.Walk
import com.example.tailtrail.data.model.WalkDetails
import com.example.tailtrail.data.model.CheckInResponse
import com.example.tailtrail.data.model.DashboardStats
import com.example.tailtrail.data.model.DashboardErrorResponse
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
    
    suspend fun getWalkDetails(walkId: Int): Result<WalkDetails> = withContext(Dispatchers.IO) {
        try {
            val response = walkApi.getWalkDetails(walkId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to get walk details: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkIn(userLat: Double, userLng: Double, routeId: Int): Result<CheckInResponse> = withContext(Dispatchers.IO) {
        try {
            println("Attempting check-in with userLat: $userLat, userLng: $userLng, routeId: $routeId")
            
            // Validate input parameters
            if (routeId <= 0) {
                return@withContext Result.failure(Exception("Invalid route ID"))
            }
            
            if (userLat == 0.0 && userLng == 0.0) {
                return@withContext Result.failure(Exception("Invalid location coordinates"))
            }
            
            val response = walkApi.checkIn(userLat, userLng, routeId)
            println("Check-in response code: ${response.code()}")
            println("Check-in response body: ${response.body()}")
            println("Check-in error body: ${response.errorBody()?.string()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Result.success(responseBody)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Check-in endpoint not found. Please verify the API endpoint."
                    400 -> "Invalid request data. Please check your location."
                    401 -> "Unauthorized. Please log in again."
                    500 -> "Server error. Please try again later."
                    else -> "Failed to check in: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("Check-in exception in repository: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getDashboardStats(userId: Int): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            println("Fetching dashboard stats for userId: $userId")
            
            val response = walkApi.getDashboardStats(userId)
            println("Dashboard stats response code: ${response.code()}")
            println("Dashboard stats response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Result.success(responseBody)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    400 -> {
                        // Try to parse error response
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null) {
                            try {
                                val gson = com.google.gson.Gson()
                                val errorResponse = gson.fromJson(errorBody, DashboardErrorResponse::class.java)
                                errorResponse.errorDescription
                            } catch (e: Exception) {
                                "Invalid data provided"
                            }
                        } else {
                            "Invalid data provided"
                        }
                    }
                    404 -> "User not found"
                    500 -> "Server error occurred"
                    else -> "Failed to fetch dashboard stats: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("Dashboard stats exception: ${e.message}")
            Result.failure(e)
        }
    }
} 