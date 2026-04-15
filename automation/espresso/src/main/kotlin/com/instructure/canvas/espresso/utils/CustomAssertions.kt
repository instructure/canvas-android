/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 *
 */
package com.instructure.canvas.espresso.utils

import android.app.Activity
import android.os.SystemClock.sleep
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.retryWithIncreasingDelay
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher

// This file is for generally useful Espresso and/or Compose UI assertions.

/**
 * Check the toast text if it's displayed.
 * @param text The string of the toast text.
 * @param activity The activity to get the decor view from.
 */
fun checkToastText(text: String, activity: Activity) {
    onView(withText(text)).inRoot(withDecorView(not(`is`(activity.window.decorView)))).check(matches(isDisplayed()))
}

/**
 * Check the toast text if it's displayed.
 * @param stringRes The string resource ID of the toast text.
 * @param activity The activity to get the decor view from.
 */
fun checkToastText(@StringRes stringRes: Int, activity: Activity) {
    retryWithIncreasingDelay(times = 3, initialDelay = 500, maxDelay = 5000) {
        try {
            onView(withText(stringRes)).inRoot(withDecorView(not(`is`(activity.window.decorView)))).check(matches(isDisplayed()))
        } catch (e: NoMatchingViewException) {
            // Intentionally empty: Toast did not appear yet, so try to check it again.
        }
    }

    retryWithIncreasingDelay(times = 5, initialDelay = 500, maxDelay = 15500) {
        try {
            onView(withText(stringRes)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        } catch (e: NoMatchingViewException) {
            //Intentionally empty as we would like to wait for the toast to disappear. Somehow doesNotExist() doesn't work because it passes even if the toast is still there and visible.
        }
    }
}

/**
 * Asserts that the SwipeRefreshLayout is refreshing or not.
 */
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

/**
 * Asserts that the TextView uses the specified font size in scaled pixels.
 * @param expectedSP The expected font size in scaled pixels.
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

/**
 * Asserts that a view is completely above of another view.
 * @param other The other view to compare against.
 */
fun ViewInteraction.assertCompletelyAbove(other: ViewInteraction) {
    val view1 = getView()
    val view2 = other.getView()
    val location1 = view1.locationOnScreen
    val location2 = view2.locationOnScreen
    val isAbove = location1[1] + view1.height <= location2[1]
    assertThat("completely above", isAbove, CoreMatchers.`is`(true))
}

/**
 * Asserts that a view is completely below of another view.
 * @param other The other view to compare against.
 */
fun ViewInteraction.assertCompletelyBelow(other: ViewInteraction) {
    val view1 = getView()
    val view2 = other.getView()
    val location1 = view1.locationOnScreen
    val location2 = view2.locationOnScreen
    val isAbove = location2[1] + view2.height <= location1[1]
    assertThat("completely below", isAbove, CoreMatchers.`is`(true))
}

/**
 * Get the location of the view on the screen.
 */
val View.locationOnScreen get() = IntArray(2).apply { getLocationOnScreen(this) }

/**
 * Assert that a TextView has the specified number of lines.
 * @param lineCount The number of the expected lines.
 */
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

/**
 * Waits for a view to disappear.
 * @param viewMatcher The matcher for the view to wait for.
 * @param timeoutInSeconds The timeout in seconds.
 */
fun waitForViewToDisappear(viewMatcher: Matcher<View>, timeoutInSeconds: Long) {
    val startTime = System.currentTimeMillis()

    while (System.currentTimeMillis() - startTime < (timeoutInSeconds * 1000)) {
        try {
            onView(viewMatcher)
                .check(ViewAssertions.doesNotExist())
            return
        } catch (e: AssertionError) {
            Thread.sleep(200)
        }
    }
    throw AssertionError("The view has not been displayed within $timeoutInSeconds seconds.")
}

/**
 * Wait for a specified matcher to appear, trying a couple of pull-to-refreshes before giving up.
 * This is one way to combat the "late delayed job" problem.
 */
fun waitForMatcherWithRefreshes(target: Matcher<View>) {
    val swipeRefreshLayoutMatcher = getSwipeRefreshLayoutMatcher()

    if(swipeRefreshLayoutMatcher != null) {
        var refreshesLeft = 2;
        while(refreshesLeft > 0) {
            try {
                onView(target).assertDisplayed()
                return
            }
            catch(t: Throwable) {
                refreshesLeft -= 1
                onView(swipeRefreshLayoutMatcher)
                    .perform(actionWithCustomConstraints(ViewActions.swipeDown(), ViewMatchers.isDisplayingAtLeast(5)))
                sleep(1000) // Allow some time to react to the update.

            }
        }
    }
}

/** A better version of the shared espresso lib's WaitForViewMatcher.waitForView()
 *  for a couple of reasons:
 *    (1) It allows the caller to specify the wait time
 *    (2) It uses sleeps to avoid monopolizing the CPU and spamming our log files.
 *
 *    Waits for [target] to become visible for up to [waitMs] milliseconds,
 *    sleeping [sleepMs] milliseconds after every attempt.
 */
fun waitForMatcherWithSleeps(target: Matcher<View>, timeout: Long = 10000, pollInterval: Long = 100) : ViewInteraction {
    val endTime = System.currentTimeMillis() + timeout
    do {
        try {
            return onView(target).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        } catch (ignored: Exception) {
            println("There is an exception occurred. Stacktrace: " + ignored.stackTrace)
        } catch (ignored: Error) {
            println("There is an error occurred. Stacktrace: " + ignored.stackTrace)
        }

        sleep(pollInterval) // re-check every 100 ms
    } while(System.currentTimeMillis() < endTime)

    // If we aren't successful by now, make one more unprotected attempt to throw
    // the correct error.
    return onView(target).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
}

/**
 * Convenience method to see whether (and by how much) a view is displayed on the screen.
 */
fun isDisplayedAtLeast(target: Matcher<View>, displayPercentage: Int) : Boolean {
    try {
        onView(target).check(matches(ViewMatchers.isDisplayingAtLeast(displayPercentage)))
        return true
    }
    catch(t: Throwable) {
        return false
    }
}

/**
 * Returns true if the element with the given resource id is currently displayed, false otherwise.
 */
fun isElementDisplayed(resourceId: Int) : Boolean {
    try {
        onView(withId(resourceId)).check(matches(isDisplayed()))
        return true
    }
    catch(t: Throwable) {
        return false
    }
}