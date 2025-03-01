package com.example.myfitness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myfitness.ui.screens.HomeScreen
import com.example.myfitness.ui.screens.LoginScreen
import com.example.myfitness.ui.screens.ProfileScreen
import com.example.myfitness.ui.screens.SignupScreen
import com.example.myfitness.ui.screens.SleepScreen
import com.example.myfitness.ui.screens.WorkoutScreen
import com.example.myfitness.ui.theme.MyFitnessTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val requestActivityRecognitionPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                println("Activity recognition permission granted.")
            } else {
                println("Activity recognition permission denied.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeFirebase()
        FirebaseApp.initializeApp(this)
        requestActivityRecognitionPermission()



        setContent {
            MyFitnessTheme {
                val navController = rememberNavController()
                Navigation(navController)
            }
        }
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://myfitness-19c11-default-rtdb.europe-west1.firebasedatabase.app")
    }
    private fun requestActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestActivityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }
}

@Composable
fun Navigation(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val isUserLoggedIn = remember { mutableStateOf(auth.currentUser != null) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            isUserLoggedIn.value = firebaseAuth.currentUser != null
        }
        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val startDestination = if (isUserLoggedIn.value) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("home") { HomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("sleep") { SleepScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("workout") { WorkoutScreen(navController) }
    }
}
