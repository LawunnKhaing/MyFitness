package com.example.myfitness

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myfitness.repository.FirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeeklyResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = FirebaseRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get the current user ID
            val userId = repository.getCurrentUserId()
            if (userId != null) {
                // Reset steps and sleep data for the user
                repository.resetWeeklySteps(userId)
                repository.resetWeeklySleep(userId)
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}