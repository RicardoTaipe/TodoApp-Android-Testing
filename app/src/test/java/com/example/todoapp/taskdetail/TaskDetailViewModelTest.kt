package com.example.todoapp.taskdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.todoapp.*
import com.example.todoapp.data.Result.Success
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.FakeTestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {
    // Subject under test
    private lateinit var taskDetailViewModel: TaskDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeTestRepository


    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    val task = Task("Title1", "Description1")

    @Before
    fun setupViewModel() {
        tasksRepository = FakeTestRepository()
        tasksRepository.addTasks(task)

        taskDetailViewModel = TaskDetailViewModel(tasksRepository)
    }

    @Test
    fun getActiveTaskFromRepositoryAndLoadIntoView() {
        taskDetailViewModel.start(task.id)

        // Then verify that the view was notified
        assertThat(taskDetailViewModel.task.getOrAwaitValue()?.title, `is`(task.title))
        assertThat(taskDetailViewModel.task.getOrAwaitValue()?.description, `is`(task.description))
    }

    @Test
    fun completeTask() {
        // Load the ViewModel
        taskDetailViewModel.start(task.id)
        // Start observing to compute transformations
        taskDetailViewModel.task.getOrAwaitValue()

        // Verify that the task was active initially
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(false))

        // When the ViewModel is asked to complete the task
        taskDetailViewModel.setCompleted(true)

        // Then the task is completed and the snackbar shows the correct message
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(true))
        assertSnackbarMessage(taskDetailViewModel.snackbarText, R.string.task_marked_complete)
    }

    @Test
    fun activateTask() = runTest {
        task.isCompleted = true

        // Load the ViewModel
        taskDetailViewModel.start(task.id)
        // Start observing to compute transformations
        taskDetailViewModel.task.observeForTesting {}

        // Verify that the task was completed initially
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(true))

        // When the ViewModel is asked to complete the task
        taskDetailViewModel.setCompleted(false)

        // Then the task is not completed and the snackbar shows the correct message
        val newTask = (tasksRepository.getTask(task.id) as Success).data
        assertTrue(newTask.isActive)
        assertSnackbarMessage(taskDetailViewModel.snackbarText, R.string.task_marked_active)
    }

    @Test
    fun taskDetailViewModel_repositoryError() = runTest {
        // Given a repository that returns errors
        tasksRepository.setReturnError(true)

        // Given an initialized ViewModel with an active task
        taskDetailViewModel.start(task.id)
        // Get the computed LiveData value
        taskDetailViewModel.task.observeForTesting {
            // Then verify that data is not available
            assertThat(taskDetailViewModel.isDataAvailable.getOrAwaitValue(), `is`(false))
        }
    }

    @Test
    fun updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        val snackbarText = taskDetailViewModel.snackbarText.value

        // Check that the value is null
        assertThat(snackbarText, nullValue())
    }

    @Test
    fun clickOnEditTask_SetsEvent() {
        // When opening a new task
        taskDetailViewModel.editTask()

        // Then the event is triggered
        val value = taskDetailViewModel.editTaskEvent.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled(), notNullValue())
    }

    @Test
    fun deleteTask() {
        assertThat(tasksRepository.tasksServiceData.containsValue(task), `is`(true))
        taskDetailViewModel.start(task.id)

        // When the deletion of a task is requested
        taskDetailViewModel.deleteTask()

        assertThat(tasksRepository.tasksServiceData.containsValue(task), `is`(false))
    }

    @Test
    fun loadTask_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        // Load the task in the viewmodel
        taskDetailViewModel.start(task.id)
        // Start observing to compute transformations
        taskDetailViewModel.task.observeForTesting {
            // Force a refresh to show the loading indicator
            taskDetailViewModel.refresh()

            // Then progress indicator is shown
            assertThat(taskDetailViewModel.dataLoading.getOrAwaitValue(), `is`(true))

            // Execute pending coroutines actions
            advanceUntilIdle()

            // Then progress indicator is hidden
            assertThat(taskDetailViewModel.dataLoading.getOrAwaitValue(), `is`(false))
        }
    }
}