package com.example.tailtrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.tailtrail.ui.screens.HomeScreen
import com.example.tailtrail.ui.screens.LoginScreen
import com.example.tailtrail.ui.screens.QuizAnswersScreen
import com.example.tailtrail.ui.screens.QuizScreen
import com.example.tailtrail.ui.screens.SignUpScreen
import com.example.tailtrail.ui.screens.UserProfileScreen
import com.example.tailtrail.ui.screens.WelcomeScreen
import com.example.tailtrail.ui.screens.GenreSelectionScreen
import com.example.tailtrail.ui.screens.RouteCreationScreen
import com.example.tailtrail.ui.theme.TailTrailTheme
import com.example.tailtrail.ui.viewmodel.AuthViewModel

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

    NavHost(
        navController = navController,
        startDestination = "home"
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
            HomeScreen(navController = navController, authViewModel = authViewModel)
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
        composable("genre") {
            GenreSelectionScreen(navController)
        }
        composable("route/{genre}", arguments = listOf(navArgument("genre") { type = NavType.StringType })) { backStackEntry ->
            val genre = backStackEntry.arguments?.getString("genre") ?: ""
            RouteCreationScreen(navController, genre)
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