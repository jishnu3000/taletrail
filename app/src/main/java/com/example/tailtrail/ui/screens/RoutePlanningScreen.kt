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
    
    // Address states
    var currentLocationAddress by remember { mutableStateOf<String>("") }
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
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
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        // Get address for current location
                        scope.launch {
                            currentLocationAddress = GeocodingUtil.getAddressFromCoordinates(
                                context, it.latitude, it.longitude
                            )
                        }
                    }
                }
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
                            // Add current location as first point
                            currentLocation?.let { location ->
                                route.add(RoutePoint(1, location.latitude, location.longitude))
                            }
                            // Add stops
                            stops.forEachIndexed { index, stop ->
                                route.add(stop.copy(order = index + 2))
                            }
                            if (route.isNotEmpty()) {
                                walkViewModel.addWalk(userId, genre, route)
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
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        mapView = this
                        getMapAsync { googleMap ->
                            googleMapObj = googleMap
                            googleMap.uiSettings.isZoomControlsEnabled = true
                            googleMap.uiSettings.isMyLocationButtonEnabled = true
                            googleMap.isMyLocationEnabled = hasLocationPermission

                            // Move camera to current location if available
                            currentLocation?.let { location ->
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            }

                            // Draw all markers
                            fun updateMarkers() {
                                googleMap.clear()
                                // Current location marker
                                currentLocation?.let { location ->
                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(location)
                                            .title("Current Location")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    )
                                }
                                // Stops markers
                                stops.forEach { stop ->
                                    val stopLatLng = LatLng(stop.latitude, stop.longitude)
                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(stopLatLng)
                                            .title("Stop ${stop.order}")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                    )
                                }
                            }
                            updateMarkers()

                            // Listen for map clicks to add a stop
                            googleMap.setOnMapClickListener { latLng ->
                                val newStop = RoutePoint(
                                    order = stops.size + 1,
                                    latitude = latLng.latitude,
                                    longitude = latLng.longitude
                                )
                                stops = stops + newStop
                                // Get address for the new stop
                                scope.launch {
                                    val address = GeocodingUtil.getAddressFromCoordinates(
                                        context, latLng.latitude, latLng.longitude
                                    )
                                    stopAddresses = stopAddresses + (newStop.order to address)
                                }
                                updateMarkers()
                            }

                            // Listen for my location button click
                            googleMap.setOnMyLocationButtonClickListener {
                                currentLocation?.let { location ->
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
                                    updateMarkers()
                                }
                                true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            
            // Route Information
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
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
                                text = "Route Information",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF673AB7)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
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
                                text = "2. Use the + button to add stops from current location",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                            
                            Text(
                                text = "3. Tap ✓ to save your route",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            currentLocation?.let { location ->
                                Text(
                                    text = "Current Location: ${if (currentLocationAddress.isNotEmpty()) currentLocationAddress else "${location.latitude}, ${location.longitude}"}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                            
                            Text(
                                text = "Stops: ${stops.size}",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                
                if (stops.isNotEmpty()) {
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