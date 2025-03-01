package com.example.myfitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.myfitness.viewmodel.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfitness.getDaysOfWeekLabels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = viewModel()) {
    val userData by viewModel.userData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showBmiDialog by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var showSetGoalDialog by remember { mutableStateOf(false) }
    var stepGoal by remember { mutableStateOf("") }
    var showWaterIntakeDialog by remember { mutableStateOf(false) }
    var waterIntakeInput by remember { mutableStateOf("") }

    Scaffold(
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
                .verticalScroll(rememberScrollState()) // Enable scrolling
        ) {
            // Welcome Text
            Text(
                text = "Welcome Back, ${userData?.fullName ?: "User"}",
                fontSize = 24.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // BMI Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6B50F6)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BMI (Body Mass Index)", fontSize = 16.sp, color = Color.White)
                    Text(
                        "${userData?.bmi?.value ?: "--"} (${userData?.bmi?.status ?: "--"})",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Button(
                        onClick = { showBmiDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("View More", color = Color(0xFF6B50F6))
                    }
                }
            }

            // Dialog for BMI Input
            if (showBmiDialog) {
                AlertDialog(
                    onDismissRequest = { showBmiDialog = false },
                    title = { Text(text = "Enter Height and Weight") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = { Text("Height (cm)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val weightValue = weight.toDoubleOrNull()
                                val heightValue = height.toDoubleOrNull()

                                if (weightValue != null && heightValue != null && heightValue > 0) {
                                    val heightInMeters = heightValue / 100
                                    val bmi = weightValue / (heightInMeters * heightInMeters)
                                    val bmiStatus = when {
                                        bmi < 18.5 -> "Underweight"
                                        bmi in 18.5..24.9 -> "Normal"
                                        bmi in 25.0..29.9 -> "Overweight"
                                        else -> "Obese"
                                    }

                                    // Update BMI Data using ViewModel
                                    viewModel.updateUserBMI(weightValue, heightValue, bmi, bmiStatus)
                                }

                                showBmiDialog = false
                            }
                        ) {
                            Text("Calculate")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showBmiDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Today Target Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E1FF)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today's Goal", fontSize = 16.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Steps: ${userData?.steps ?: 0} / ${userData?.dailyStepGoal ?: "--"}", fontSize = 20.sp)
                        Button(
                            onClick = { showSetGoalDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B50F6)),
                        ) {
                            Text("Set Goal", color = Color.White)
                        }
                    }
                }
            }

            // Dialog for Setting Daily Step Goal
            if (showSetGoalDialog) {
                AlertDialog(
                    onDismissRequest = { showSetGoalDialog = false },
                    title = { Text(text = "Set Daily Step Goal") },
                    text = {
                        OutlinedTextField(
                            value = stepGoal,
                            onValueChange = {
                                stepGoal = it
                            },
                            label = { Text(text = "Enter Step Goal") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val newGoal = stepGoal.toIntOrNull()
                                if (newGoal != null) {
                                    viewModel.updateDailyStepGoal(newGoal)
                                }
                                showSetGoalDialog = false
                            }
                        ) {
                            Text("Set Goal")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showSetGoalDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Water Intake Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Water Intake", fontSize = 16.sp)
                    Text("${userData?.waterIntake ?: 0} Liters", fontSize = 20.sp)
                    Button(
                        onClick = { showWaterIntakeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B50F6)),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Add Water Intake", color = Color.White)
                    }
                }
            }

            // Dialog for Water Intake Input
            if (showWaterIntakeDialog) {
                AlertDialog(
                    onDismissRequest = { showWaterIntakeDialog = false },
                    title = { Text(text = "Enter Water Intake") },
                    text = {
                        OutlinedTextField(
                            value = waterIntakeInput,
                            onValueChange = { waterIntakeInput = it },
                            label = { Text("Water Intake (Liters)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val intake = waterIntakeInput.toIntOrNull()
                                if (intake != null) {
                                    viewModel.updateWaterIntake(intake)
                                }
                                showWaterIntakeDialog = false
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showWaterIntakeDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Weekly Steps Visualization using MPAndroidChart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEECCFF)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Steps", fontSize = 16.sp)

                    // If data is loading, show a progress indicator
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        val weeklySteps = userData?.weeklySteps ?: listOf(0, 0, 0, 0, 0, 0, 0)

                        // AndroidView for BarChart
                        AndroidView(factory = {
                            val barChart = BarChart(it)
                            val entries = weeklySteps.mapIndexed { index, value ->
                                BarEntry(index.toFloat(), value.toFloat())
                            }
                            val dataSet = BarDataSet(entries, "Steps per day").apply {
                                colors = ColorTemplate.COLORFUL_COLORS.toList()
                            }
                            val barData = BarData(dataSet)
                            barChart.data = barData

                            // Configure the chart settings
                            barChart.description.isEnabled = false
                            barChart.setFitBars(true)
                            barChart.axisRight.isEnabled = false
                            barChart.axisLeft.axisMinimum = 0f
                            barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                            barChart.xAxis.granularity = 1f
                            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(getDaysOfWeekLabels())
                            barChart.xAxis.setDrawGridLines(false)
                            barChart.axisLeft.setDrawGridLines(true)
                            barChart.invalidate() // Refresh the chart with the new data

                            barChart
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp))
                    }
                }
            }
            // Water Intake and Sleep Hours
        }
    }
}

