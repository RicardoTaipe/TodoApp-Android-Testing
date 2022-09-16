package com.example.todoapp.statistics

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.todoapp.R
import com.example.todoapp.ServiceLocator
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.FakeAndroidTestRepository
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.util.DataBindingIdlingResource
import com.example.todoapp.util.monitorFragment
import com.example.todoapp.util.withProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class StatisticsFragmentTest {
    private lateinit var repository: TasksRepository

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun cleanupDb() {
        ServiceLocator.resetRepository()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun tasks_showsNonEmptyMessage() = runTest {
        repository.apply {
            saveTask(Task("Title1", "Description1", false))
            saveTask(Task("Title2", "Description2", true))
        }

        val scenario =
            launchFragmentInContainer<StatisticsFragment>(Bundle(), R.style.Theme_TodoApp)
        dataBindingIdlingResource.monitorFragment(scenario)

        val expectedActiveTaskText =
            getApplicationContext<Context>().getString(R.string.statistics_active_tasks, 50.0f)
        val expectedCompletedTaskText = getApplicationContext<Context>()
            .getString(R.string.statistics_completed_tasks, 50.0f)

        // check that both info boxes are displayed and contain the correct info
        onView(withId(R.id.stats_active_text)).check(matches(isDisplayed()))
        onView(withId(R.id.stats_active_text)).check(matches(withText(expectedActiveTaskText)))
        onView(withId(R.id.stats_active_indicator)).check(matches(withProgress(50)))

        onView(withId(R.id.stats_completed_text)).check(matches(isDisplayed()))
        onView(withId(R.id.stats_completed_text))
            .check(matches(withText(expectedCompletedTaskText)))
        onView(withId(R.id.stats_completed_indicator)).check(matches(withProgress(50)))
    }
}