package com.example.tailtrail.data.model

data class Walk(
    val walkId: Int,
    val genre: String,
    val noOfStops: Int,
    val stopDist: Int
)

data class RoutePoint(
    val order: Int,
    val latitude: Double,
    val longitude: Double
)

data class AddWalkRequest(
    val userId: Int,
    val genre: String,
    val stopDist: Int,
    val noOfStops: Int,
    val route: List<RoutePoint>
)

data class AddWalkResponse(
    val message: String
)

data class ErrorResponse(
    val errorDescription: String,
    val errorShortDescription: String,
    val errorCode: String
) 