package com.example.myfitness.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import com.example.myfitness.database.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentExercise = MutableStateFlow<Exercise?>(null)
    val currentExercise: StateFlow<Exercise?> get() = _currentExercise

    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> get() = _remainingTime

    private val exercises = listOf(
        Exercise("Push-ups", "Place your hands shoulder-width apart on the floor. Lower your body until your chest nearly touches the floor. Push your body back up until your arms are fully extended.", 30, "https://media.tenor.com/gI-8qCUEko8AAAAC/pushup.gif"),
        Exercise("Squats", "Stand with your feet shoulder-width apart. Lower your body as far as you can by pushing your hips back and bending your knees. Return to the starting position.", 45, "https://athletesacceleration.com/wp-content/uploads/2014/08/bodyweight-squat.gif"),
        Exercise("Plank", "Start in a push-up position, then bend your elbows and rest your weight on your forearms. Hold this position for as long as you can.", 60, "https://media.tenor.com/6SOetkNbfakAAAAM/plank-abs.gif")
    )

    private var exerciseIndex = 0
    private lateinit var timer: CountDownTimer

    fun startWorkout() {
        exerciseIndex = 0
        startNextExercise()
    }

    private fun startNextExercise() {
        if (exerciseIndex < exercises.size) {
            _currentExercise.value = exercises[exerciseIndex]
            _remainingTime.value = exercises[exerciseIndex].durationInSeconds
            startTimer(exercises[exerciseIndex].durationInSeconds)
            exerciseIndex++
        } else {
            _currentExercise.value = null // Workout Complete
        }
    }

    private fun startTimer(seconds: Int) {
        timer = object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                startNextExercise()
            }
        }.start()
    }

    fun completeExercise() {
        timer.cancel()
        startNextExercise()
    }
}
