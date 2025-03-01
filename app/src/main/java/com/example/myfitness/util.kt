package com.example.myfitness

import java.util.Calendar


fun getDaysOfWeekLabels(): List<String> {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val updatedLabels = daysOfWeek.toMutableList()
    if (todayIndex in updatedLabels.indices) {
        updatedLabels[todayIndex] = "Today"
    }
    return updatedLabels
}
