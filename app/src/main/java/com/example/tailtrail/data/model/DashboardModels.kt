package com.example.tailtrail.data.model

data class DashboardStats(
    val totalDistance: Int,
    val completedWalks: Int,
    val incompleteWalks: Int,
    val placesVisited: Int,
    val placesNotVisited: Int,
    val visitedPlaces: List<VisitedPlace>,
    val notVisitedPlaces: List<NotVisitedPlace>
)

data class VisitedPlace(
    val walkId: Int,
    val latitude: Double,
    val longitude: Double,
    val storySegment: String
)

data class NotVisitedPlace(
    val walkId: Int,
    val latitude: Double,
    val longitude: Double
)

data class DashboardErrorResponse(
    val errorDescription: String,
    val errorShortDescription: String,
    val errorCode: String
)
