package com.example.myfitness.repository

import android.util.Log
import com.example.myfitness.database.BMI
import com.example.myfitness.database.SleepData
import com.example.myfitness.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://myfitness-19c11-default-rtdb.europe-west1.firebasedatabase.app/").reference

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getUserData(): User? {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e("FirebaseRepository", "User ID is null. User might not be logged in.")
            return null
        }

        return try {
            val snapshot = database.child("users").child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
            if (user == null) {
                Log.e("FirebaseRepository", "No user data found for userId: $userId")
            } else {
                Log.d("FirebaseRepository", "User data successfully fetched for userId: $userId")
            }
            user
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Failed to fetch user data: \${e.message}")
            null
        }
    }

    fun saveUserData(userId: String, user: User, onComplete: (Boolean, String?) -> Unit) {
        database.child("users").child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseRepository", "User data saved successfully for userId: $userId")
                    onComplete(true, null)
                } else {
                    Log.e("FirebaseRepository", "Failed to save user data for userId: $userId: \${task.exception?.message}")
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun updateUserBMI(userId: String, bmi: BMI, onComplete: (Boolean, String?) -> Unit) {
        database.child("users").child(userId).child("bmi").setValue(bmi)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseRepository", "BMI data updated successfully for userId: $userId")
                    onComplete(true, null)
                } else {
                    Log.e("FirebaseRepository", "Failed to update BMI data for userId: $userId: \${task.exception?.message}")
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun updateUserSteps(userId: String, steps: Int, dayIndex: Int, onComplete: (Boolean, String?) -> Unit) {
        val userRef = database.child("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val currentUser = snapshot.getValue(User::class.java)
            if (currentUser != null) {
                val updatedWeeklySteps = currentUser.weeklySteps.toMutableList()
                if (dayIndex in updatedWeeklySteps.indices) {
                    updatedWeeklySteps[dayIndex] = steps
                }

                val updatedUser = currentUser.copy(steps = steps, weeklySteps = updatedWeeklySteps)
                userRef.setValue(updatedUser).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FirebaseRepository", "Steps data updated successfully for userId: $userId")
                        onComplete(true, null)
                    } else {
                        Log.e("FirebaseRepository", "Failed to update steps data for userId: $userId: ${task.exception?.message}")
                        onComplete(false, task.exception?.message)
                    }
                }
            } else {
                Log.e("FirebaseRepository", "Failed to update steps data: User data is null")
                onComplete(false, "User data is null")
            }
        }
    }

    suspend fun getSleepData(): SleepData? {
        val userId = getCurrentUserId() ?: return null
        val snapshot = database.child("users").child(userId).child("sleepData").get().await()
        return snapshot.getValue(SleepData::class.java)
    }


    fun setAlarmTime(userId: String, time: String, alarmSoundUri: String, onComplete: (Boolean, String?) -> Unit) {
        val userRef = database.child("users").child(userId).child("sleepData")

        userRef.get().addOnSuccessListener { snapshot ->
            val currentSleepData = snapshot.getValue(SleepData::class.java) ?: SleepData()

            val updatedSleepData = currentSleepData.copy(
                alarmTime = time,
                alarmSoundUri = alarmSoundUri
            )

            userRef.setValue(updatedSleepData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseRepository", "Alarm data updated successfully for userId: $userId")
                    onComplete(true, null)
                } else {
                    Log.e("FirebaseRepository", "Failed to update alarm data for userId: $userId: ${task.exception?.message}")
                    onComplete(false, task.exception?.message)
                }
            }
        }.addOnFailureListener {
            Log.e("FirebaseRepository", "Failed to fetch current sleep data: ${it.message}")
            onComplete(false, it.message)
        }
    }




    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        val userId = getCurrentUserId()
        if (userId != null) {
            // Delete user data from database
            database.child("users").child(userId).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Delete user authentication account
                    auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Log.d("FirebaseRepository", "Account deleted successfully for userId: $userId")
                            onComplete(true)
                        } else {
                            Log.e("FirebaseRepository", "Failed to delete account: ${deleteTask.exception?.message}")
                            onComplete(false)
                        }
                    }
                } else {
                    Log.e("FirebaseRepository", "Failed to delete user data for userId: $userId: ${task.exception?.message}")
                    onComplete(false)
                }
            }
        } else {
            onComplete(false)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun updateSleepData(userId: String, sleepData: SleepData, onComplete: (Boolean, String?) -> Unit) {
        database.child("users").child(userId).child("sleepData").setValue(sleepData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseRepository", "Sleep data updated successfully for userId: $userId")
                    onComplete(true, null)
                } else {
                    Log.e("FirebaseRepository", "Failed to update sleep data for userId: $userId: ${task.exception?.message}")
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun updateWaterIntake(userId: String, waterIntake: Int, onComplete: (Boolean, String?) -> Unit) {
        database.child("users").child(userId).child("waterIntake").setValue(waterIntake)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseRepository", "Water intake updated successfully for userId: $userId")
                    onComplete(true, null)
                } else {
                    Log.e("FirebaseRepository", "Failed to update water intake for userId: $userId: ${task.exception?.message}")
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun resetWeeklySteps(userId: String) {
        val weeklySteps = List(7) { 0 }  // Reset steps to 0 for each day of the week
        database.child("users").child(userId).child("weeklySteps").setValue(weeklySteps)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseRepository", "Weekly steps reset successfully for userId: $userId")
                } else {
                    Log.e("FirebaseRepository", "Failed to reset weekly steps for userId: $userId: ${task.exception?.message}")
                }
            }
    }

    fun resetWeeklySleep(userId: String) {
        val weeklySleep = List(7) { "0h 0m" }  // Reset sleep hours to 0 for each day of the week
        database.child("users").child(userId).child("sleepData").child("weeklySleep").setValue(weeklySleep)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseRepository", "Weekly sleep data reset successfully for userId: $userId")
                } else {
                    Log.e("FirebaseRepository", "Failed to reset weekly sleep data for userId: $userId: ${task.exception?.message}")
                }
            }
    }


}
