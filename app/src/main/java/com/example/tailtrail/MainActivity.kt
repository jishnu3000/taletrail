package com.example.tailtrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tailtrail.ui.screens.*
import com.example.tailtrail.ui.theme.TailTrailTheme
import com.example.tailtrail.ui.viewmodel.AuthViewModel
import com.example.tailtrail.ui.viewmodel.WalkViewModel
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
    }
}


@Preview(showBackground = true)
@Composable
fun TailTrailAppPreview() {
    TailTrailTheme {
        TailTrailApp()
    }
}