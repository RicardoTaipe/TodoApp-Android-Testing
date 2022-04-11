package com.example.todoapp.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.domain.ActivateTaskUseCase
import com.example.todoapp.domain.CompleteTaskUseCase
import com.example.todoapp.domain.DeleteTaskUseCase
import com.example.todoapp.domain.GetTaskUseCase
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class TaskDetailViewModelFactory(
    private val tasksRepository: TasksRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            return TaskDetailViewModel(
                GetTaskUseCase(tasksRepository), DeleteTaskUseCase(tasksRepository),
                CompleteTaskUseCase(tasksRepository), ActivateTaskUseCase(tasksRepository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}