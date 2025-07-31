package com.example.tailtrail.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.tailtrail.data.model.RouteDetail
import com.example.tailtrail.data.util.LocationUtil
import com.example.tailtrail.data.util.GeocodingUtil
import com.example.tailtrail.ui.viewmodel.WalkViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkDetailsScreen(
    navController: NavHostController,
    walkViewModel: WalkViewModel,
    walkId: Int,
    userId: Int
) {
    println("WalkDetailsScreen: Starting WalkDetailsScreen composable")
    System.out.println("=== WalkDetailsScreen: Starting WalkDetailsScreen composable ===")
    
    val context = LocalContext.current
    val walkDetails by walkViewModel.walkDetails.collectAsState()
    val isLoading by walkViewModel.isLoadingDetails.collectAsState()
    val error by walkViewModel.detailsError.collectAsState()
    val isCheckingIn by walkViewModel.isCheckingIn.collectAsState()
    val checkInError by walkViewModel.checkInError.collectAsState()
    val checkInSuccess by walkViewModel.checkInSuccess.collectAsState()
    
    // Location states
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    println("WalkDetailsScreen: Initialized with walkId=$walkId, userId=$userId")
    System.out.println("=== WalkDetailsScreen: Initialized with walkId=$walkId, userId=$userId ===")
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        println("WalkDetailsScreen: Permission result received: $permissions")
        System.out.println("=== WalkDetailsScreen: Permission result received: $permissions ===")
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        println("WalkDetailsScreen: hasLocationPermission set to $hasLocationPermission")
        System.out.println("=== WalkDetailsScreen: hasLocationPermission set to $hasLocationPermission ===")
    }
    
    // Request location permission on first load
    LaunchedEffect(Unit) {
        println("WalkDetailsScreen: LaunchedEffect Unit triggered")
        System.out.println("=== WalkDetailsScreen: LaunchedEffect Unit triggered ===")
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            println("WalkDetailsScreen: Location permission already granted")
            System.out.println("=== WalkDetailsScreen: Location permission already granted ===")
            hasLocationPermission = true
        } else {
            println("WalkDetailsScreen: Requesting location permission")
            System.out.println("=== WalkDetailsScreen: Requesting location permission ===")
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // Get current location when needed for check-in
    LaunchedEffect(hasLocationPermission) {
        println("WalkDetailsScreen: LaunchedEffect hasLocationPermission=$hasLocationPermission")
        System.out.println("=== WalkDetailsScreen: LaunchedEffect hasLocationPermission=$hasLocationPermission ===")
        
        if (hasLocationPermission) {
            try {
                println("WalkDetailsScreen: Getting current location")
                System.out.println("=== WalkDetailsScreen: Getting current location ===")
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        println("WalkDetailsScreen: Current location received: ${it.latitude}, ${it.longitude}")
                        System.out.println("=== WalkDetailsScreen: Current location received: ${it.latitude}, ${it.longitude} ===")
                        currentLocation = it
                    } ?: run {
                        println("WalkDetailsScreen: No location available")
                        System.out.println("=== WalkDetailsScreen: No location available ===")
                    }
                }.addOnFailureListener { exception ->
                    println("WalkDetailsScreen: Failed to get location: ${exception.message}")
                    System.out.println("=== WalkDetailsScreen: Failed to get location: ${exception.message} ===")
                }
            } catch (e: SecurityException) {
                println("WalkDetailsScreen: SecurityException getting location: ${e.message}")
                System.out.println("=== WalkDetailsScreen: SecurityException getting location: ${e.message} ===")
            }
        }
    }
    
    // Load walk details
    LaunchedEffect(walkId) {
        try {
            println("WalkDetailsScreen: LaunchedEffect walkId=$walkId")
            System.out.println("=== WalkDetailsScreen: LaunchedEffect walkId=$walkId ===")
            
            // Clear previous data
            walkViewModel.clearWalkDetails()
            walkViewModel.clearDetailsError()
            walkViewModel.clearCheckInError()
            walkViewModel.clearCheckInSuccess()
            
            if (walkId > 0) {
                println("WalkDetailsScreen: Loading walk details for walkId=$walkId")
                System.out.println("=== WalkDetailsScreen: Loading walk details for walkId=$walkId ===")
                walkViewModel.loadWalkDetails(walkId)
            } else {
                println("WalkDetailsScreen: Invalid walkId=$walkId")
                System.out.println("=== WalkDetailsScreen: Invalid walkId=$walkId ===")
            }
        } catch (e: Exception) {
            println("WalkDetailsScreen: Exception in walkId LaunchedEffect: ${e.message}")
            System.out.println("=== WalkDetailsScreen: Exception in walkId LaunchedEffect: ${e.message} ===")
            e.printStackTrace()
        }
    }
    
    // Periodic location updates for check-in (separate LaunchedEffect)
    LaunchedEffect(hasLocationPermission, walkId) {
        if (hasLocationPermission && walkId > 0) {
            while (true) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            currentLocation = it
                        }
                    }
                } catch (e: SecurityException) {
                    // Handle permission denied
                }
                delay(5000) // Update every 5 seconds
            }
        }
    }
    
                    // Handle check-in button click
                val onCheckIn: (Double, Double, Int) -> Unit = { lat, lng, routeId ->
                    try {
                        println("WalkDetailsScreen: Check-in clicked for lat=$lat, lng=$lng, routeId=$routeId")
                        
                        if (walkDetails != null) {
                            println("WalkDetailsScreen: Calling checkIn for routeId=$routeId")
                            walkViewModel.checkIn(userId, walkId, routeId, lat, lng)
                        } else {
                            println("WalkDetailsScreen: No walk details available")
                        }
                    } catch (e: Exception) {
                        println("WalkDetailsScreen: Exception in onCheckIn: ${e.message}")
                        e.printStackTrace()
                    }
                }
    
    // Show error message if any
    LaunchedEffect(checkInError) {
        checkInError?.let { error ->
            println("WalkDetailsScreen: Check-in error: $error")
            System.out.println("=== WalkDetailsScreen: Check-in error: $error ===")
            try {
                // Clear error after 5 seconds
                delay(5000)
                walkViewModel.clearCheckInError()
            } catch (e: Exception) {
                println("WalkDetailsScreen: Exception clearing error: ${e.message}")
                System.out.println("=== WalkDetailsScreen: Exception clearing error: ${e.message} ===")
            }
        }
    }
    
                    // Show success message if any and reload walk details
                LaunchedEffect(checkInSuccess) {
                    checkInSuccess?.let { success ->
                        println("WalkDetailsScreen: Check-in success: $success")
                        
                        // Reload walk details to get updated status
                        walkViewModel.loadWalkDetails(walkId)
                        
                        // Clear success after 3 seconds
                        delay(3000)
                        walkViewModel.clearCheckInSuccess()
                    }
                }
    
    // UI Content
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Walk Details",
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE0E0E0))
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF673AB7)
                        )
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error ?: "Unknown error",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                walkDetails != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            WalkInfoCard(walkDetails!!)
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            WalkProgressCard(walkDetails!!)
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            LocationStatusCard(hasLocationPermission, currentLocation)
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Route Points",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF673AB7)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        items(walkDetails!!.routes) { route ->
                            RoutePointCard(
                                route = route,
                                isCheckingIn = isCheckingIn == route.routeId,
                                                                    onCheckIn = onCheckIn,
                                    currentLocation = currentLocation,
                                    hasLocationPermission = hasLocationPermission,
                                    walkViewModel = walkViewModel
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No walk details available",
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Show Snackbar for check-in responses
            checkInError?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { walkViewModel.clearCheckInError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
            
            checkInSuccess?.let { success ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { walkViewModel.clearCheckInSuccess() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(success)
                }
            }
        }
    }
}

