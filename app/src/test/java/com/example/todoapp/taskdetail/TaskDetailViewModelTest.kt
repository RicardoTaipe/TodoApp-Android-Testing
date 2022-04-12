package com.example.todoapp.taskdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.todoapp.LiveDataTestUtil.getValue
import com.example.todoapp.MainCoroutineRule
import com.example.todoapp.R
import com.example.todoapp.assertSnackbarMessage
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.FakeTestRepository
import com.example.todoapp.domain.ActivateTaskUseCase
import com.example.todoapp.domain.CompleteTaskUseCase
import com.example.todoapp.domain.DeleteTaskUseCase
import com.example.todoapp.domain.GetTaskUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TaskDetailViewModelTest {
    // Subject under test
    private lateinit var taskDetailViewModel: TaskDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeTestRepository
    // Set the main coroutines dispatcher for unit testing.

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val task = Task("Title1", "Description1")

    @Before
    fun setupViewModel() {
        tasksRepository = FakeTestRepository()
        tasksRepository.addTasks(task)

        taskDetailViewModel = TaskDetailViewModel(
            GetTaskUseCase(tasksRepository),
            DeleteTaskUseCase(tasksRepository),
            CompleteTaskUseCase(tasksRepository),
            ActivateTaskUseCase(tasksRepository)
        )
    }

    @Test
    fun getActiveTaskFromRepositoryAndLoadIntoView() {
        taskDetailViewModel.start(task.id)

        // Then verify that the view was notified
        assertThat(getValue(taskDetailViewModel.task)?.title, `is`(task.title))
        assertThat(getValue(taskDetailViewModel.task)?.description, `is`(task.description))
    }

    @Test
    fun completeTask() {
        taskDetailViewModel.start(task.id)

        // Verify that the task was active initially
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(false))

        // When the ViewModel is asked to complete the task
        taskDetailViewModel.setCompleted(true)

        // Then the task is completed and the snackbar shows the correct message
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(true))
        assertSnackbarMessage(taskDetailViewModel.snackbarText, R.string.task_marked_complete)
    }

    @Test
    fun activateTask() {
        task.isCompleted = true

        taskDetailViewModel.start(task.id)

        // Verify that the task was completed initially
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(true))

        // When the ViewModel is asked to complete the task
        taskDetailViewModel.setCompleted(false)

        // Then the task is not completed and the snackbar shows the correct message
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(false))
        assertSnackbarMessage(taskDetailViewModel.snackbarText, R.string.task_marked_active)

    }

    @Test
    fun taskDetailViewModel_repositoryError() {
        // Given a repository that returns errors
        tasksRepository.setReturnError(true)

        // Given an initialized ViewModel with an active task
        taskDetailViewModel.start(task.id)

        // Then verify that data is not available
        assertThat(getValue(taskDetailViewModel.isDataAvailable), `is`(false))
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
        val value = getValue(taskDetailViewModel.editTaskEvent)
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
        // Pause dispatcher so we can verify initial values
        Dispatchers.setMain(StandardTestDispatcher())

        // Load the task in the viewmodel
        taskDetailViewModel.start(task.id)

        // Then progress indicator is shown
        assertThat(getValue(taskDetailViewModel.dataLoading),`is`(true))

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(getValue(taskDetailViewModel.dataLoading),`is`(false))
    }
}