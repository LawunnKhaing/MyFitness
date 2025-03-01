package com.example.myfitness.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myfitness.WeeklyResetWorker
import com.example.myfitness.database.BMI
import com.example.myfitness.database.User
import com.example.myfitness.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HomeViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val repository = FirebaseRepository()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> get() = _userData

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("com.example.myfitness.PREFS", Context.MODE_PRIVATE)

    private var initialStepCount: Float? = null

    init {
        getUserData()
        startStepCounter()
        if (stepSensor == null) {
            println("Step sensor is not available on this device.")
            Log.e("HomeViewModel", "Step sensor is not available on this device.")
        }
        scheduleWeeklyReset()
        scheduleDailyReset()
    }

    private fun scheduleWeeklyReset() {
        val workRequest = PeriodicWorkRequestBuilder<WeeklyResetWorker>(7, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Requires internet connection
                    .build()
            )
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            "weekly_reset_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }


    private fun scheduleDailyReset() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val delay = calendar.timeInMillis - System.currentTimeMillis() + 86400000L
        viewModelScope.launch {
            kotlinx.coroutines.delay(delay)
            resetStepsForNewDay()
        }
    }

    private fun getUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = repository.getUserData()
                if (user == null) {
                    println("Failed to fetch user data: User data is null")
                } else {
                    println("Successfully fetched user data: $user")
                }
                _userData.value = user
            } catch (e: Exception) {
                println("Error fetching user data: ${e.message}")
            }
            _isLoading.value = false
        }
    }

    fun updateUserBMI(weight: Double, height: Double, bmiValue: Double, status: String) {
        val userId = repository.getCurrentUserId()
        val roundBMIValue = String.format("%.2f", bmiValue).toDouble()
        if (userId == null) {
            println("Error: User is not logged in.")
            return
        }

        val bmi = BMI(weight = weight, height = height, value = roundBMIValue, status = status)
        repository.updateUserBMI(userId, bmi) { success, error ->
            if (success) {
                println("BMI updated successfully")
                _userData.value = _userData.value?.copy(bmi = bmi)
            } else {
                println("Failed to update BMI: $error")
            }
        }
    }

    private fun startStepCounter() {
        // Load saved initial step count from shared preferences
        val savedStepCount = sharedPreferences.getFloat("INITIAL_STEP_COUNT", -1f)
        if (savedStepCount != -1f) {
            initialStepCount = savedStepCount
        }

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (initialStepCount == null) {
                // Set the initial step count only once to track steps from that point
                initialStepCount = event.values[0]
                saveInitialStepCount(initialStepCount!!)
            }

            // Calculate steps since initialization
            val stepsSinceInit = (event.values[0] - (initialStepCount ?: 0f)).toInt()

            // Update user steps in ViewModel
            updateUserSteps(stepsSinceInit)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    private fun saveInitialStepCount(stepCount: Float) {
        sharedPreferences.edit().putFloat("INITIAL_STEP_COUNT", stepCount).apply()
    }

    private fun resetStepsForNewDay() {
        val userId = repository.getCurrentUserId() ?: return
        val updatedWeeklySteps = _userData.value?.weeklySteps?.toMutableList() ?: mutableListOf(0, 0, 0, 0, 0, 0, 0)

        // Shift steps in the list to the left and add today's steps at the end
        if (updatedWeeklySteps.size == 7) {
            updatedWeeklySteps.removeAt(0)
        }
        updatedWeeklySteps.add(_userData.value?.steps ?: 0)

        // Reset steps count for the new day
        val updatedUser = _userData.value?.copy(steps = 0, weeklySteps = updatedWeeklySteps) ?: return

        repository.saveUserData(userId, updatedUser) { success, error ->
            if (success) {
                println("Steps reset for the new day and weekly steps updated.")
                _userData.value = updatedUser
                resetInitialStepCount()
            } else {
                println("Failed to reset steps: $error")
            }
        }
    }

    private fun resetInitialStepCount() {
        initialStepCount = null
        sharedPreferences.edit().remove("INITIAL_STEP_COUNT").apply()
    }

    fun updateUserSteps(steps: Int) {
        val userId = repository.getCurrentUserId() ?: return
        var todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2 // Adjust for 0-based index
        if (todayIndex < 0) todayIndex = 6 // Handle Sunday (Calendar.DAY_OF_WEEK = 1)

        repository.updateUserSteps(userId, steps, todayIndex) { success, error ->
            if (success) {
                Log.d("HomeViewModel", "Steps updated successfully for today: $steps")
                // Update local userData object to reflect the new steps
                _userData.value = _userData.value?.copy(steps = steps)
                getUserData() // Refresh user data to update UI
            } else {
                Log.e("HomeViewModel", "Failed to update steps: $error")
            }
        }
    }

    fun updateDailyStepGoal(newGoal: Int) {
        val userId = repository.getCurrentUserId() ?: return
        val updatedUser = _userData.value?.copy(dailyStepGoal = newGoal) ?: return

        repository.saveUserData(userId, updatedUser) { success, error ->
            if (success) {
                println("Daily step goal updated successfully")
                _userData.value = updatedUser
            } else {
                println("Failed to update daily step goal: $error")
            }
        }
    }

    fun updateWaterIntake(intake: Int) {
        val userId = repository.getCurrentUserId() ?: return
        val updatedUser = _userData.value?.copy(waterIntake = intake) ?: return

        repository.updateWaterIntake(userId, intake) { success, error ->
            if (success) {
                println("Water intake updated successfully")
                _userData.value = updatedUser
            } else {
                println("Failed to update water intake: $error")
            }
        }
    }


}
