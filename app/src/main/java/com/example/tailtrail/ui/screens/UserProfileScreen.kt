package com.example.tailtrail.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tailtrail.ui.viewmodel.AuthViewModel
import com.example.tailtrail.data.util.Utils
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource
import android.net.Uri
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userDetails = authViewModel.userDetails
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    // Photo upload states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Function to create camera image file
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.getExternalFilesDir(null), "profile_photos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "PROFILE_${timeStamp}.jpg")
    }
    
    // Function to save profile photo URI with persistence
    fun saveProfilePhotoUri(uri: Uri) {
        try {
            println("ProfileScreen: Saving URI: $uri (scheme: ${uri.scheme})")
            
            // Take persistent URI permission only for gallery images (content:// scheme)
            if (uri.scheme == "content") {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    println("ProfileScreen: Successfully took persistent permission for content URI")
                } catch (e: SecurityException) {
                    println("ProfileScreen: Could not take persistent permission (this is normal for some content providers): ${e.message}")
                    // This is okay - some content providers don't support persistent permissions
                }
            } else {
                println("ProfileScreen: File URI detected, no persistent permission needed")
            }
            
            val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("profile_image_uri", uri.toString()).apply()
            selectedImageUri = uri
            println("ProfileScreen: Successfully saved and set URI: $uri")
            scope.launch {
                snackbarHostState.showSnackbar("Profile photo updated successfully!")
            }
        } catch (e: Exception) {
            println("ProfileScreen: Error saving photo: ${e.message}")
            scope.launch {
                snackbarHostState.showSnackbar("Error saving photo: ${e.message}")
            }
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        println("ProfileScreen: Camera result - success: $success, cameraImageUri: $cameraImageUri")
        if (success && cameraImageUri != null) {
            println("ProfileScreen: Camera capture successful, saving URI: ${cameraImageUri}")
            saveProfilePhotoUri(cameraImageUri!!)
        } else {
            println("ProfileScreen: Camera capture failed or cancelled")
            scope.launch {
                snackbarHostState.showSnackbar("Camera capture ${if (success) "failed - no image URI" else "cancelled"}")
            }
        }
    }
    
    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with camera
            try {
                println("ProfileScreen: Camera permission granted, creating camera image file...")
                val imageFile = createImageFile()
                println("ProfileScreen: Created file: ${imageFile.absolutePath}")
                
                cameraImageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                println("ProfileScreen: Created FileProvider URI: $cameraImageUri")
                
                cameraLauncher.launch(cameraImageUri!!)
                println("ProfileScreen: Launched camera with URI: $cameraImageUri")
            } catch (e: Exception) {
                println("ProfileScreen: Error accessing camera after permission granted: ${e.message}")
                e.printStackTrace()
                scope.launch {
                    snackbarHostState.showSnackbar("Error accessing camera: ${e.message}")
                }
            }
        } else {
            // Permission denied
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to take photos")
            }
        }
    }
    
    // Load saved profile photo on app start
    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val savedImagePath = sharedPrefs.getString("profile_image_uri", null)
        println("ProfileScreen: Loading saved image path: $savedImagePath")
        if (savedImagePath != null) {
            try {
                val uri = Uri.parse(savedImagePath)
                println("ProfileScreen: Parsed URI: $uri (scheme: ${uri.scheme})")
                
                // Verify the URI is still accessible
                if (uri.scheme == "file") {
                    // For file URIs, check if file exists
                    val file = File(uri.path ?: "")
                    if (file.exists()) {
                        println("ProfileScreen: File URI verified - file exists")
                        selectedImageUri = uri
                    } else {
                        println("ProfileScreen: File URI invalid - file does not exist")
                        sharedPrefs.edit().remove("profile_image_uri").apply()
                    }
                } else {
                    // For content URIs, try to open input stream
                    context.contentResolver.openInputStream(uri)?.use { 
                        println("ProfileScreen: Content URI verified - stream opened successfully")
                        selectedImageUri = uri
                    }
                }
                println("ProfileScreen: Final selectedImageUri: $selectedImageUri")
            } catch (e: Exception) {
                println("ProfileScreen: Failed to load image URI: ${e.message}")
                // If URI is no longer valid, remove it from preferences
                sharedPrefs.edit().remove("profile_image_uri").apply()
            }
        }
    }
    
    // Function to check camera permission and launch camera
    fun launchCameraWithPermission() {
        val cameraPermission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, proceed with camera
                try {
                    println("ProfileScreen: Camera permission already granted, creating camera image file...")
                    val imageFile = createImageFile()
                    println("ProfileScreen: Created file: ${imageFile.absolutePath}")
                    
                    cameraImageUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        imageFile
                    )
                    println("ProfileScreen: Created FileProvider URI: $cameraImageUri")
                    
                    cameraLauncher.launch(cameraImageUri!!)
                    println("ProfileScreen: Launched camera with URI: $cameraImageUri")
                } catch (e: Exception) {
                    println("ProfileScreen: Error accessing camera: ${e.message}")
                    e.printStackTrace()
                    scope.launch {
                        snackbarHostState.showSnackbar("Error accessing camera: ${e.message}")
                    }
                }
            }
            else -> {
                // Request permission
                println("ProfileScreen: Requesting camera permission...")
                cameraPermissionLauncher.launch(cameraPermission)
            }
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { saveProfilePhotoUri(it) }
    }
    
    LaunchedEffect(Unit) {
        authViewModel.fetchUserDetails()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
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
                    selected = true,
                    onClick = { navController.navigate("profile") },
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
                    selected = false,
                    onClick = { navController.navigate("dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFDDA04B),
                        selectedTextColor = Color(0xFFDDA04B),
                        unselectedIconColor = Color(0xFFDDA04B),
                        unselectedTextColor = Color(0xFFDDA04B),
                        indicatorColor = Color(0xFFDDA04B).copy(alpha = 0.1f)
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFBBBABA))
        ) {
            if (userDetails == null) {
                // Loading state - center the progress indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                // Content in LazyColumn for scrollability
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Profile Avatar
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(140.dp), // Increased to accommodate camera icon
                                contentAlignment = Alignment.Center
                            ) {
                                // Main profile image circle
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { showImagePickerDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImageUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = selectedImageUri,
                                                onError = { 
                                                    // If image fails to load, clear the saved URI
                                                    scope.launch {
                                                        val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                                                        sharedPrefs.edit().remove("profile_image_uri").apply()
                                                        selectedImageUri = null
                                                        snackbarHostState.showSnackbar("Failed to load profile image")
                                                    }
                                                }
                                            ),
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Default profile icon
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .background(
                                                    Color(0xFF170E29),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier.size(60.dp),
                                                tint = Color(0xFFDDA04B)
                                            )
                                        }
                                    }
                                }
                                
                                // Camera overlay icon - positioned outside the main circle
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-8).dp, y = (-8).dp) // Offset to position nicely
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF170E29))
                                        .clickable { showImagePickerDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Change Photo",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFFDDA04B)
                                    )
                                }
                            }
                            
                            // Remove photo button (only show when photo is selected)
                            if (selectedImageUri != null) {
                                TextButton(
                                    onClick = {
                                        val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                                        sharedPrefs.edit().remove("profile_image_uri").apply()
                                        selectedImageUri = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Profile photo removed")
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Remove Photo",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        // Name Card
                        ProfileInfoCard(
                            icon = Icons.Default.Person,
                            label = "Name",
                            value = userDetails.name
                        )
                    }

                    item {
                        // Phone Number Card
                        ProfileInfoCard(
                            icon = Icons.Default.Phone,
                            label = "Phone Number",
                            value = userDetails.phoneNumber
                        )
                    }

                    item {
                        // Pincode Card
                        ProfileInfoCard(
                            icon = Icons.Default.Home,
                            label = "Pincode",
                            value = userDetails.pincode
                        )
                    }

                    item {
                        // Email Card
                        ProfileInfoCard(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = userDetails.email
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        // View Quiz Answers Button
                        Button(
                            onClick = { navController.navigate("quiz_answers") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF170E29),
                                contentColor = Color(0xFFDDA04B)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Quiz,
                                contentDescription = "Quiz Answers",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Quiz Answers",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    item {
                        // Logout Button
                        Button(
                            onClick = {
                                authViewModel.signOut()
                                navController.navigate("welcome") {
                                    popUpTo("home") { inclusive = true }
                                }
                                Utils.showSnackbar(
                                    snackbarHostState,
                                    scope,
                                    "Signed out successfully"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    item {
                        // Bottom spacing for better scrolling experience
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        
        // Image Picker Dialog
        if (showImagePickerDialog) {
            AlertDialog(
                onDismissRequest = { showImagePickerDialog = false },
                containerColor = Color(0xFF170E29),
                title = {
                    Text(
                        text = "Change Profile Photo",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDDA04B)
                    )
                },
                text = {
                    Text(
                        text = "Choose how you'd like to add your photo:",
                        color = Color(0xFFDDA04B)
                    )
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                                showImagePickerDialog = false
                            },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFDDA04B)
                            )
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery")
                        }
                        
                        TextButton(
                            onClick = {
                                launchCameraWithPermission()
                                showImagePickerDialog = false
                            },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFDDA04B)
                            )
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showImagePickerDialog = false },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFDDA04B)
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF170E29)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDA04B).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = Color(0xFFDDA04B),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color(0xFFDDA04B).copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = Color(0xFFDDA04B),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