@Composable
fun WalkInfoCard(walkDetails: com.example.tailtrail.data.model.WalkDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = walkDetails.genre,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                    Text(
                        text = "Walk #${walkDetails.walkId}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                
                StatusChip(walkDetails.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoItem(
                    icon = Icons.Default.Place,
                    label = "Stops",
                    value = "${walkDetails.noOfStops}"
                )
                InfoItem(
                    icon = Icons.Default.DirectionsWalk,
                    label = "Distance",
                    value = "${walkDetails.stopDistance}m"
                )
            }
        }
    }
}

@Composable
fun WalkProgressCard(walkDetails: com.example.tailtrail.data.model.WalkDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressItem(
                    icon = Icons.Default.LockOpen,
                    label = "Unlocked",
                    value = "${walkDetails.placesUnlocked}",
                    color = Color(0xFF4CAF50)
                )
                ProgressItem(
                    icon = Icons.Default.Lock,
                    label = "Locked",
                    value = "${walkDetails.placesLocked}",
                    color = Color(0xFFF44336)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = if (walkDetails.noOfStops > 0) {
                    walkDetails.placesUnlocked.toFloat() / walkDetails.noOfStops.toFloat()
                } else 0f,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun LocationStatusCard(
    hasLocationPermission: Boolean,
    currentLocation: Location?
) {
    var currentAddress by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    // Get address for current location
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            val address = try {
                GeocodingUtil.getAddressFromCoordinates(
                    context = context,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } catch (e: Exception) {
                println("Error getting current location address: ${e.message}")
                "Location: ${location.latitude}, ${location.longitude}"
            }
            currentAddress = address
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasLocationPermission) Icons.Default.LocationOn else Icons.Default.LocationOff,
                    contentDescription = "Location Status",
                    tint = if (hasLocationPermission) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (hasLocationPermission) "Location Active" else "Location Disabled",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasLocationPermission) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            if (hasLocationPermission && currentLocation != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                if (currentAddress != null) {
                    Text(
                        text = currentAddress!!,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = "Loading address...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun RoutePointCard(
    route: RouteDetail,
    isCheckingIn: Boolean,
    onCheckIn: (Double, Double, Int) -> Unit,
    currentLocation: Location?,
    hasLocationPermission: Boolean,
    walkViewModel: WalkViewModel
) {
    println("RoutePointCard: Rendering route ${route.routeId}, isCheckingIn=$isCheckingIn, hasLocationPermission=$hasLocationPermission, currentLocation=${currentLocation != null}")
    
    val showStoryForRouteId by walkViewModel.showStoryForRouteId.collectAsState()
    val showStory = showStoryForRouteId == route.routeId
    
    // Get address for this route point
    var routeAddress by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    // Text-to-speech state
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var availableVoices by remember { mutableStateOf<List<android.speech.tts.Voice>>(emptyList()) }
    var selectedVoice by remember { mutableStateOf<android.speech.tts.Voice?>(null) }
    var showVoicePicker by remember { mutableStateOf(false) }
    
    // Initialize text-to-speech
    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Get available voices
                val voices = textToSpeech?.voices?.filter { voice ->
                    voice.isNetworkConnectionRequired == false && voice.quality >= android.speech.tts.Voice.QUALITY_NORMAL
                } ?: emptyList()
                availableVoices = voices
                
                // Set default voice (first available or system default)
                selectedVoice = voices.firstOrNull() ?: textToSpeech?.defaultVoice
                
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                    }
                    
                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                    }
                })
            }
        }
    }
    
    // Get address for this route point
    LaunchedEffect(route.latitude, route.longitude) {
        val address = try {
            GeocodingUtil.getAddressFromCoordinates(
                context = context,
                latitude = route.latitude,
                longitude = route.longitude
            )
        } catch (e: Exception) {
            println("Error getting route address: ${e.message}")
            "Location: ${route.latitude}, ${route.longitude}"
        }
        routeAddress = address
    }
    
    // Cleanup text-to-speech
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.shutdown()
        }
    }
    
    // Function to open Google Maps with directions to this stop
    fun openGoogleMapsDirections(latitude: Double, longitude: Double) {
        try {
            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback to any map app
                val fallbackIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            println("Error opening Google Maps: ${e.message}")
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (route.lockStatus == 1) Color(0xFFE8F5E8) else Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (route.lockStatus == 1) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = if (route.lockStatus == 1) "Unlocked" else "Locked",
                        tint = if (route.lockStatus == 1) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Stop ${route.order}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                }
                
                // Navigation button to open Google Maps
                IconButton(
                    onClick = { openGoogleMapsDirections(route.latitude, route.longitude) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navigate to this stop",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (routeAddress != null) {
                Text(
                    text = routeAddress!!,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "Loading address...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Show check button only for locked stops
            if (route.lockStatus == 0 && hasLocationPermission && currentLocation != null) {
                println("RoutePointCard: Showing check button for route ${route.routeId}")
                System.out.println("=== RoutePointCard: Showing check button for route ${route.routeId} ===")
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { 
                        println("RoutePointCard: Check button clicked for route ${route.routeId}")
                        System.out.println("=== RoutePointCard: Check button clicked for route ${route.routeId} ===")
                        try {
                            if (currentLocation != null) {
                                System.out.println("=== RoutePointCard: About to call onCheckIn with lat=${currentLocation.latitude}, lng=${currentLocation.longitude} ===")
                                onCheckIn(currentLocation.latitude, currentLocation.longitude, route.routeId)
                                System.out.println("=== RoutePointCard: onCheckIn call completed successfully ===")
                            } else {
                                println("RoutePointCard: Current location is null")
                                System.out.println("=== RoutePointCard: Current location is null ===")
                            }
                        } catch (e: Exception) {
                            println("RoutePointCard: Exception in button onClick: ${e.message}")
                            System.out.println("=== RoutePointCard: Exception in button onClick: ${e.message} ===")
                            e.printStackTrace()
                        }
                    },
                    enabled = !isCheckingIn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (isCheckingIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Check")
                }
            } else if (route.lockStatus == 0) {
                // Show message when location permission is not available
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Location permission needed to check in",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            // Show "View Story" and "Listen" buttons for unlocked stops with story
            if (route.lockStatus == 1 && route.storySegment != null) {
                println("RoutePointCard: Showing view story button for route ${route.routeId}")
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // View Story Button
                    Button(
                        onClick = { 
                            val newValue = if (showStory) null else route.routeId
                            println("RoutePointCard: View story button clicked for route ${route.routeId}, newValue=$newValue")
                            walkViewModel.setShowStoryForRoute(newValue)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        )
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = "View Story",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showStory) "Hide Story" else "View Story")
                    }
                    
                    // Listen Button
                    Button(
                        onClick = { 
                            route.storySegment?.let { story ->
                                textToSpeech?.speak(
                                    story,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "story_${route.routeId}"
                                )
                            }
                        },
                        enabled = !isSpeaking && textToSpeech != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpeaking) Color(0xFF9E9E9E) else Color(0xFF2196F3)
                        )
                    ) {
                        Icon(
                            if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop" else "Listen",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSpeaking) "Stop" else "Listen")
                    }
                }
            }
            
            // Show story segment if unlocked and story is available
            if (route.lockStatus == 1 && route.storySegment != null && showStory) {
                println("RoutePointCard: Showing story segment for route ${route.routeId}")
                System.out.println("=== RoutePointCard: Showing story segment for route ${route.routeId} ===")
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Story Segment",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF673AB7)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = route.storySegment,
                            fontSize = 14.sp,
                            color = Color.Black,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val backgroundColor = when (status.uppercase()) {
        "COMPLETED" -> Color(0xFF4CAF50)
        "IN-PROGRESS" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF673AB7),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF673AB7)
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ProgressItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
} 