package com.example.todoapp.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.todoapp.data.source.TasksDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.example.todoapp.data.Result
import com.example.todoapp.data.Result.Error
import com.example.todoapp.data.Result.Success
import com.example.todoapp.data.Task
import kotlinx.coroutines.withContext

class TasksLocalDataSource(
    private val tasksDao: TasksDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TasksDataSource {

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        return Transformations.map(tasksDao.observeTasks()) {
            Success(it)
        }
    }

    override fun observeTask(taskId: String): LiveData<Result<Task>> {
        return Transformations.map(tasksDao.observeTaskById(taskId)) {
            Success(it)
        }
    }

    override suspend fun refreshTask(taskId: String) {
        //NO-OP
    }

    override suspend fun refreshTasks() {
        //NO-OP
    }

    override suspend fun getTasks(): Result<List<Task>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(tasksDao.getTasks())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getTask(taskId: String): Result<Task> = withContext(ioDispatcher) {
        try {
            val task = tasksDao.getTaskById(taskId)
            if (task != null) {
                return@withContext Success(task)
            } else {
                return@withContext Error(Exception("Task not found!"))
            }
        } catch (e: Exception) {
            return@withContext Error(e)
        }
    }

    override suspend fun saveTask(task: Task) = withContext(ioDispatcher) {
        tasksDao.insertTask(task)
    }

    override suspend fun completeTask(task: Task) = withContext(ioDispatcher) {
        tasksDao.updateCompleted(task.id, true)
    }

    override suspend fun completeTask(taskId: String) {
        tasksDao.updateCompleted(taskId, true)
    }

    override suspend fun activateTask(task: Task) = withContext(ioDispatcher) {
        tasksDao.updateCompleted(task.id, false)
    }

    override suspend fun activateTask(taskId: String) {
        tasksDao.updateCompleted(taskId, false)
    }

    override suspend fun clearCompletedTasks() = withContext<Unit>(ioDispatcher) {
        tasksDao.deleteCompletedTasks()
    }

    override suspend fun deleteAllTasks() = withContext(ioDispatcher) {
        tasksDao.deleteTasks()
    }

    override suspend fun deleteTask(taskId: String) = withContext<Unit>(ioDispatcher) {
        tasksDao.deleteTaskById(taskId)
    }
}