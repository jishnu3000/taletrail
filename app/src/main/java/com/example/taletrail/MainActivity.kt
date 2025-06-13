package com.example.taletrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taletrail.ui.theme.TaleTrailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaleTrailTheme {
                val viewModel: MainViewModel = viewModel()
                val response by viewModel.response.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = { viewModel.fetchData() }) {
                            Text("Fetch Data")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "API Response: $response")
                    }
                }
            }
        }
    }
}

@Composable
fun ApiScreen(modifier: Modifier = Modifier, viewModel: MainViewModel = viewModel()) {
    val response by viewModel.response.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = { viewModel.fetchData() }) {
            Text("Fetch API Data")
        }
        Text(text = response)
    }
}
