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

// Generic Place model for map display
data class Place(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val walkId: Int? = null,
    val storySegment: String? = null
)

data class DashboardErrorResponse(
    val errorDescription: String,
    val errorShortDescription: String,
    val errorCode: String
)

// Extension functions to convert to generic Place model
fun VisitedPlace.toPlace(): Place {
    return Place(
        name = "Walk ${this.walkId}",
        latitude = this.latitude,
        longitude = this.longitude,
        walkId = this.walkId,
        storySegment = this.storySegment
    )
}

fun NotVisitedPlace.toPlace(): Place {
    return Place(
        name = "Walk ${this.walkId}",
        latitude = this.latitude,
        longitude = this.longitude,
        walkId = this.walkId,
        storySegment = null
    )
}

// Helper functions to convert lists
fun visitedPlacesToPlaces(visitedPlaces: List<VisitedPlace>): List<Place> {
    return visitedPlaces.map { it.toPlace() }
}

fun notVisitedPlacesToPlaces(notVisitedPlaces: List<NotVisitedPlace>): List<Place> {
    return notVisitedPlaces.map { it.toPlace() }
}
