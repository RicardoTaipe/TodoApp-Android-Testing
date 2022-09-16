package com.example.todoapp.util

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.BoundedMatcher
import com.example.todoapp.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.hamcrest.Description
import org.hamcrest.Matcher

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}

fun withProgress(expectedProgress: Int): Matcher<View> {
    return object : BoundedMatcher<View, LinearProgressIndicator>(LinearProgressIndicator::class.java) {
        public override fun matchesSafely(view: LinearProgressIndicator): Boolean {
            return view.progress == expectedProgress
        }

        override fun describeTo(description: Description) {
            description.appendText("expected: ")
            description.appendText("" + expectedProgress)
        }
    }
}