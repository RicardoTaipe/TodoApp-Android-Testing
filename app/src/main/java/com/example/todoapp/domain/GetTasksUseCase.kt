package com.example.todoapp.domain

import com.example.todoapp.data.Result
import com.example.todoapp.data.Result.Success
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.tasks.TasksFilterType
import com.example.todoapp.tasks.TasksFilterType.*
import com.example.todoapp.util.wrapEspressoIdlingResource

class GetTasksUseCase(private val tasksRepository: TasksRepository) {
    suspend operator fun invoke(
        forceUpdate: Boolean = false,
        currentFiltering: TasksFilterType = ALL_TASKS
    ): Result<List<Task>> {
        wrapEspressoIdlingResource {
            val taskResult = tasksRepository.getTasks(forceUpdate)
            if (taskResult is Success && currentFiltering != ALL_TASKS) {
                val tasks = taskResult.data
                when (currentFiltering) {
                    ACTIVE_TASKS -> return Success(tasks.filter { it.isActive })
                    COMPLETED_TASKS -> return Success(tasks.filter { it.isCompleted })
                    else -> NotImplementedError()
                }
            }
            return taskResult
        }
    }
}