package com.example.todoapp.domain

import com.example.todoapp.data.Task
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.util.wrapEspressoIdlingResource

class SaveTaskUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {
        wrapEspressoIdlingResource {
            return tasksRepository.saveTask(task)
        }
    }

}