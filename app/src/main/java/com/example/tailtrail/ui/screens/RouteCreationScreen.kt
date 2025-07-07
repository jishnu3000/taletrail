package com.example.tailtrail.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tailtrail.data.api.RouteStop
import com.example.tailtrail.data.api.WalkRequest
import com.example.tailtrail.ui.viewmodel.WalkViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Composable
fun RouteCreationScreen(
    navController: NavController,
    genre: String,
    viewModel: WalkViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var dropLocation by remember { mutableStateOf<LatLng?>(null) }
    var stops by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var stopDist by remember { mutableStateOf(100) }
    val userId = 4 // Hardcoded for now

    // Get current location
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                currentLocation = LatLng(it.latitude, it.longitude)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Genre: $genre", modifier = Modifier.padding(8.dp))
        Text("No of stops: ${stops.size}", modifier = Modifier.padding(8.dp))
        Text("Pickup: ${currentLocation?.latitude}, ${currentLocation?.longitude}", modifier = Modifier.padding(8.dp))
        Text("Drop: ${dropLocation?.latitude}, ${dropLocation?.longitude}", modifier = Modifier.padding(8.dp))

        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPositionState().position.copy(
                    target = currentLocation ?: LatLng(0.0, 0.0),
                    zoom = 15f
                )
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    if (dropLocation == null) {
                        dropLocation = latLng
                    } else {
                        stops = stops + latLng
                    }
                }
            ) {
                currentLocation?.let {
                    Marker(state = MarkerState(position = it), title = "Pickup")
                }
                dropLocation?.let {
                    Marker(state = MarkerState(position = it), title = "Drop")
                }
                stops.forEachIndexed { idx, stop ->
                    Marker(state = MarkerState(position = stop), title = "Stop ${idx + 1}")
                }
                // Draw polyline for route
                val routePoints = listOfNotNull(currentLocation) + stops + listOfNotNull(dropLocation)
                if (routePoints.size > 1) {
                    Polyline(points = routePoints)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                if (stops.isNotEmpty()) stops = stops.dropLast(1)
            }, enabled = stops.isNotEmpty()) {
                Text("Remove Last Stop")
            }
            Button(onClick = {
                dropLocation = null
                stops = emptyList()
            }) {
                Text("Reset Route")
            }
        }

        Button(
            onClick = {
                if (currentLocation != null && dropLocation != null) {
                    val route = (listOf(currentLocation!!) + stops + listOf(dropLocation!!)).mapIndexed { idx, latLng ->
                        RouteStop(order = idx + 1, latitude = latLng.latitude, longitude = latLng.longitude)
                    }
                    val walkRequest = WalkRequest(
                        userId = userId,
                        genre = genre,
                        stopDist = stopDist,
                        noOfStops = stops.size,
                        route = route
                    )
                    viewModel.addWalk(walkRequest)
                    navController.popBackStack("home", false)
                }
            },
            enabled = currentLocation != null && dropLocation != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Create Route")
        }
    }
} 