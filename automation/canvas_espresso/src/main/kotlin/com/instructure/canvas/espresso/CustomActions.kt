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
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
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
    Espresso.onView(Matchers.allOf(ViewMatchers.withId(recyclerViewId), ViewMatchers.isDisplayed()))
            .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(target)))

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
