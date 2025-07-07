package com.example.tailtrail.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun GenreSelectionScreen(navController: NavController) {
    val genres = listOf("Horror", "Comedy", "Thriller", "Adventure", "Sci-Fi", "Romance", "Mystery", "Fantasy")
    var selectedGenre by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose Genre", style = MaterialTheme.typography.headlineLarge, color = Color.White, modifier = Modifier.padding(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f)
        ) {
            items(genres) { genre ->
                Button(
                    onClick = { selectedGenre = genre },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedGenre == genre) Color.Magenta else Color(0xFFDA46D4)),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(genre, color = Color.White)
                }
            }
        }
        Button(
            onClick = { selectedGenre?.let { navController.navigate("route/$it") } },
            enabled = selectedGenre != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Submit")
        }
    }
} 