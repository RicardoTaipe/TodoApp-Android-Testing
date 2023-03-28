package com.example.todoapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.todoapp.addedittask.AddEditTaskViewModel
import com.example.todoapp.util.sendNotification

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        with(NotificationManagerCompat.from(context)) {
            val text = intent.extras?.getString(AddEditTaskViewModel.TASK_NAME)
                ?: "It's time for your event"
            sendNotification(text, context)
        }
    }
}