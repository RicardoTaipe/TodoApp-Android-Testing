package com.example.todoapp.tasks

enum class TasksFilterType {
    ALL_TASKS,
    ACTIVE_TASKS,
    COMPLETED_TASKS;

    override fun toString(): String {
        return when (this) {
            ALL_TASKS -> "All"
            ACTIVE_TASKS -> "Active"
            COMPLETED_TASKS -> "Completed"
        }
    }
}