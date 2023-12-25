package com.example.quicklist

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("message") ?: "Default Message"


        // Instead of casting to NotifActivity, you can use a NotificationManager
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        // Ensure the notificationManager is not null before showing the notification
        notificationManager?.let {
            // Build and show the notification using the notification manager
            showNotification(context, message)
        }
    }

    private fun showNotification(context: Context?, message: String) {
        // Build and show your notification using NotificationCompat.Builder
        val notificationBuilder = NotificationCompat.Builder(context!!, "default_channel_id")
            .setSmallIcon(R.drawable.notifcheck)
            .setContentTitle("Your Current Task")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(0, notificationBuilder.build())
    }
}


