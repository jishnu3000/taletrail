package com.example.tailtrail.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tailtrail.data.model.Walk
import com.example.tailtrail.data.util.Utils
import com.example.tailtrail.ui.viewmodel.AuthViewModel
import com.example.tailtrail.ui.viewmodel.WalkViewModel
import kotlinx.coroutines.launch

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
        containerColor = Color(0xFFE0E0E0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
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
                    selected = true,
                    onClick = { navController.navigate("home") },
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
                    onClick = {
                        if (!isQuizRequired) navController.navigate("profile")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF673AB7),
                        selectedTextColor = Color(0xFF673AB7),
                        indicatorColor = Color(0xFF673AB7).copy(alpha = 0.1f)
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
                    // User Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
            Text(
                                text = "Welcome, ${currentUser?.name ?: "User"}!",
                                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                                color = Color(0xFF673AB7)
            )
                            
                            Spacer(modifier = Modifier.height(8.dp))

            Text(
                                text = "Your walking adventures await",
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                
                item {
                    // Previous Walks Section
                    Text(
                        text = "Previous Walks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
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
                                .padding(bottom = 8.dp),
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
                                        text = walk.genre,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF673AB7)
                                    )
                                    Text(
                                        text = "Stops: ${walk.noOfStops} • Distance: ${walk.stopDist}m",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                                
                                Icon(
                                    Icons.Default.DirectionsWalk,
                                    contentDescription = "Walk",
                                    tint = Color(0xFF9C27B0)
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
                            containerColor = Color(0xFF673AB7)
                )
            ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Walk",
                                tint = Color.White,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                Text(
                                text = "Add Walk",
                    fontSize = 18.sp,
                                color = Color.White
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
