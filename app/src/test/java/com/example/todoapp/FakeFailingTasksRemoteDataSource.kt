package com.example.todoapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.todoapp.data.Task
import com.example.todoapp.data.Result
import com.example.todoapp.data.source.TasksDataSource

object FakeFailingTasksRemoteDataSource : TasksDataSource {
    override suspend fun getTasks(): Result<List<Task>> {
        return Result.Error(Exception("Test"))
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        return Result.Error(Exception("Test"))
    }

    override suspend fun saveTask(task: Task) {
        TODO("not implemented")
    }

    override suspend fun completeTask(task: Task) {
        TODO("not implemented")
    }

    override suspend fun completeTask(taskId: String) {
        TODO("not implemented")
    }

    override suspend fun activateTask(task: Task) {
        TODO("not implemented")
    }

    override suspend fun activateTask(taskId: String) {
        TODO("not implemented")
    }

    override suspend fun clearCompletedTasks() {
        TODO("not implemented")
    }

    override suspend fun deleteAllTasks() {
        TODO("not implemented")
    }

    override suspend fun deleteTask(taskId: String) {
        TODO("not implemented")
    }
}
