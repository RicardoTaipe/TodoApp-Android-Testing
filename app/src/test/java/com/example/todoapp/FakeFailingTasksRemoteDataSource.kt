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

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        return liveData { emit(getTasks()) }
    }

    override suspend fun refreshTasks() {

    }

    override fun observeTask(taskId: String): LiveData<Result<Task>> {
        return liveData { emit(getTask(taskId)) }
    }

    override suspend fun refreshTask(taskId: String) {

    }

    override suspend fun saveTask(task: Task) {

    }

    override suspend fun completeTask(task: Task) {

    }

    override suspend fun completeTask(taskId: String) {

    }

    override suspend fun activateTask(task: Task) {

    }

    override suspend fun activateTask(taskId: String) {

    }

    override suspend fun clearCompletedTasks() {

    }

    override suspend fun deleteAllTasks() {

    }

    override suspend fun deleteTask(taskId: String) {

    }
}