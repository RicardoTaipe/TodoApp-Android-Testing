package com.example.todoapp.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.todoapp.FakeFailingTasksRemoteDataSource
import com.example.todoapp.MainCoroutineRule
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.DefaultTasksRepository
import com.example.todoapp.data.source.FakeTestRepository
import com.example.todoapp.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StatisticsViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var statisticsViewModel: StatisticsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeTestRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupStatisticsViewModel() {
        // We initialise the repository with no tasks
        tasksRepository = FakeTestRepository()

        statisticsViewModel = StatisticsViewModel(tasksRepository)
    }

    @Test
    fun loadEmptyTasksFromRepository_EmptyResults() = runTest {
        // Given an initialized StatisticsViewModel with no tasks

        // Then the results are empty
        assertThat(statisticsViewModel.empty.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadNonEmptyTasksFromRepository_NonEmptyResults() {
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        val task4 = Task("Title4", "Description4", true)
        tasksRepository.addTasks(task1, task2, task3, task4)
        assertThat(statisticsViewModel.empty.getOrAwaitValue(), `is`(false))
        assertThat(statisticsViewModel.activeTasksPercent.getOrAwaitValue(), `is`(25f))
        assertThat(statisticsViewModel.completedTasksPercent.getOrAwaitValue(), `is`(75f))
    }

    @Test
    fun loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() = runTest {
        val errorViewModel = StatisticsViewModel(
            DefaultTasksRepository(
                FakeFailingTasksRemoteDataSource,
                FakeFailingTasksRemoteDataSource,
                Dispatchers.Main // Main is set in MainCoroutineRule
            )
        )

        // Then an error message is shown
        assertThat(errorViewModel.empty.getOrAwaitValue(), `is`(true))
        assertThat(errorViewModel.error.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadStatisticsWhenTasksAreUnavailable_callErrorToDisplay() {
        // Make the repository return errors
        tasksRepository.setReturnError(true)
        statisticsViewModel.refresh()

        // Then an error message is shown
        assertThat(statisticsViewModel.empty.getOrAwaitValue(), `is`(true))
        assertThat(statisticsViewModel.error.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadTasks_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        // Load the task in the viewmodel
        statisticsViewModel.refresh()

        // Then progress indicator is shown
        assertThat(statisticsViewModel.dataLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(statisticsViewModel.dataLoading.getOrAwaitValue(), `is`(false))
    }

}