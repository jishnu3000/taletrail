package com.example.tailtrail.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.tailtrail.data.model.RoutePoint
import com.example.tailtrail.data.util.GeocodingUtil
import com.example.tailtrail.ui.viewmodel.WalkViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.LocationManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlanningScreen(
    navController: NavHostController,
    genre: String,
    walkViewModel: WalkViewModel,
    userId: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var stops by remember { mutableStateOf<List<RoutePoint>>(emptyList()) }
    var stopAddresses by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var googleMapObj by remember { mutableStateOf<GoogleMap?>(null) }
    var customMarkers by remember { mutableStateOf<MutableList<com.google.android.gms.maps.model.Marker>>(mutableListOf()) }
    var updateMarkersFunction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var mapError by remember { mutableStateOf<String?>(null) }
    
    // Address states
    var currentLocationAddress by remember { mutableStateOf<String>("") }
    var stopDist by remember { mutableStateOf("100") }
    var showInstructions by remember { mutableStateOf(false) }
    var isGpsEnabled by remember { mutableStateOf(false) }
    var locationStatus by remember { mutableStateOf("Checking location...") }
    

    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationManager = remember { context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasLocationPermission = true
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    LaunchedEffect(hasLocationPermission) {
        // Check if GPS is enabled
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                      locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        if (!isGpsEnabled) {
            locationStatus = "GPS is disabled. Please enable location services."
            println("RoutePlanningScreen: GPS is disabled")
        } else {
            locationStatus = "GPS is enabled"
            println("RoutePlanningScreen: GPS is enabled")
        }
        
        if (hasLocationPermission) {
            try {
                println("RoutePlanningScreen: Requesting last location...")
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        println("RoutePlanningScreen: Got location - lat: ${location.latitude}, lng: ${location.longitude}")
                        currentLocation = LatLng(location.latitude, location.longitude)
                        // Get address for current location
                        scope.launch {
                            currentLocationAddress = GeocodingUtil.getAddressFromCoordinates(
                                context, location.latitude, location.longitude
                            )
                            println("RoutePlanningScreen: Current location address: $currentLocationAddress")
                        }
                        
                        // Zoom to current location when it becomes available
                        googleMapObj?.let { map ->
                            println("RoutePlanningScreen: Moving camera to current location")
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 17f))
                            
                            // Force enable my-location when we have a location
                            if (hasLocationPermission) {
                                try {
                                    map.isMyLocationEnabled = true
                                    println("RoutePlanningScreen: My location enabled successfully")
                                } catch (e: SecurityException) {
                                    println("RoutePlanningScreen: SecurityException enabling my location: ${e.message}")
                                }
                            }
                        }
                    } else {
                        println("RoutePlanningScreen: Location is null - GPS might be disabled or no recent location")
                        // Try to get location updates if last location is null
                        try {
                            fusedLocationClient.requestLocationUpdates(
                                com.google.android.gms.location.LocationRequest.Builder(1000).build(),
                                object : com.google.android.gms.location.LocationCallback() {
                                    override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                                        locationResult.lastLocation?.let { location ->
                                            println("RoutePlanningScreen: Got location from updates - lat: ${location.latitude}, lng: ${location.longitude}")
                                            currentLocation = LatLng(location.latitude, location.longitude)
                                            scope.launch {
                                                currentLocationAddress = GeocodingUtil.getAddressFromCoordinates(
                                                    context, location.latitude, location.longitude
                                                )
                                            }
                                            googleMapObj?.let { map ->
                                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 17f))
                                                try {
                                                    map.isMyLocationEnabled = true
                                                } catch (e: SecurityException) {
                                                    println("RoutePlanningScreen: SecurityException in location updates: ${e.message}")
                                                }
                                            }
                                            fusedLocationClient.removeLocationUpdates(this)
                                        }
                                    }
                                },
                                null
                            )
                        } catch (e: SecurityException) {
                            println("RoutePlanningScreen: SecurityException requesting location updates: ${e.message}")
                        }
                    }
                }.addOnFailureListener { exception ->
                    println("RoutePlanningScreen: Failed to get location: ${exception.message}")
                }
            } catch (e: SecurityException) {
                println("RoutePlanningScreen: SecurityException in location setup: ${e.message}")
            }
        } else {
            println("RoutePlanningScreen: No location permission granted")
        }
    }
    
    // Enable my-location feature when permissions are granted
    LaunchedEffect(hasLocationPermission, googleMapObj) {
        if (hasLocationPermission && googleMapObj != null) {
            try {
                googleMapObj?.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                // Handle permission denied
            }
        }
    }
    
    Scaffold(
        containerColor = Color(0xFFE0E0E0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Plan Route - $genre",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInstructions = !showInstructions }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Instructions",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF673AB7)
                )
            )
        },
        floatingActionButton = {
            Column {
                // Only show Save button if at least one stop is present
                if (stops.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = {
                            val route = mutableListOf<RoutePoint>()
                            // Only add user-selected stops (orders 1, 2, 3, etc.)
                            stops.forEachIndexed { index, stop ->
                                route.add(stop.copy(order = index + 1))
                            }
                            if (route.isNotEmpty()) {
                                // Send route with only user-selected stops
                                walkViewModel.addWalk(userId, genre, route, stopDist.toIntOrNull() ?: 100, stops.size)
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(Icons.Default.Check, "Save Route")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            mapView = this
                            getMapAsync { googleMap ->
                                try {
                                    googleMapObj = googleMap
                                    
                                    // Basic map settings
                                    googleMap.uiSettings.isZoomControlsEnabled = true
                                    googleMap.uiSettings.isMyLocationButtonEnabled = true
                                    googleMap.uiSettings.isCompassEnabled = true
                                    googleMap.uiSettings.isMapToolbarEnabled = true
                                
                                    // Enable my-location feature if permissions are granted
                                    if (hasLocationPermission) {
                                        try {
                                            googleMap.isMyLocationEnabled = true
                                            // Force enable my-location button
                                            googleMap.uiSettings.isMyLocationButtonEnabled = true
                                        } catch (e: SecurityException) {
                                            // Handle permission denied
                                        }
                                    }
                                    
                                    // Also enable my-location button
                                    googleMap.uiSettings.isMyLocationButtonEnabled = true
                                    
                                    // Enable my-location after a short delay to ensure map is ready
                                    scope.launch {
                                        kotlinx.coroutines.delay(1000)
                                        if (hasLocationPermission) {
                                            try {
                                                googleMap.isMyLocationEnabled = true
                                            } catch (e: SecurityException) {
                                                // Handle permission denied
                                            }
                                        }
                                        
                                        // Try again after 2 seconds
                                        kotlinx.coroutines.delay(1000)
                                        if (hasLocationPermission) {
                                            try {
                                                googleMap.isMyLocationEnabled = true
                                            } catch (e: SecurityException) {
                                                // Handle permission denied
                                            }
                                        }
                                    }

                                    // Move camera to current location if available, otherwise show a default location
                                    currentLocation?.let { location ->
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
                                        
                                        // Add a custom current location marker as fallback
                                        googleMap.addMarker(
                                            MarkerOptions()
                                                .position(location)
                                                .title("Current Location")
                                                .snippet("You are here")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                        )
                                    } ?: run {
                                        // Fallback to a default location (e.g., city center)
                                        val defaultLocation = LatLng(40.7128, -74.0060) // New York City
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
                                        
                                        // Add a test marker to verify map is working
                                        googleMap.addMarker(
                                            MarkerOptions()
                                                .position(defaultLocation)
                                                .title("Test Location")
                                                .snippet("Map is working!")
                                        )
                                    }
                                    
                                    // Draw all markers function
                                    fun updateMarkers() {
                                        // Clear only custom markers, preserve built-in my-location
                                        customMarkers.forEach { it.remove() }
                                        customMarkers = mutableListOf()
                                        
                                        // Add current location marker if available
                                        currentLocation?.let { location ->
                                            val currentLocationMarker = googleMap.addMarker(
                                                MarkerOptions()
                                                    .position(location)
                                                    .title("Current Location")
                                                    .snippet("You are here")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                            )
                                            currentLocationMarker?.let { customMarkers.add(it) }
                                        }
                                        
                                        // Add markers for user-selected stops
                                        stops.forEach { stop ->
                                            val stopLatLng = LatLng(stop.latitude, stop.longitude)
                                            val marker = googleMap.addMarker(
                                                MarkerOptions()
                                                    .position(stopLatLng)
                                                    .title("Stop ${stop.order}")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                            )
                                            marker?.let { customMarkers.add(it) }
                                        }
                                    }
                                    
                                    // Store the updateMarkers function for external use
                                    updateMarkersFunction = { updateMarkers() }
                                    
                                    // Set up map click listener for adding stops
                                    googleMap.setOnMapClickListener { latLng ->
                                        val newStop = RoutePoint(stops.size + 1, latLng.latitude, latLng.longitude)
                                        stops = stops + newStop
                                        
                                        // Get address for the new stop
                                        scope.launch {
                                            val address = GeocodingUtil.getAddressFromCoordinates(context, latLng.latitude, latLng.longitude)
                                            stopAddresses = stopAddresses + (newStop.order to address)
                                        }
                                        
                                        // Update all markers
                                        updateMarkers()
                                    }
                                    
                                    // Initial markers
                                    updateMarkers()

                                    // Listen for my location button click
                                    googleMap.setOnMyLocationButtonClickListener {
                                        currentLocation?.let { location ->
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
                                            updateMarkers()
                                        }
                                        true
                                    }
                                } catch (e: Exception) {
                                    // Handle any map initialization errors
                                    e.printStackTrace()
                                    mapError = e.message ?: "Unknown map error"
                                }
                            }
                        }
                    }
                )
                
                // Loading indicator or error
                if (googleMapObj == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (mapError != null) {
                                Text(
                                    text = "Map Error: $mapError",
                                    fontSize = 14.sp,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Please check your internet connection and try again",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "Loading map...",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
            
            // Route Information
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Instructions (collapsible)
                if (showInstructions) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Instructions:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF673AB7)
                                )
                                
                                Text(
                                    text = "1. Tap the map to add a stop",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                
                                Text(
                                    text = "2. Select stops on map",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                
                                Text(
                                    text = "3. Tap ✓ to save your route",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                
                                Text(
                                    text = "4. Tap the location button (top-right) to show your position",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
                
                // Location Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentLocation != null) Color(0xFFE8F5E8) else Color(0xFFFFF3E0)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentLocation != null) Icons.Default.LocationOn else Icons.Default.LocationOff,
                                    contentDescription = "Location Status",
                                    tint = if (currentLocation != null) Color.Green else Color(0xFFFF9800),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLocation != null) "Location Found" else "Location Status",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentLocation != null) Color.Green else Color(0xFFFF9800)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (currentLocation != null) {
                                currentLocation?.let { location ->
                                    Text(
                                        text = "Current Location: ${if (currentLocationAddress.isNotEmpty()) currentLocationAddress else "${location.latitude}, ${location.longitude}"}",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            } else {
                                Text(
                                    text = locationStatus,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                
                                if (!isGpsEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            // Open location settings
                                            val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                    ) {
                                        Text("Enable Location Services")
                                    }
                                }
                                
                                if (!hasLocationPermission) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text("Grant Location Permission")
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Permission: ${if (hasLocationPermission) "✓ Granted" else "✗ Denied"}",
                                fontSize = 12.sp,
                                color = if (hasLocationPermission) Color.Green else Color.Red
                            )
                            
                            Text(
                                text = "GPS: ${if (isGpsEnabled) "✓ Enabled" else "✗ Disabled"}",
                                fontSize = 12.sp,
                                color = if (isGpsEnabled) Color.Green else Color.Red
                            )
                        }
                    }
                }
                
                // Route Settings Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Route Settings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF673AB7)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = stopDist,
                                onValueChange = { stopDist = it },
                                label = { Text("Stop Distance (meters)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Selected Stops: ${stops.size}",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Stops",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (stops.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No stops selected. Tap the map to add stops.",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(stops) { index, stop ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Stop ${stop.order}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF673AB7)
                                    )
                                    Text(
                                        text = stopAddresses[stop.order] ?: "${stop.latitude}, ${stop.longitude}",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        val stopToRemove = stops[index]
                                        stops = stops.filterIndexed { i, _ -> i != index }
                                            .mapIndexed { i, s -> s.copy(order = i + 1) }
                                        
                                        // Update addresses for remaining stops with correct order
                                        val newStopAddresses = mutableMapOf<Int, String>()
                                        val oldAddresses = stopAddresses.toList()
                                        stops.forEachIndexed { newIndex, stop ->
                                            if (newIndex < oldAddresses.size) {
                                                newStopAddresses[stop.order] = oldAddresses[newIndex].second
                                            }
                                        }
                                        stopAddresses = newStopAddresses
                                        
                                        // Update map markers using the updateMarkers function
                                        updateMarkersFunction?.invoke()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Stop",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}