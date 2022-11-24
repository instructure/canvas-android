/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.ui.utils

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun ViewInteraction.assertLineCount(lineCount: Int) {
    val matcher = object : TypeSafeMatcher<View>() {
        override fun matchesSafely(item: View): Boolean {
            return (item as TextView).lineCount == lineCount
        }

        override fun describeTo(description: Description) {
            description.appendText("isTextInLines")
        }
    }
    check(matches(matcher))
}


fun ViewInteraction.getView(): View {
    lateinit var matchingView: View
    perform(object : ViewAction {
        override fun getDescription() = "Get View reference"

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(View::class.java)
        }

        override fun perform(uiController: UiController?, view: View) {
            matchingView = view
        }
    })
    return matchingView
}

fun ViewInteraction.assertCompletelyAbove(other: ViewInteraction) {
    val view1 = getView()
    val view2 = other.getView()
    val location1 = view1.locationOnScreen
    val location2 = view2.locationOnScreen
    val isAbove = location1[1] + view1.height <= location2[1]
    assertThat("completely above", isAbove, `is`(true))
}

fun ViewInteraction.assertCompletelyBelow(other: ViewInteraction) {
    val view1 = getView()
    val view2 = other.getView()
    val location1 = view1.locationOnScreen
    val location2 = view2.locationOnScreen
    val isAbove = location2[1] + view2.height <= location1[1]
    assertThat("completely below", isAbove, `is`(true))
}

val View.locationOnScreen get() = IntArray(2).apply { getLocationOnScreen(this) }


/**
 * Asserts that the TextView uses the specified font size in scaled pixels
 */
fun ViewInteraction.assertFontSizeSP(expectedSP: Float) {
    val matcher = object : TypeSafeMatcher<View>(View::class.java) {

        override fun matchesSafely(target: View): Boolean {
            if (target !is TextView) return false
            val actualSP = target.textSize / target.getResources().displayMetrics.scaledDensity
            return actualSP.compareTo(expectedSP) == 0
        }

        override fun describeTo(description: Description) {
            description.appendText("with fontSize: ${expectedSP}px")
        }
    }
    check(matches(matcher))
}

fun ViewInteraction.assertIsRefreshing(isRefreshing: Boolean) {
    val matcher = object : BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout::class.java) {

        override fun describeTo(description: Description) {
            description.appendText(if (isRefreshing) "is refreshing" else "is not refreshing")
        }

        override fun matchesSafely(view: SwipeRefreshLayout): Boolean {
            return view.isRefreshing == isRefreshing
        }
    }
    check(matches(matcher))
}

open class IntentActionMatcher(private val intentType: String, private val dataMatcher: String) : TypeSafeMatcher<Intent>() {

    override fun describeTo(description: Description?) {
        description?.appendText("Intent Matcher")
    }

    override fun matchesSafely(item: Intent?): Boolean {
        return (intentType == item?.action) && (item?.dataString?.contains(dataMatcher) ?: false)
    }
}
