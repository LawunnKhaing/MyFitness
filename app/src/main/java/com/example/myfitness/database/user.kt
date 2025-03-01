package com.example.myfitness.database

data class User(
    val fullName: String? = null,
    val email: String? = null,
    val gender: String? = null,
    val steps: Int = 0,
    val dailyStepGoal: Int = 1000,
    val weeklySteps: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0),
    val waterIntake: Int = 0,
    val sleepData: SleepData? = null,
    val bmi: BMI? = null,

)

data class BMI(
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val value: Double = 0.0,
    val status: String? = null
)

data class SleepData(
    val lastNightSleep: String? = null,  // Stores sleep time from the previous night
    val alarmTime: String? = null,       // Stores alarm time for the next wakeup
    val alarmSoundUri: String? = null,   // Stores URI for the alarm sound
    val sleepGoal: Int = 8, // Sleep goal, default is 8 hours
    val weeklySleep: List<String> = listOf("0h 0m", "0h 0m", "0h 0m", "0h 0m", "0h 0m", "0h 0m", "0h 0m"),
)

data class Exercise(
    val name: String,
    val description: String,
    val durationInSeconds: Int,
    val gifImageUrl: String
)