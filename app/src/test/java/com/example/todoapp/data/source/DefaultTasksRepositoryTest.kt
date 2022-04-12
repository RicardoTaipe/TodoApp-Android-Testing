package com.example.todoapp.data.source

import com.example.todoapp.MainCoroutineRule
import com.example.todoapp.data.Result.*
import com.example.todoapp.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultTasksRepositoryTest {
    private val task1 = Task("Title1", "Description1")
    private val task2 = Task("Title2", "Description2")
    private val task3 = Task("Title3", "Description3")
    private val newTask = Task("Title new", "Description new")
    private val remoteTasks = listOf(task1, task2).sortedBy { it.id }
    private val localTasks = listOf(task3).sortedBy { it.id }
    private val newTasks = listOf(task3).sortedBy { it.id }
    private lateinit var tasksRemoteDataSource: FakeDataSource
    private lateinit var tasksLocalDataSource: FakeDataSource

    // Class under test
    private lateinit var tasksRepository: DefaultTasksRepository

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun createRepository() {
        tasksRemoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeDataSource(localTasks.toMutableList())
        tasksRepository =
            DefaultTasksRepository(tasksRemoteDataSource, tasksLocalDataSource, Dispatchers.Main)
    }

    @Test
    fun getTasks_emptyRepositoryAndUninitializedCache() = runTest {
        val emptySource = FakeDataSource()
        val tasksRepository = DefaultTasksRepository(
            emptySource, emptySource, Dispatchers.Main
        )

        assertThat(tasksRepository.getTasks() is Success, `is`(true))
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() = runTest {
        // Trigger the repository to load data, which loads from remote and caches
        val initial = tasksRepository.getTasks()

        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        val second = tasksRepository.getTasks()

        // Initial and second should match because we didn't force a refresh
        assertThat(second, `is`(initial))
    }

    @Test
    fun getTasks_requestsAllTasksFromRemoteDataSource() = runTest {
        // When tasks are requested from the tasks repository
        val tasks = tasksRepository.getTasks() as Success

        // Then tasks are loaded from the remote data source
        assertThat(tasks.data, `is`(remoteTasks))
    }

    @Test
    fun saveTask_savesToCacheLocalAndRemote() = runTest {
        // Make sure newTask is not in the remote or local datasources or cache
        assertThat(tasksRemoteDataSource.tasks, not(hasItem(newTask)))
        assertThat(tasksLocalDataSource.tasks, not(hasItem(newTask)))
        assertThat((tasksRepository.getTasks() as? Success)?.data, not(hasItem(newTask)))
        // When a task is saved to the tasks repository
        tasksRepository.saveTask(newTask)

        // Then the remote and local sources are called and the cache is updated
        assertThat(tasksRemoteDataSource.tasks, hasItem(newTask))
        assertThat(tasksLocalDataSource.tasks, hasItem(newTask))

        val result = tasksRepository.getTasks() as? Success
        assertThat(result?.data, hasItem(newTask))
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runTest {
        // First call returns from REMOTE
        val tasks = tasksRepository.getTasks()

        // Set a different list of tasks in REMOTE
        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = tasksRepository.getTasks()
        assertThat(cachedTasks, `is`(tasks))

        // Now force remote loading
        val refreshedTasks = tasksRepository.getTasks(true) as Success

        // Tasks must be the recently updated in REMOTE
        assertThat(refreshedTasks.data, `is`(newTasks))
    }

    @Test
    fun getTasks_WithDirtyCache_remoteUnavailable_error() = runTest {
        // Make remote data source unavailable
        tasksRemoteDataSource.tasks = null

        // Load tasks forcing remote load
        val refreshedTasks = tasksRepository.getTasks(true)

        // Result should be an error
        assertThat(refreshedTasks, instanceOf(Error::class.java))
    }

    @Test
    fun getTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() = runTest {
        // When the remote data source is unavailable
        tasksRemoteDataSource.tasks = null

        // The repository fetches from the local source
        assertThat((tasksRepository.getTasks() as Success).data, `is`(localTasks))
    }

    @Test
    fun getTasks_WithBothDataSourcesUnavailable_returnsError() = runTest {
        // When both sources are unavailable
        tasksRemoteDataSource.tasks = null
        tasksLocalDataSource.tasks = null

        // The repository returns an error
        assertThat(tasksRepository.getTasks(), instanceOf(Error::class.java))
    }

    @Test
    fun getTasks_refreshesLocalDataSource() = runTest {
        val initialLocal = tasksLocalDataSource.tasks!!.toList()

        // First load will fetch from remote
        val newTasks = (tasksRepository.getTasks() as Success).data

        assertThat(newTasks, `is`(remoteTasks))
        assertThat(newTasks, `is`(tasksLocalDataSource.tasks))
        assertThat(tasksLocalDataSource.tasks, not(`is`(initialLocal)))
    }

    @Test
    fun saveTask_savesTaskToRemoteAndUpdatesCache() = runTest {
        // Save a task
        tasksRepository.saveTask(newTask)

        // Verify it's in all the data sources
        assertThat(tasksLocalDataSource.tasks, hasItem(newTask))
        assertThat(tasksRemoteDataSource.tasks, hasItem(newTask))

        // Verify it's in the cache
        tasksLocalDataSource.deleteAllTasks() // Make sure they don't come from local
        tasksRemoteDataSource.deleteAllTasks() // Make sure they don't come from remote
        val result = tasksRepository.getTasks() as Success
        assertThat(result.data, hasItem(newTask))
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = runTest {
        // Save a task
        tasksRepository.saveTask(newTask)

        // Make sure it's active
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isCompleted, `is`(false))

        // Mark is as complete
        tasksRepository.completeTask(newTask.id)

        // Verify it's now completed
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isCompleted, `is`(true))
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = runTest {
        // Save a task
        tasksRepository.saveTask(newTask)
        tasksRepository.completeTask(newTask.id)

        // Make sure it's completed
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isActive, `is`(false))

        // Mark is as active
        tasksRepository.activateTask(newTask.id)

        // Verify it's now activated
        val result = tasksRepository.getTask(newTask.id) as Success
        assertThat(result.data.isActive, `is`(true))
    }

    @Test
    fun getTask_repositoryCachesAfterFirstApiCall() = runTest {
        // Trigger the repository to load data, which loads from remote
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        tasksRepository.getTask(task1.id)

        // Configure the remote data source to store a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        val task1SecondTime = tasksRepository.getTask(task1.id) as Success
        val task2SecondTime = tasksRepository.getTask(task2.id) as Success

        // Both work because one is in remote and the other in cache
        assertThat(task1SecondTime.data.id, `is`(task1.id))
        assertThat(task2SecondTime.data.id, `is`(task2.id))
    }

    @Test
    fun getTask_forceRefresh() = runTest {
        // Trigger the repository to load data, which loads from remote and caches
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        tasksRepository.getTask(task1.id)

        // Configure the remote data source to return a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        // Force refresh
        val task1SecondTime = tasksRepository.getTask(task1.id, true)
        val task2SecondTime = tasksRepository.getTask(task2.id, true)

        // Only task2 works because the cache and local were invalidated
        assertThat((task1SecondTime as? Success)?.data?.id, nullValue())
        assertThat((task2SecondTime as? Success)?.data?.id, `is`(task2.id))
    }

    @Test
    fun clearCompletedTasks() = runTest {
        val completedTask = task1.copy().apply { isCompleted = true }
        tasksRemoteDataSource.tasks = mutableListOf(completedTask, task2)
        tasksRepository.clearCompletedTasks()

        val tasks = (tasksRepository.getTasks() as? Success)?.data

        assertThat(tasks?.size, `is`(1))
        assertThat(tasks, hasItem(task2))
        assertThat(tasks, not(hasItem(completedTask)))
    }

    @Test
    fun deleteAllTasks() = runTest {
        val initialTasks = (tasksRepository.getTasks() as? Success)?.data

        // Delete all tasks
        tasksRepository.deleteAllTasks()

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks() as? Success)?.data

        // Verify tasks are empty now
        assertThat(initialTasks?.isEmpty(), `is`(false))
        assertThat(afterDeleteTasks?.isEmpty(), `is`(true))
    }

    @Test
    fun deleteSingleTask() = runTest {
        val initialTasks = (tasksRepository.getTasks() as? Success)?.data

        // Delete first task
        tasksRepository.deleteTask(task1.id)

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks() as? Success)?.data

        // Verify only one task was deleted
        assertThat(afterDeleteTasks?.size, `is`(initialTasks!!.size - 1))
        assertThat(afterDeleteTasks, not(hasItem(task1)))
    }
}