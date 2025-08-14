package com.example.tailtrail.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
    onNavigateToHome: () -> Unit,
    onNavigateToFullScreenVisitedMap: (List<VisitedPlace>) -> Unit = {},
    onNavigateToFullScreenNotVisitedMap: (List<NotVisitedPlace>) -> Unit = {}
) {
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        dashboardViewModel.fetchDashboardStats(userId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFBBBABA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDDA04B)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF170E29)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF170E29),
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
                        selectedIconColor = Color(0xFFDDA04B),
                        selectedTextColor = Color(0xFFDDA04B),
                        unselectedIconColor = Color(0xFFDDA04B),
                        unselectedTextColor = Color(0xFFDDA04B),
                        indicatorColor = Color(0xFFDDA04B).copy(alpha = 0.1f)
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
                        selectedIconColor = Color(0xFFDDA04B),
                        selectedTextColor = Color(0xFFDDA04B),
                        unselectedIconColor = Color(0xFFDDA04B),
                        unselectedTextColor = Color(0xFFDDA04B),
                        indicatorColor = Color(0xFFDDA04B).copy(alpha = 0.1f)
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
                        selectedIconColor = Color(0xFFDDA04B),
                        selectedTextColor = Color(0xFFDDA04B),
                        unselectedIconColor = Color(0xFFDDA04B),
                        unselectedTextColor = Color(0xFFDDA04B),
                        indicatorColor = Color(0xFFDDA04B).copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFBBBABA))
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
                        dashboardViewModel = dashboardViewModel,
                        onNavigateToFullScreenVisitedMap = onNavigateToFullScreenVisitedMap,
                        onNavigateToFullScreenNotVisitedMap = onNavigateToFullScreenNotVisitedMap
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    stats: DashboardStats,
    dashboardViewModel: DashboardViewModel,
    onNavigateToFullScreenVisitedMap: (List<VisitedPlace>) -> Unit,
    onNavigateToFullScreenNotVisitedMap: (List<NotVisitedPlace>) -> Unit
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
                notVisitedPlaces = stats.notVisitedPlaces,
                onFullScreenVisitedMap = { 
                    onNavigateToFullScreenVisitedMap(stats.visitedPlaces)
                },
                onFullScreenNotVisitedMap = { 
                    onNavigateToFullScreenNotVisitedMap(stats.notVisitedPlaces)
                }
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF170E29)),
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
                color = Color(0xFFDDA04B)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
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
                
                StatCard(
                    icon = Icons.Default.DirectionsWalk,
                    title = "Walks In Progress",
                    value = "${stats.incompleteWalks}",
                    color = Color(0xFFFF9800)
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
        modifier = Modifier.width(85.dp).height(95.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 8.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF170E29)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Places Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDDA04B)
            )
            
            // Places Progress Pie Chart
            PlacesPieChart(
                visited = stats.placesVisited,
                notVisited = stats.placesNotVisited,
                dashboardViewModel = dashboardViewModel
            )
        }
    }
}

@Composable
fun PlacesPieChart(
    visited: Int,
    notVisited: Int,
    dashboardViewModel: DashboardViewModel
) {
    val total = visited + notVisited
    val visitedAngle = if (total > 0) (visited.toFloat() / total) * 360f else 0f
    val notVisitedAngle = if (total > 0) (notVisited.toFloat() / total) * 360f else 0f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pie Chart
        Canvas(
            modifier = Modifier.size(120.dp)
        ) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Draw visited places arc
            if (visitedAngle > 0) {
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = -90f,
                    sweepAngle = visitedAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
            
            // Draw not visited places arc
            if (notVisitedAngle > 0) {
                drawArc(
                    color = Color(0xFFF44336),
                    startAngle = -90f + visitedAngle,
                    sweepAngle = notVisitedAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
            
            // Draw center circle for donut effect
            drawCircle(
                color = Color.White,
                radius = radius * 0.5f,
                center = center
            )
        }
        
        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            Color(0xFF4CAF50),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Column {
                    Text(
                        text = "Visited",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "$visited places",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            Color(0xFFF44336),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Column {
                    Text(
                        text = "Not Visited",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "$notVisited places",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Total percentage
            Text(
                text = "${dashboardViewModel.getProgressPercentage(visited, total).times(100).toInt()}% Complete",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun PlacesMapSection(
    visitedPlaces: List<VisitedPlace>,
    notVisitedPlaces: List<NotVisitedPlace>,
    onFullScreenVisitedMap: () -> Unit,
    onFullScreenNotVisitedMap: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visited Places Map
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF170E29)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                            color = Color(0xFFDDA04B)
                        )
                    }
                    
                    IconButton(
                        onClick = onFullScreenVisitedMap
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Open full screen",
                            tint = Color(0xFFDDA04B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF170E29)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                            color = Color(0xFFDDA04B)
                        )
                    }
                    
                    IconButton(
                        onClick = onFullScreenNotVisitedMap
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Open full screen",
                            tint = Color(0xFFDDA04B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
