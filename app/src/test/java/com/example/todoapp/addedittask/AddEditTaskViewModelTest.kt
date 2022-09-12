package com.example.todoapp.addedittask

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.todoapp.LiveDataTestUtil.getValue
import com.example.todoapp.MainCoroutineRule
import com.example.todoapp.R
import com.example.todoapp.assertSnackbarMessage
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.FakeTestRepository
import com.example.todoapp.domain.GetTaskUseCase
import com.example.todoapp.domain.SaveTaskUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddEditTaskViewModelTest {
    // Subject under test
    private lateinit var addEditTaskViewModel: AddEditTaskViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeTestRepository

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private val task = Task("Title1", "Description1")

    @Before
    fun setupViewModel() {
        // We initialise the repository with no tasks
        tasksRepository = FakeTestRepository()

        // Create class under test
        addEditTaskViewModel = AddEditTaskViewModel(
            GetTaskUseCase(tasksRepository),
            SaveTaskUseCase(tasksRepository)
        )
    }

    @Test
    fun saveNewTaskToRepository_showsSuccessMessageUi() {
        val newTitle = "New Task Title"
        val newDescription = "Some Task Description"
        (addEditTaskViewModel).apply {
            title.value = newTitle
            description.value = newDescription
        }
        addEditTaskViewModel.saveTask()

        val newTask = tasksRepository.tasksServiceData.values.first()

        // Then a task is saved in the repository and the view updated
        assertThat(newTask.title, `is`(newTitle))
        assertThat(newTask.description, `is`(newDescription))
    }

    @Test
    fun loadTasks_loading() = runTest {
        // Pause dispatcher so we can verify initial values
        Dispatchers.setMain(StandardTestDispatcher())

        // Load the task in the viewmodel
        addEditTaskViewModel.start(task.id)

        // Then progress indicator is shown
        assertThat(getValue(addEditTaskViewModel.dataLoading), `is`(true))

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(getValue(addEditTaskViewModel.dataLoading), `is`(false))
    }

    @Test
    fun loadTasks_taskShown() {
        // Add task to repository
        tasksRepository.addTasks(task)

        // Load the task with the viewmodel
        addEditTaskViewModel.start(task.id)

        // Verify a task is loaded
        assertThat(getValue(addEditTaskViewModel.title), `is`(task.title))
        assertThat(getValue(addEditTaskViewModel.description), `is`(task.description))
        assertThat(getValue(addEditTaskViewModel.dataLoading), `is`(false))
    }

    @Test
    fun saveNewTaskToRepository_emptyTitle_error() {
        saveTaskAndAssertSnackbarError("", "Some Task Description")
    }

    @Test
    fun saveNewTaskToRepository_nullTitle_error() {
        saveTaskAndAssertSnackbarError(null, "Some Task Description")
    }

    @Test
    fun saveNewTaskToRepository_emptyDescription_error() {
        saveTaskAndAssertSnackbarError("Title", "")
    }

    @Test
    fun saveNewTaskToRepository_nullDescription_error() {
        saveTaskAndAssertSnackbarError("Title", null)
    }

    @Test
    fun saveNewTaskToRepository_nullDescriptionNullTitle_error() {
        saveTaskAndAssertSnackbarError(null, null)
    }

    @Test
    fun saveNewTaskToRepository_emptyDescriptionEmptyTitle_error() {
        saveTaskAndAssertSnackbarError("", "")
    }

    private fun saveTaskAndAssertSnackbarError(title: String?, description: String?) {
        (addEditTaskViewModel).apply {
            this.title.value = title
            this.description.value = description
        }

        // When saving an incomplete task
        addEditTaskViewModel.saveTask()

        // Then the snackbar shows an error
        assertSnackbarMessage(addEditTaskViewModel.snackbarText, R.string.empty_task_message)
    }

}