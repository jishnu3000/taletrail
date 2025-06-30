package com.example.tailtrail.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tailtrail.data.util.Utils
import com.example.tailtrail.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Home screen that users see after logging in or signing up
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val currentUser = authViewModel.currentUser

    // Setup Snackbar host state and coroutine scope
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                    onClick = { navController.navigate("profile") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Welcome Text
            Text(
                text = "Welcome to Tail Trail${currentUser?.let { ", ${it.name}" } ?: ""}!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "You have successfully logged in",
                fontSize = 18.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 64.dp)
            )

            // Sign Out Button
            Button(
                onClick = {
                    // Sign out action
                    authViewModel.signOut()
                    // Navigate back to welcome screen
                    navController.navigate("welcome") {
                        popUpTo("home") { inclusive = true }
                    }
                    // Show sign out message using Snackbar
                    Utils.showSnackbar(snackbarHostState, scope, "Signed out successfully")
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                Text(
                    text = "Sign Out",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
