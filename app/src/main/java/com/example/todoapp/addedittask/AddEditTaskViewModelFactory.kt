package com.example.todoapp.addedittask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.domain.*
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class AddEditTaskViewModelFactory(
    private val tasksRepository: TasksRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditTaskViewModelFactory::class.java)) {
            return AddEditTaskViewModel(
                GetTaskUseCase(tasksRepository),
                SaveTaskUseCase(tasksRepository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}