package com.example.todoapp.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.todoapp.*
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.FakeTestRepository
import com.example.todoapp.domain.ActivateTaskUseCase
import com.example.todoapp.domain.ClearCompletedTasksUseCase
import com.example.todoapp.domain.CompleteTaskUseCase
import com.example.todoapp.domain.GetTasksUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TasksViewModelTest {

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeTestRepository

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        tasksRepository = FakeTestRepository()
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        tasksRepository.addTasks(task1, task2, task3)

        tasksViewModel = TasksViewModel(
            GetTasksUseCase(tasksRepository),
            ClearCompletedTasksUseCase(tasksRepository),
            CompleteTaskUseCase(tasksRepository),
            ActivateTaskUseCase(tasksRepository)
        )
    }

    @Test
    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, so we can verify initial values
        Dispatchers.setMain(StandardTestDispatcher())

        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Trigger loading of tasks
        tasksViewModel.loadTasks(true)

        //The progress indicator is shown
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading), `is`(true))

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading), `is`(false))

        // And data correctly loaded
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items).size, `is`(3))
    }

    @Test
    fun loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)
        // Load tasks
        tasksViewModel.loadTasks(true)
        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading), `is`(false))
        // And data correctly loaded
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items).size, `is`(1))
    }

    @Test
    fun loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)

        // Load tasks
        tasksViewModel.loadTasks(true)

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading), `is`(false))

        // And data correctly loaded
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items).size, `is`(2))
    }

    @Test
    fun loadTasks_error() {
        // Make the repository return errors
        tasksRepository.setReturnError(true)

        // Load tasks
        tasksViewModel.loadTasks(true)

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading), `is`(false))

        // And the list of items is empty
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items).isEmpty(), `is`(true))

        // And the snackbar updated
        assertSnackbarMessage(tasksViewModel.snackbarText, R.string.loading_tasks_error)
    }

    @Test
    fun clickOnFab_showsAddTaskUi() {
        // When adding a new task
        tasksViewModel.addNewTask()

        // Then the event is triggered
        val value = LiveDataTestUtil.getValue(tasksViewModel.newTaskEvent)
        assertThat(value.getContentIfNotHandled(), notNullValue())
    }

    @Test
    fun clickOnOpenTask_setsEvent() {
        // When opening a new task
        val taskId = "42"
        tasksViewModel.openTask(taskId)

        // Then the event is triggered
        assertLiveDataEventTriggered(tasksViewModel.openTaskEvent, taskId)
    }

    @Test
    fun clearCompletedTasks_clearsTasks() = runTest {
        // When completed tasks are cleared
        tasksViewModel.clearCompletedTasks()

        // Fetch tasks
        tasksViewModel.loadTasks(true)

        // Fetch tasks
        val allTasks = LiveDataTestUtil.getValue(tasksViewModel.items)
        val completedTasks = allTasks.filter { it.isCompleted }

        // Verify there are no completed tasks left
        assertThat(completedTasks.isEmpty(),`is`(true))

        // Verify active task is not cleared
        assertThat(allTasks.size,`is`(1))

        // Verify snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.completed_tasks_cleared
        )
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(EDIT_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.successfully_saved_task_message
        )
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.successfully_added_task_message
        )
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(DELETE_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.successfully_deleted_task_message
        )
    }

    @Test
    fun completeTask_dataAndSnackbarUpdated() {
        // With a repository that has an active task
        val task = Task("Title", "Description")
        tasksRepository.addTasks(task)

        // Complete task
        tasksViewModel.completeTask(task, true)

        // Verify the task is completed
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted, `is`(true))

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.task_marked_complete
        )
    }

    @Test
    fun activateTask_dataAndSnackbarUpdated() {
        // With a repository that has a completed task
        val task = Task("Title", "Description", true)
        tasksRepository.addTasks(task)

        // Activate task
        tasksViewModel.completeTask(task, false)

        // Verify the task is active
        assertThat(tasksRepository.tasksServiceData[task.id]?.isActive, `is`(true))

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.task_marked_active
        )
    }

    @Test
    fun getTasksAddViewVisible() {
        // When the filter type is ALL_TASKS
        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Then the "Add task" action is visible
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.tasksAddViewVisible),`is`(true))
    }
}
