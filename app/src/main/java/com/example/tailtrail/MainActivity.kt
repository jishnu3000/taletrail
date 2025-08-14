package com.example.tailtrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.example.tailtrail.data.model.Place
import com.example.tailtrail.data.model.visitedPlacesToPlaces
import com.example.tailtrail.data.model.notVisitedPlacesToPlaces
import com.example.tailtrail.ui.screens.*
import com.example.tailtrail.ui.theme.TailTrailTheme
import com.example.tailtrail.ui.viewmodel.AuthViewModel
import com.example.tailtrail.ui.viewmodel.WalkViewModel
import com.example.tailtrail.viewmodel.DashboardViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.tailtrail.data.api.RetrofitClient
import com.example.tailtrail.data.repository.WalkRepository
import com.example.tailtrail.ui.screens.WalkDetailsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TailTrailTheme {
                TailTrailApp()
            }
        }
    }
}

@Composable
fun TailTrailApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(context))
    
    // Initialize WalkViewModel
    val walkRepository = WalkRepository(RetrofitClient.walkApi)
    val walkViewModel: WalkViewModel = viewModel(factory = WalkViewModel.provideFactory(walkRepository))
    
    // Initialize DashboardViewModel
    val dashboardViewModel: DashboardViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(walkRepository) as T
        }
    })

    // Check if user needs to take quiz and navigate accordingly
    LaunchedEffect(authViewModel.currentUser) {
        authViewModel.currentUser?.let { user ->
            if (user.isQuiz == 0) {
                navController.navigate("quiz") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.currentUser != null) "home" else "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("signup") {
            SignUpScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("home") {
            HomeScreen(navController = navController, authViewModel = authViewModel, walkViewModel = walkViewModel)
        }
        composable("profile") {
            UserProfileScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("quiz") {
            QuizScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("quiz_answers") {
            QuizAnswersScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("genre_selection") {
            GenreSelectionScreen(navController = navController)
        }
        composable("route_planning/{genre}") { backStackEntry ->
            val genre = backStackEntry.arguments?.getString("genre") ?: "Adventure"
            RoutePlanningScreen(
                navController = navController,
                genre = genre,
                walkViewModel = walkViewModel,
                userId = authViewModel.currentUser?.userId ?: 0
            )
        }
        composable("walk_details/{walkId}") { backStackEntry ->
            val walkId = backStackEntry.arguments?.getString("walkId")?.toIntOrNull() ?: 0
            WalkDetailsScreen(
                navController = navController,
                walkViewModel = walkViewModel,
                walkId = walkId,
                userId = authViewModel.currentUser?.userId ?: 0
            )
        }
        composable("dashboard") {
            DashboardScreen(
                dashboardViewModel = dashboardViewModel,
                userId = authViewModel.currentUser?.userId ?: 0,
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToFullScreenVisitedMap = { visitedPlaces ->
                    // Store the places data in the ViewModel for the full-screen map
                    dashboardViewModel.setTempVisitedPlaces(visitedPlacesToPlaces(visitedPlaces))
                    navController.navigate("full_screen_visited_map")
                },
                onNavigateToFullScreenNotVisitedMap = { notVisitedPlaces ->
                    // Store the places data in the ViewModel for the full-screen map
                    dashboardViewModel.setTempNotVisitedPlaces(notVisitedPlacesToPlaces(notVisitedPlaces))
                    navController.navigate("full_screen_not_visited_map")
                }
            )
        }
        
        // Full-screen visited places map
        composable("full_screen_visited_map") {
            val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
            FullScreenMapScreen(
                title = "Visited Places",
                visitedPlaces = dashboardViewModel.tempVisitedPlaces ?: emptyList(),
                notVisitedPlaces = emptyList(),
                onBack = { navController.popBackStack() }
            )
        }
        
        // Full-screen not visited places map
        composable("full_screen_not_visited_map") {
            val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
            FullScreenMapScreen(
                title = "Places to Visit",
                visitedPlaces = emptyList(),
                notVisitedPlaces = dashboardViewModel.tempNotVisitedPlaces ?: emptyList(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenMapScreen(
    title: String,
    visitedPlaces: List<Place>,
    notVisitedPlaces: List<Place>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDDA04B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFDDA04B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF170E29)
                )
            )
        },
        containerColor = Color(0xFFBBBABA)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    org.osmdroid.config.Configuration.getInstance().load(
                        context,
                        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                    )
                    
                    MapView(context).apply {
                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(13.0)
                        controller.setCenter(org.osmdroid.util.GeoPoint(12.9716, 77.5946)) // Bangalore coordinates
                        
                        // Add markers for visited places (green)
                        visitedPlaces.forEach { place ->
                            val marker = org.osmdroid.views.overlay.Marker(this)
                            marker.position = org.osmdroid.util.GeoPoint(place.latitude, place.longitude)
                            marker.title = place.name
                            marker.snippet = if (place.storySegment != null) place.storySegment else "Visited"
                            
                            // Set custom marker icon for visited places (green)
                            try {
                                val drawable = androidx.core.content.ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_map)
                                drawable?.let { d ->
                                    d.setTint(android.graphics.Color.GREEN)
                                    marker.icon = d
                                }
                            } catch (e: Exception) {
                                // Fallback to default marker
                            }
                            
                            overlays.add(marker)
                        }
                        
                        // Add markers for not visited places (red)
                        notVisitedPlaces.forEach { place ->
                            val marker = org.osmdroid.views.overlay.Marker(this)
                            marker.position = org.osmdroid.util.GeoPoint(place.latitude, place.longitude)
                            marker.title = place.name
                            marker.snippet = "Not Visited"
                            
                            // Set custom marker icon for not visited places (red)
                            try {
                                val drawable = androidx.core.content.ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_map)
                                drawable?.let { d ->
                                    d.setTint(android.graphics.Color.RED)
                                    marker.icon = d
                                }
                            } catch (e: Exception) {
                                // Fallback to default marker
                            }
                            
                            overlays.add(marker)
                        }
                        
                        invalidate()
                    }
                },
                update = { mapView ->
                    // Update the map if needed
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TailTrailAppPreview() {
    TailTrailTheme {
        TailTrailApp()
    }
}