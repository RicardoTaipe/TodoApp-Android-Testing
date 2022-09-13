package com.example.todoapp.tasks

import com.example.todoapp.R

enum class TaskPriority(val label: Int) {
    NONE(R.string.none),
    LOW(R.string.low),
    MEDIUM(R.string.medium),
    HIGH(R.string.high);
}