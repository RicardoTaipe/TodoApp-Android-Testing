package com.example.todoapp.addedittask

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.TodoApplication
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.domain.*
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class AddEditTaskViewModelFactory(
    private val tasksRepository: TasksRepository,
    private val application: Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditTaskViewModel::class.java)) {
            return AddEditTaskViewModel(
                GetTaskUseCase(tasksRepository),
                SaveTaskUseCase(tasksRepository),
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}