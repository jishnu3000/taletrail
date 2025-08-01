package com.example.tailtrail.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tailtrail.data.model.DashboardStats
import com.example.tailtrail.data.model.VisitedPlace
import com.example.tailtrail.data.model.NotVisitedPlace
import com.example.tailtrail.viewmodel.DashboardViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    userId: Int,
    onNavigateToProfile: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        dashboardViewModel.fetchDashboardStats(userId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFE0E0E0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF673AB7)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onNavigateToHome,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF673AB7),
                        selectedTextColor = Color(0xFF673AB7),
                        indicatorColor = Color(0xFF673AB7).copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF673AB7),
                        selectedTextColor = Color(0xFF673AB7),
                        indicatorColor = Color(0xFF673AB7).copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF673AB7),
                        selectedTextColor = Color(0xFF673AB7),
                        indicatorColor = Color(0xFF673AB7).copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A5ACD),
                            Color(0xFF483D8B)
                        )
                    )
                )
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Button(
                                onClick = { dashboardViewModel.fetchDashboardStats(userId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF673AB7)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.stats != null -> {
                    DashboardContent(
                        stats = uiState.stats!!,
                        dashboardViewModel = dashboardViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    stats: DashboardStats,
    dashboardViewModel: DashboardViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Cards
        item {
            StatisticsSection(stats = stats, dashboardViewModel = dashboardViewModel)
        }

        // Progress Bars Section
        item {
            ProgressSection(stats = stats, dashboardViewModel = dashboardViewModel)
        }

        // Places Map Section
        item {
            PlacesMapSection(
                visitedPlaces = stats.visitedPlaces,
                notVisitedPlaces = stats.notVisitedPlaces
            )
        }
    }
}

@Composable
fun StatisticsSection(
    stats: DashboardStats,
    dashboardViewModel: DashboardViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    icon = Icons.Default.TrendingUp,
                    title = "Total Distance",
                    value = dashboardViewModel.formatDistance(stats.totalDistance),
                    color = Color(0xFF4CAF50)
                )
                
                StatCard(
                    icon = Icons.Default.DirectionsWalk,
                    title = "Walks Completed",
                    value = "${stats.completedWalks}",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.size(120.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProgressSection(
    stats: DashboardStats,
    dashboardViewModel: DashboardViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Progress Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7)
            )
            
            // Walks Progress
            ProgressItem(
                title = "Walks Progress",
                completed = stats.completedWalks,
                total = stats.completedWalks + stats.incompleteWalks,
                color = Color(0xFF4CAF50),
                dashboardViewModel = dashboardViewModel
            )
            
            // Places Progress
            ProgressItem(
                title = "Places Visited",
                completed = stats.placesVisited,
                total = stats.placesVisited + stats.placesNotVisited,
                color = Color(0xFF2196F3),
                dashboardViewModel = dashboardViewModel
            )
        }
    }
}

@Composable
fun ProgressItem(
    title: String,
    completed: Int,
    total: Int,
    color: Color,
    dashboardViewModel: DashboardViewModel
) {
    val progress = dashboardViewModel.getProgressPercentage(completed, total)
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = "$completed/$total",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun PlacesMapSection(
    visitedPlaces: List<VisitedPlace>,
    notVisitedPlaces: List<NotVisitedPlace>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visited Places Map
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Visited Places (${visitedPlaces.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                }
                
                AndroidView(
                    factory = { context ->
                        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            
                            // Set default center
                            controller.setCenter(GeoPoint(12.9716, 77.5946))
                            controller.setZoom(10.0)
                            
                            // Add markers for visited places only
                            visitedPlaces.forEach { place ->
                                val marker = Marker(this)
                                marker.position = GeoPoint(place.latitude, place.longitude)
                                marker.title = "Walk ${place.walkId} - ${place.storySegment}"
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                
                                // Create green marker for visited places
                                try {
                                    val greenMarkerDrawable = android.graphics.drawable.GradientDrawable()
                                    greenMarkerDrawable.shape = android.graphics.drawable.GradientDrawable.OVAL
                                    greenMarkerDrawable.setColor(android.graphics.Color.GREEN)
                                    greenMarkerDrawable.setSize(30, 30)
                                    marker.icon = greenMarkerDrawable
                                } catch (e: Exception) {
                                    // Fallback to default marker if custom creation fails
                                    marker.icon = null
                                }
                                
                                overlays.add(marker)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
        
        // Not Visited Places Map
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Places to Visit (${notVisitedPlaces.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                }
                
                AndroidView(
                    factory = { context ->
                        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            
                            // Set default center
                            controller.setCenter(GeoPoint(12.9716, 77.5946))
                            controller.setZoom(10.0)
                            
                            // Add markers for not visited places only
                            notVisitedPlaces.forEach { place ->
                                val marker = Marker(this)
                                marker.position = GeoPoint(place.latitude, place.longitude)
                                marker.title = "Walk ${place.walkId} (Not Visited Yet)"
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                
                                // Create red marker for not visited places
                                try {
                                    val redMarkerDrawable = android.graphics.drawable.GradientDrawable()
                                    redMarkerDrawable.shape = android.graphics.drawable.GradientDrawable.OVAL
                                    redMarkerDrawable.setColor(android.graphics.Color.RED)
                                    redMarkerDrawable.setSize(30, 30)
                                    marker.icon = redMarkerDrawable
                                } catch (e: Exception) {
                                    // Fallback to default marker if custom creation fails
                                    marker.icon = null
                                }
                                
                                overlays.add(marker)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
