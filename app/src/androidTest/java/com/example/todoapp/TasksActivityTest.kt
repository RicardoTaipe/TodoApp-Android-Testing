package com.example.todoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.todoapp.data.Task
import com.example.todoapp.data.source.TasksRepository
import com.example.todoapp.tasks.TaskPriority
import com.example.todoapp.tasks.TasksActivity
import com.example.todoapp.tasks.TasksFilterType
import com.example.todoapp.util.DataBindingIdlingResource
import com.example.todoapp.util.EspressoIdlingResource
import com.example.todoapp.util.PickerTestUtil.setProgress
import com.example.todoapp.util.PickerTestUtil.withColorPriority
import com.example.todoapp.util.getToolbarNavigationContentDescription
import com.example.todoapp.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class TasksActivityTest {
    private lateinit var repository: TasksRepository

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        repository =
            ServiceLocator.provideTasksRepository(
                getApplicationContext()
            )
        runBlocking {
            repository.deleteAllTasks()
        }
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun editTask() = runTest {
        val task = Task("TITLE1", "DESCRIPTION")

        repository.saveTask(task)

        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list and verify that all the data is correct
        onView(withText("TITLE1")).perform(click())
        //Navigate to detail fragment and test
        onView(withId(R.id.task_detail_title_text)).check(matches(withText("TITLE1")))
        onView(withId(R.id.task_detail_description_text)).check(matches(withText("DESCRIPTION")))
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(not(isChecked())))

        // Click on the edit button, edit, and save
        onView(withId(R.id.edit_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("NEW TITLE"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("NEW DESCRIPTION"))
        onView(withId(R.id.add_task_priority_picker)).perform(setProgress(TaskPriority.MEDIUM.ordinal))

        onView(withId(R.id.save_task_fab)).perform(click())

        // Verify task is displayed on screen in the task list.
        onView(withText("NEW TITLE")).check(matches(isDisplayed()))
        // Verify previous task is not displayed
        onView(withText("TITLE1")).check(doesNotExist())
        //Verify priority color
        onView(withTagValue(`is`(task.id))).check(
            matches(
                withColorPriority(
                    R.array.note_color_array,
                    TaskPriority.MEDIUM.ordinal
                )
            )
        )
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun createOneTask_deleteTask() {

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Add active task
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text))
            .perform(typeText("TITLE1"), closeSoftKeyboard())
        onView(withId(R.id.add_task_description_edit_text)).perform(typeText("DESCRIPTION"))
        onView(withId(R.id.save_task_fab)).perform(click())

        // Open it in details view
        onView(withText("TITLE1")).perform(click())
        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify it was deleted
        onView(withText(TasksFilterType.ALL_TASKS.toString())).perform(click())
        onView(withText("TITLE1")).check(doesNotExist())
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun createTwoTasks_deleteOneTask() = runTest {
        repository.saveTask(Task("TITLE1", "DESCRIPTION"))
        repository.saveTask(Task("TITLE2", "DESCRIPTION"))

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Open the second task in details view
        onView(withText("TITLE2")).perform(click())
        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify only one task was deleted
        onView(withText(TasksFilterType.ALL_TASKS.toString())).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("TITLE2")).check(doesNotExist())
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun markTaskAsCompleteOnDetailScreen_taskIsCompleteInList() = runTest {
        // Add 1 active task
        val taskTitle = "COMPLETED ONE"
        repository.saveTask(Task(taskTitle, "DESCRIPTION"))

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list
        onView(withText(taskTitle)).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())

        // Click on the navigation up button to go back to the list
        onView(
            withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check that the task is marked as completed
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(taskTitle))))
            .check(matches(isChecked()))
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()

    }

    @Test
    fun markTaskAsActiveOnDetailScreen_taskIsActiveInList() = runTest {
        // Add 1 completed task
        val taskTitle = "ACTIVE ONE"
        repository.saveTask(Task(taskTitle, "DESCRIPTION", true))

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list
        onView(withText(taskTitle)).perform(click())
        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())

        // Click on the navigation up button to go back to the list
        onView(
            withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(taskTitle))))
            .check(matches(not(isChecked())))
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun markTaskAsCompleteAndActiveOnDetailScreen_taskIsActiveInList() = runTest {
        // Add 1 active task
        val taskTitle = "ACT-COMP"
        repository.saveTask(Task(taskTitle, "DESCRIPTION"))

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list
        onView(withText(taskTitle)).perform(click())
        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())
        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())

        // Click on the navigation up button to go back to the list
        onView(
            withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(taskTitle))))
            .check(matches(not(isChecked())))
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }


    @Test
    fun markTaskAsActiveAndCompleteOnDetailScreen_taskIsCompleteInList() = runTest {
        // Add 1 completed task
        val taskTitle = "COMP-ACT"
        repository.saveTask(Task(taskTitle, "DESCRIPTION", true))

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list
        onView(withText(taskTitle)).perform(click())
        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())
        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())

        // Click on the navigation up button to go back to the list
        onView(
            withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(taskTitle))))
            .check(matches(isChecked()))
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun createTask() {
        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val titleTask = "title"
        // Click on the "+" button, add details, and save
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(
            typeText(titleTask),
            closeSoftKeyboard()
        )
        onView(withId(R.id.add_task_description_edit_text)).perform(typeText("description"))
        onView(withId(R.id.add_task_priority_picker)).perform(setProgress(TaskPriority.MEDIUM.ordinal))
        // SAVE
        onView(withId(R.id.save_task_fab)).perform(click())
        // Then verify task is displayed on screen
        onView(withText(titleTask)).check(matches(isDisplayed()))
        //Verify priority color
        onView(
            allOf(
                withId(R.id.priority_color), hasSibling(
                    allOf(
                        withId(R.id.task_container),
                        hasDescendant(withText(titleTask))
                    )
                )
            )
        ).check(
            matches(
                withColorPriority(
                    R.array.note_color_array,
                    TaskPriority.MEDIUM.ordinal
                )
            )
        )
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()

    }
}