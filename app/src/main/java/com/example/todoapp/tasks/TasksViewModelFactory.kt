package com.example.todoapp.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.domain.ActivateTaskUseCase
import com.example.todoapp.domain.ClearCompletedTasksUseCase
import com.example.todoapp.domain.CompleteTaskUseCase
import com.example.todoapp.domain.GetTasksUseCase
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class TasksViewModelFactory(
    private val tasksRepository: TasksRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            return TasksViewModel(
                GetTasksUseCase(tasksRepository),
                ClearCompletedTasksUseCase(tasksRepository),
                CompleteTaskUseCase(tasksRepository),
                ActivateTaskUseCase(tasksRepository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
