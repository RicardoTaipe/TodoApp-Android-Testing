package com.example.todoapp.domain

import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.util.wrapEspressoIdlingResource

class ClearCompletedTasksUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke() {
        wrapEspressoIdlingResource {
            tasksRepository.clearCompletedTasks()
        }
    }
}