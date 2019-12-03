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

import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.swipeUp
import org.hamcrest.Matcher
import org.hamcrest.Matchers

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
