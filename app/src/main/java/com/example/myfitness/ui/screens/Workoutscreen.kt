package com.example.myfitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myfitness.viewmodel.WorkoutViewModel
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(navController: NavHostController, viewModel: WorkoutViewModel = viewModel()) {
    val currentExercise by viewModel.currentExercise.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    var showCompleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Workout", fontSize = 20.sp)
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF6B50F6), titleContentColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Enable scrolling for the page
        ) {
            if (currentExercise == null) {
                // Initial Message to Start the Workout
                Text(
                    text = "Click Start to do the workout",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Start Workout Button
                Button(
                    onClick = { viewModel.startWorkout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B50F6)),
                ) {
                    Text(text = "Start Workout", color = Color.White)
                }
            } else {
                // Exercise Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF6B50F6)),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = currentExercise!!.name, fontSize = 20.sp, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
                        Text(text = currentExercise!!.description, fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
                        Text(text = "Time Remaining: ${remainingTime}s", fontSize = 20.sp, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))

                        // Exercise GIF
                        GlideImage(
                            imageModel = currentExercise!!.gifImageUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 16.dp)
                        )
                    }
                }

                // Complete Exercise Button
                Button(
                    onClick = {
                        viewModel.completeExercise()
                        showCompleteDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B50F6)),
                ) {
                    Text(text = "Complete Exercise", color = Color.White)
                }
            }

            // Dialog to indicate Workout Completion
            if (showCompleteDialog) {
                AlertDialog(
                    onDismissRequest = { showCompleteDialog = false },
                    title = { Text(text = "Workout Completed!") },
                    text = { Text(text = "You have successfully completed the workout.") },
                    confirmButton = {
                        Button(onClick = { showCompleteDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}
