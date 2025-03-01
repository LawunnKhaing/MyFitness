package com.example.myfitness.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.TimePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfitness.AlarmReceiver
import com.example.myfitness.getDaysOfWeekLabels
import com.example.myfitness.viewmodel.SleepViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

@Composable
fun SleepScreen(navController: NavHostController, viewModel: SleepViewModel = viewModel()) {
    val sleepData by viewModel.sleepData.collectAsState()
    var showSleepDialog by remember { mutableStateOf(false) }
    var sleepHours by remember { mutableStateOf("") }
    var sleepMinutes by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    var sleepDisplayText by remember { mutableStateOf("-- h -- m") }
    var showSetAlarmDialog by remember { mutableStateOf(false) }
    var alarmTime by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedAlarmSound by remember { mutableStateOf(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) }
    val context = LocalContext.current

    // Launcher for ringtone picker intent
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val ringtoneUri: Uri? = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                selectedAlarmSound = ringtoneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        }
    }

    LaunchedEffect(sleepData) {
        sleepData?.lastNightSleep?.let {
            sleepDisplayText = it
        } ?: run {
            sleepDisplayText = "-- h -- m"
        }
    }

    RequestNotificationPermission()

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
                .verticalScroll(rememberScrollState())
        ) {
            // Last Night's Sleep Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF80DEEA)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sleep Hours", fontSize = 16.sp, color = Color.Black)
                    val latestSleep = sleepData?.weeklySleep?.get((Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7) ?: "--h --m"
                    Text(text = latestSleep, fontSize = 24.sp, color = Color.Black, modifier = Modifier.padding(top = 8.dp))
                    Button(
                        onClick = { showSleepDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Enter Sleep Hours", color = Color(0xFF00796B))
                    }
                }
            }

            // Dialog for Sleep Input
            if (showSleepDialog) {
                AlertDialog(
                    onDismissRequest = { showSleepDialog = false },
                    title = { Text(text = "Enter Hours and Minutes of Sleep") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = sleepHours,
                                onValueChange = { sleepHours = it },
                                label = { Text("Hours") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = sleepMinutes,
                                onValueChange = { sleepMinutes = it },
                                label = { Text("Minutes") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val hours = sleepHours.toIntOrNull()
                                val minutes = sleepMinutes.toIntOrNull()

                                if (hours != null && minutes != null) {
                                    viewModel.updateWeeklySleep(hours, minutes)
                                }

                                showSleepDialog = false
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showSleepDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Button(
                onClick = { /* Navigate to detailed sleep schedule */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1C4E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Daily Sleep Schedule", color = Color.Black)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1BEE7)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Sleep", fontSize = 16.sp)

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        val weeklySleep = sleepData?.weeklySleep ?: listOf("0h 0m", "0h 0m", "0h 0m", "0h 0m", "0h 0m", "0h 0m", "0h 0m")
                        val sleepValues = weeklySleep.mapIndexed { index, value ->
                            val hours = value.split("h")[0].toFloatOrNull() ?: 0f
                            BarEntry(index.toFloat(), hours)
                        }

                        AndroidView(factory = {
                            val barChart = BarChart(it)
                            val dataSet = BarDataSet(sleepValues, "Sleep per day").apply {
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


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1BEE7)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today Schedule", fontSize = 16.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Alarm: ${sleepData?.alarmTime ?: "Not Set"}", fontSize = 20.sp)
                        Button(
                            onClick = { showSetAlarmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        ) {
                            Text("Set Alarm", color = Color.White)
                        }
                    }
                }
            }

            // Dialog for Setting Alarm
            if (showSetAlarmDialog) {
                AlertDialog(
                    onDismissRequest = { showSetAlarmDialog = false },
                    title = { Text(text = "Set Alarm Time") },
                    text = {
                        Column {
                            // Time Picker
                            AndroidView(
                                factory = { context ->
                                    TimePicker(context).apply {
                                        setIs24HourView(true)
                                        setOnTimeChangedListener { _, hourOfDay, minute ->
                                            alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            alarmTime.set(Calendar.MINUTE, minute)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Select Alarm Sound Button
                            Button(
                                onClick = {
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedAlarmSound)
                                    ringtonePickerLauncher.launch(intent)
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Select Alarm Sound")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                setAlarm(context, alarmTime, selectedAlarmSound)
                                viewModel.setAlarm("${alarmTime.get(Calendar.HOUR_OF_DAY)}:${alarmTime.get(Calendar.MINUTE)}", selectedAlarmSound.toString())
                                showSetAlarmDialog = false
                            }
                        ) {
                            Text("Set Alarm")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showSetAlarmDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}


@SuppressLint("ScheduleExactAlarm")
private fun setAlarm(context: Context, calendar: Calendar, alarmSound: Uri) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("ALARM_SOUND_URI", alarmSound.toString())
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                // Handle the case when the permission is not granted
            }
        }

        LaunchedEffect(Unit) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
