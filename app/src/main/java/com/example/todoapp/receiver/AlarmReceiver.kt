package com.example.todoapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.todoapp.util.sendNotification

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        with(NotificationManagerCompat.from(context)) {
            sendNotification("It's time for your event", context)
        }
    }
}