package com.example.tailtrail.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tailtrail.R
import com.example.tailtrail.data.model.Walk
import com.example.tailtrail.data.util.Utils
import com.example.tailtrail.ui.viewmodel.AuthViewModel
import com.example.tailtrail.ui.viewmodel.WalkViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable

/**
 * Get genre-specific background color
 */
fun getGenreBackgroundColor(genre: String): Color {
    return when (genre.lowercase()) {
        "adventure" -> Color(0xFFc2c2c4)
        "horror" -> Color(0xFFcabfb5)
        "mystery" -> Color(0xFF968d82)
        "fantasy" -> Color(0xFF5c70ad)
        "romance" -> Color(0xFFbf7a86)
        "sci-fi" -> Color(0xFF5588a1)
        "comedy" -> Color(0xFFddb89a)
        "drama" -> Color(0xFF393c40)
        else -> Color.White
    }
}

/**
 * Get genre-specific drawable resource
 */
fun getGenreDrawable(genre: String): Int {
    return when (genre.lowercase()) {
        "adventure" -> R.drawable.adventure
        "horror" -> R.drawable.horror
        "mystery" -> R.drawable.mystery
        "fantasy" -> R.drawable.fantasy
        "romance" -> R.drawable.romance
        "sci-fi" -> R.drawable.sci_fi
        "comedy" -> R.drawable.comedy
        "drama" -> R.drawable.drama
        else -> R.drawable.adventure // fallback
    }
}

/**
 * Format distance to show km if over 1000m
 */
fun formatDistance(distanceInMeters: Int): String {
    return if (distanceInMeters >= 1000) {
        val km = distanceInMeters / 1000.0
        String.format("%.1fkm", km)
    } else {
        "${distanceInMeters}m"
    }
}

/**
 * Home screen that users see after logging in or signing up
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, authViewModel: AuthViewModel, walkViewModel: WalkViewModel) {
    val currentUser = authViewModel.currentUser
    val isQuizRequired = currentUser?.isQuiz == 0

    // Setup Snackbar host state and coroutine scope
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Load walks when screen is displayed
    LaunchedEffect(currentUser?.userId) {
        currentUser?.userId?.let { userId ->
            walkViewModel.loadUserWalks(userId)
        }
    }
    
    val walks by walkViewModel.walks.collectAsState()
    val isLoading by walkViewModel.isLoading.collectAsState()
    val error by walkViewModel.error.collectAsState()
    val addWalkSuccess by walkViewModel.addWalkSuccess.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFBBBABA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
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
                    selected = true,
                    onClick = { navController.navigate("home") },
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
                    onClick = {
                        if (!isQuizRequired) navController.navigate("profile")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFDDA04B),
                        selectedTextColor = Color(0xFFDDA04B),
                        unselectedIconColor = Color(0xFFDDA04B),
                        unselectedTextColor = Color(0xFFDDA04B),
                        indicatorColor = Color(0xFFDDA04B).copy(alpha = 0.1f)
                    ),
                    enabled = !isQuizRequired
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard") },
                    selected = false,
                    onClick = {
                        if (!isQuizRequired) navController.navigate("dashboard")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFDDA04B),
                        selectedTextColor = Color(0xFFDDA04B),
                        unselectedIconColor = Color(0xFFDDA04B),
                        unselectedTextColor = Color(0xFFDDA04B),
                        indicatorColor = Color(0xFFDDA04B).copy(alpha = 0.1f)
                    ),
                    enabled = !isQuizRequired
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Show error if any
            error?.let { errorMessage ->
                LaunchedEffect(errorMessage) {
                    Utils.showSnackbar(snackbarHostState, scope, errorMessage)
                    walkViewModel.clearError()
                }
            }
            
            // Show success message if any
            addWalkSuccess?.let { successMessage ->
                LaunchedEffect(successMessage) {
                    Utils.showSnackbar(snackbarHostState, scope, successMessage)
                    walkViewModel.clearSuccess()
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    // Walks in Progress Section in Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF170E29),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Walks in Progress",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDDA04B)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF673AB7))
                        }
                    }
                } else if (walks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
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
                                    text = "No walks yet. Create your first adventure!",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(walks) { walk ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    navController.navigate("walk_details/${walk.walkId}")
                                },
                            colors = CardDefaults.cardColors(containerColor = getGenreBackgroundColor(walk.genre)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Genre Logo
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = getGenreDrawable(walk.genre)),
                                            contentDescription = "${walk.genre} logo",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column {
                                        Text(
                                            text = walk.genre,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Stops: ${walk.noOfStops} • Distance: ${formatDistance(walk.stopDist)}",
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "View Details",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                

                
                // Add Walk Button
                item {
                    Button(
                        onClick = { navController.navigate("genre_selection") },
                modifier = Modifier
                            .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF170E29)
                )
            ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Walk",
                                tint = Color(0xFFDDA04B),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                Text(
                                text = "Add Walk",
                    fontSize = 18.sp,
                                color = Color(0xFFDDA04B)
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
