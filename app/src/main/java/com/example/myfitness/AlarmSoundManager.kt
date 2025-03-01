package com.example.myfitness

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object AlarmSoundManager {
    private var mediaPlayer: MediaPlayer? = null

    fun startAlarm(context: Context, alarmUri: Uri) {
        stopAlarm() // Stop any previous alarm if it's still playing

        mediaPlayer = MediaPlayer.create(context, alarmUri)
        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener {
            stopAlarm()
        }
    }

    fun stopAlarm() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}
