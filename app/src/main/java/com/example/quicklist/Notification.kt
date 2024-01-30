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
import java.util.Random


class NotificationReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        Log.d("NotificationReceiver", "onReceive: Notification received")

        var message = intent.getStringExtra("message") ?: "Default Message"
        var id = intent.getIntExtra("id", 0)

        // Instead of casting to NotifActivity, you can use a NotificationManager
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        // Ensure the notificationManager is not null before showing the notification
        notificationManager?.let {
            // Build and show the notification using the notification manager
            showNotification(context, message, id)

        }
    }

    private fun showNotification(context: Context?, message: String, notificationId: Int) {
        Log.d("NotificationReceiver", "showNotification: Notification ID: $notificationId")

        val sharedPreferences = context?.getSharedPreferences("TodoListPrefs", Context.MODE_PRIVATE)
        val serializedTodoList = sharedPreferences?.getString("todoList", "")
        val existingTasks = mutableListOf<Todo>()

        if (!serializedTodoList.isNullOrBlank()) {
            val items = serializedTodoList.split(";")
            for (item in items) {
                val parts = item.split(",")
                if (parts.size == 6) {
                    val title = parts[0]
                    val time = parts[1].toLong()
                    val detail = parts[2].toString()
                    val notid = parts[3].toInt()
                    val cat = parts[4].toString()
                    val completed = parts[5].toBoolean()
                    val todoItem = Todo(title, time,detail , notid,cat,completed)
                    existingTasks.add(todoItem)
                }
            }
        }

        // Check if the notification corresponds to an existing task
        val isExistingTask = existingTasks.any { it.notifid == notificationId }

        if (context != null && isExistingTask) {
            // Build and show your notification using NotificationCompat.Builder
            val notificationBuilder = NotificationCompat.Builder(context, "default_channel_id")
                .setSmallIcon(R.drawable.notifcheck)
                .setContentTitle("Your Current Task")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(notificationId, notificationBuilder.build())
            Log.d("NotificationReceiver", "showNotification: Notification shown")
        } else {
            Log.d(
                "NotificationReceiver",
                "showNotification: Notification not shown (Task does not exist)"
            )
        }

    }
}



