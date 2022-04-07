package com.example.todoapp.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.taskdetail.TaskDetailViewModel
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class StatisticsViewModelFactory(
    private val tasksRepository: TasksRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(tasksRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}