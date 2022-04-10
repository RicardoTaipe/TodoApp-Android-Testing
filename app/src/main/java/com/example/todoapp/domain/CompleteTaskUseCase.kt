package com.example.todoapp.domain

import com.example.todoapp.data.Task
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.util.wrapEspressoIdlingResource

class CompleteTaskUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {
        wrapEspressoIdlingResource {
            tasksRepository.completeTask(task)
        }
    }
}