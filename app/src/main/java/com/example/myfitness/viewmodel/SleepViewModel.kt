package com.example.myfitness.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myfitness.WeeklyResetWorker
import com.example.myfitness.database.SleepData
import com.example.myfitness.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SleepViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirebaseRepository()

    private val _sleepData = MutableStateFlow<SleepData?>(null)
    val sleepData: StateFlow<SleepData?> get() = _sleepData

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    init {
        fetchSleepData()
        scheduleWeeklyReset()
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

    private fun fetchSleepData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = repository.getSleepData()
                _sleepData.value = data
            } catch (e: Exception) {
                println("Error fetching sleep data: ${e.message}")
            }
            _isLoading.value = false
        }
    }

    fun updateWeeklySleep(hours: Int, minutes: Int) {
        val userId = repository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val user = repository.getUserData() ?: return@launch
            val sleepData = user.sleepData ?: SleepData()

            val updatedWeeklySleep = sleepData.weeklySleep.toMutableList()
            val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
            val sleepString = "${hours}h ${minutes}m"

            if (todayIndex in updatedWeeklySleep.indices) {
                updatedWeeklySleep[todayIndex] = sleepString
            }

            val updatedSleepData = sleepData.copy(weeklySleep = updatedWeeklySleep)
            repository.updateSleepData(userId, updatedSleepData) { success, error ->
                if (success) {
                    println("Weekly sleep updated successfully.")
                    fetchSleepData() // Refresh the data after updating
                } else {
                    println("Failed to update weekly sleep: $error")
                }
            }
        }
    }



    fun setAlarm(time: String, alarmSoundUri: String) {
        val userId = repository.getCurrentUserId() ?: return
        repository.setAlarmTime(userId, time, alarmSoundUri) { success, error ->
            if (success) {
                println("Alarm set successfully")
                fetchSleepData() // Refresh sleep data
            } else {
                println("Failed to set alarm: $error")
            }
        }
    }
}
