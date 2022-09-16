package com.example.todoapp.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.SeekBar
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher


object PickerTestUtil {
    fun withProgress(expectedProgress: Int): Matcher<View> {
        return object : BoundedMatcher<View, AppCompatSeekBar>(AppCompatSeekBar::class.java) {
            public override fun matchesSafely(view: AppCompatSeekBar): Boolean {
                return view.progress == expectedProgress
            }

            override fun describeTo(description: Description) {
                description.appendText("expected: ")
                description.appendText("" + expectedProgress)
            }
        }
    }

    fun withColorPriority(@ArrayRes resourceId: Int, expectedColor: Int): Matcher<View> {
        return object : BoundedMatcher<View, View>(View::class.java) {
            public override fun matchesSafely(view: View): Boolean {
                val colors = view.resources.getStringArray(resourceId)
                val backgroundColor = view.background as ColorDrawable
                return Color.parseColor(colors[expectedColor]) == backgroundColor.color
            }

            override fun describeTo(description: Description) {
                description.appendText("expected: ")
                description.appendText("" + expectedColor)
            }
        }
    }

    fun setProgress(progress: Int): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController?, view: View) {
                val seekBar = view as SeekBar
                seekBar.progress = progress
            }

            override fun getDescription(): String {
                return "Set a priority on a SeekBar"
            }

            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(SeekBar::class.java)
            }
        }
    }
}