package com.example.myfitness

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmSoundUriString = intent.getStringExtra("ALARM_SOUND_URI")
        if (alarmSoundUriString != null) {
            try {
                val alarmUri = Uri.parse(alarmSoundUriString)
                AlarmSoundManager.startAlarm(context, alarmUri)

                // Show notification to stop the alarm
                showNotification(context)
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error playing alarm sound: ${e.message}")
            }
        } else {
            Log.e("AlarmReceiver", "Alarm sound URI is null")
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ALARM_CHANNEL_ID",
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(context, StopAlarmReceiver::class.java)
        val pendingStopIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "ALARM_CHANNEL_ID")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm Ringing")
            .setContentText("Your alarm is ringing!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_delete, "Stop", pendingStopIntent)
            .build()

        notificationManager.notify(1, notification)
    }
}
