/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.canvas.espresso

import android.app.Activity
import android.os.SystemClock
import android.os.SystemClock.sleep
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.viewpager.widget.ViewPager
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.espresso.swipeUp
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not

//
// This is a repo for generally useful Espresso actions
//

// Copied from https://stackoverflow.com/questions/33505953/espresso-how-to-test-swiperefreshlayout
// Allows you to perform an action with custom constraints.  This is especially useful for
// being able to perform a swipe on a view that is not quite 90% displayed.
fun withCustomConstraints(action: ViewAction, constraints: Matcher<View>): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return constraints
        }

        override fun getDescription(): String {
            return action.description
        }

        override fun perform(uiController: UiController, view: View) {
            action.perform(uiController, view)
        }
    }
}

/**
 * Scroll a recycler view to the given string target
 */
fun scrollRecyclerView(recyclerViewId: Int, target: String) {
    val matcher = ViewMatchers.withText(target)
    scrollRecyclerView(recyclerViewId, matcher)
}

/**
 * Scroll a recycler view to the given matcher target
 */
fun scrollRecyclerView(recyclerViewId: Int, target: Matcher<View>) {
    val recyclerViewMatcher = Matchers.allOf(ViewMatchers.withId(recyclerViewId), ViewMatchers.isDisplayed())

    // Grab the SwipeRefreshLayout, if one is available
    val swipeRefreshLayoutMatcher = getSwipeRefreshLayoutMatcher()

    // If a SwipeRefreshLayout is available, then allow a couple of swipe-refreshes if the list/recycler
    // is not immediately populated.  This is one thing that will allow us to recover from the
    // "late delayed job" situation.
    if(swipeRefreshLayoutMatcher != null) {
        var refreshesLeft = 2
        while(refreshesLeft > 0)  {
            try {
                onView(recyclerViewMatcher).assertDisplayed()
                break
            }
            catch(t: Throwable) {
                refreshesLeft -= 1
                onView(swipeRefreshLayoutMatcher)
                        .perform(withCustomConstraints(ViewActions.swipeDown(), ViewMatchers.isDisplayingAtLeast(5)))
                SystemClock.sleep(1000) // Allow some time to react to the update.
            }
        }
    }

    onView(recyclerViewMatcher)
            .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(target)))

    // If you have long recycler view elements and a short screen, it's possible that portions of
    // a recycler view element will still be off-screen when the logic above completes.  Let's
    // make an attempt to scroll up until "target" is displayed at least 10 percent.
    var swipesRemaining = 3
    while(!isDisplayedAtLeast(target, 10) && swipesRemaining > 0) {
        onView(recyclerViewMatcher).swipeUp()
        swipesRemaining -= 1
    }

}

/**
 * Returns a matcher for the SwipeRefreshLayout if one is available, otherwise null.
 */
private fun getSwipeRefreshLayoutMatcher(): Matcher<View>? {
    val swipeRefreshLayoutMatcher = allOf(ViewMatchers.isAssignableFrom(SwipeRefreshLayout::class.java), isDisplayed())
    try {
        onView(swipeRefreshLayoutMatcher).check(matches(isDisplayed()));
        return swipeRefreshLayoutMatcher
    }
    catch(e: Exception) {
        return null
    }
}


// Custom action to directly populate an EditText, bypassing the normal espresso actions
// of clicking on the EditText, typing into it, and then dismissing the soft keyboard.
class DirectlyPopulateEditText(val text: String) : ViewAction {
    override fun getDescription(): String {
        return "Populate EditText with $text"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(EditText::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        when(view) {
            is EditText -> {
                view.text.clear()
                view.text.append(text)
            }
        }
    }

}

/**
 * Convenience method to see whether (and by how much) a view is displayed on the screen.
 */
private fun isDisplayedAtLeast(target: Matcher<View>, displayPercentage: Int) : Boolean {
    try {
        onView(target).check(matches(ViewMatchers.isDisplayingAtLeast(displayPercentage)))
        return true
    }
    catch(t: Throwable) {
        return false
    }
}

/**
 * Send an explicit performClick() to a view, instead of using Espresso's click(), which
 * can sometimes be interpreted as a long-click.
 */
fun explicitClick() : ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Explicitly click on something"
        }

        override fun getConstraints(): Matcher<View> {
            return  ViewMatchers.isAssignableFrom(View::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            view?.performClick()
        }

    }
}

