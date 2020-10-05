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
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

//
// This is a repo for useful custom matchers
//

/**
 * Matches if the view is a TextView and its text contains [textToMatch], case insensitive
 */
fun containsTextCaseInsensitive(textToMatch: String) : Matcher<View> {
    return object: BaseMatcher<View>() {
        override fun matches(item: Any?): Boolean {
            when(item) {
                is TextView -> {
                    return item.text.toString().contains(textToMatch, ignoreCase = true)
                }
            }
            return false
        }

        override fun describeTo(description: Description?) {
            description?.appendText("check to see if TextView contains \"$textToMatch\", case insensitive")
        }
    }
}

// Similar to containsTextCaseInsensitive(), but operates on a String rather than a TextView.
// Originally created to combat situations where a content description contains garbage characters.
fun stringContainsTextCaseInsensitive(textToMatch: String) : Matcher<String> {
    return object: BaseMatcher<String>() {
        override fun matches(item: Any?): Boolean {
            when(item) {
                is String -> {
                    return item.contains(textToMatch, ignoreCase = true)
                }
            }
            return false
        }

        override fun describeTo(description: Description?) {
            description?.appendText("check to see if String contains \"$textToMatch\", case insensitive")
        }
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

inline fun <reified T : View> typedViewCondition(crossinline onCheckCondition: (T) -> Boolean): Matcher<View> {
    return object : BaseMatcher<View>() {
        override fun matches(item: Any?): Boolean = (item as? T)?.let(onCheckCondition) ?: false
        override fun describeTo(description: Description?) {
            description?.appendText("matches view type '${T::class.java.simpleName}' and fulfills the given condition")
        }
    }
}

