package com.example.todoapp.util

import java.text.SimpleDateFormat

fun convertLongToDateString(systemTime: Long): String {
    return SimpleDateFormat("EEE MMM-dd-yyyy HH:mm a")
        .format(systemTime).toString()
}