/**
 * Clear the focus of a view (i.e., remove focus from a view).
 */
fun clearFocus() : ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Explicitly remove focus from a view"
        }

        override fun getConstraints(): Matcher<View> {
            return  ViewMatchers.isAssignableFrom(View::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            view?.clearFocus()
        }

    }
}

/**
 * Helper method that clicks on the coordinates of a view based the x/y percentages given
 */
fun clickCoordinates(percentX: Float, percentY: Float) : ViewAction {
    return GeneralClickAction(
        Tap.SINGLE,
        CoordinatesProvider { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)
            val w = view.width
            val h = view.height

            val x = w * percentX
            val y = h * percentY

            val screenX = screenPos[0] + x
            val screenY = screenPos[1] + y

            floatArrayOf(screenX, screenY)
        },
        Press.FINGER,
        InputDevice.SOURCE_MOUSE,
        MotionEvent.BUTTON_PRIMARY
    )
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
                        .perform(withCustomConstraints(ViewActions.swipeDown(), ViewMatchers.isDisplayingAtLeast(5)))
                SystemClock.sleep(1000) // Allow some time to react to the update.

            }
        }
    }
}

/**
 * Refresh the current screen via pull-to-refresh.
 * Assumes that a SwipeRefreshLayout element is visible.
 */
fun refresh() {
    var swipeRefreshLayoutMatcher = getSwipeRefreshLayoutMatcher()
    if(swipeRefreshLayoutMatcher == null) {
        Thread.sleep(5000)
        swipeRefreshLayoutMatcher = getSwipeRefreshLayoutMatcher()
    }
    onView(swipeRefreshLayoutMatcher)
        .perform(withCustomConstraints(ViewActions.swipeDown(), ViewMatchers.isDisplayingAtLeast(5)))
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

class SetViewPagerCurrentItemAction(private val pageNumber: Int) : ViewAction {

    override fun getDescription() = "set ViewPager current item to $pageNumber"

    override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(ViewPager::class.java)

    override fun perform(uiController: UiController, view: View?) {
        val pager = view as ViewPager

        val adapter = pager.adapter ?: throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(RuntimeException("ViewPager adapter cannot be null"))
                .build()

        if (pageNumber >= adapter.count) throw PerformException.Builder()
                .withActionDescription(this.description)
            .withViewDescription(HumanReadables.describe(view))
            .withCause(IndexOutOfBoundsException("Requested page $pageNumber in ViewPager of size ${adapter.count}"))
            .build()

        pager.setCurrentItem(pageNumber, false)

        uiController.loopMainThreadUntilIdle()
    }

}

fun checkToastText(text: String, activity: Activity) {
    onView(withText(text)).inRoot(withDecorView(not(`is`(activity.window.decorView)))).check(matches(isDisplayed()))
}

fun checkToastText(@StringRes stringRes: Int, activity: Activity) {
    onView(withText(stringRes)).inRoot(withDecorView(not(`is`(activity.window.decorView)))).check(matches(isDisplayed()))

    retryWithIncreasingDelay(times = 5, initialDelay = 500, maxDelay = 15500) {
        try {
            onView(withText(stringRes)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        } catch (e: NoMatchingViewException) {
            //Intentionally empty as we would like to wait for the toast to disappear. Somehow doesNotExist() doesn't work because it passes even if the toast is still there and visible.
        }
    }
}

fun pressBackButton(times: Int) {
    for(i in 1..times) {
        Espresso.pressBack()
    }
}

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

fun toString(view: View): String {
    return HumanReadables.getViewHierarchyErrorMessage(view, null, "", null)
}

class TypeInRCETextEditor(val text: String) : ViewAction {
    override fun getDescription(): String {
        return "Enters text into an RCETextEditor"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(RCETextEditor::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        when(view) {
            is RCETextEditor -> view.applyHtml(text)
        }
    }

}