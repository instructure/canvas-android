/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.espresso.matchers

import android.util.DisplayMetrics
import com.google.android.material.textfield.TextInputLayout
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.RadioButton
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import com.instructure.espresso.ActivityHelper
import junit.framework.AssertionFailedError
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


fun checked(checked: Boolean = true, index: Int = 0, getText: (String) -> Unit = {}): BoundedMatcher<View, RadioButton> {
    return object : BoundedMatcher<View, RadioButton>(RadioButton::class.java) {
        var currentIndex = 0
        override fun describeTo(description: Description) {
            description.appendText("selected radio button ")
        }

        override fun matchesSafely(item: RadioButton): Boolean {
            if (index == currentIndex++ && item.isChecked == checked) {
                getText(item.text.toString())
                return true
            }
            currentIndex--
            return false
        }
    }
}


/**
 * @param matchTitle True if matching with title, false if matching with subtitle
 */
fun matchToolbarText(matchText: Matcher<String>, matchTitle: Boolean = true): BoundedMatcher<View, Toolbar> {
    return object : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with toolbar ${if (matchTitle) "title" else "subtitle"} ")
            matchText.describeTo(description)
        }

        override fun matchesSafely(view: Toolbar): Boolean {
            return matchText.matches(if (matchTitle) view.title else view.subtitle)
        }
    }
}

fun has(expectedCount: Int, selector: Matcher<View>): ViewAssertion {
    return ViewAssertion { view, noViewFoundException ->
        val rootView = view

        val descendantViews = breadthFirstViewTraversal(rootView)
        val selectedViews = ArrayList<View>()
        descendantViews.forEach {
            if (selector.matches(it)) {
                selectedViews.add(it)
            }
        }

        if (selectedViews.size != expectedCount) {
            val errorMessage = HumanReadables.getViewHierarchyErrorMessage(rootView,
                    selectedViews,
                    String.format("Found %d views instead of %d matching: %s", selectedViews.size, expectedCount, selector),
                    "****MATCHES****")
            throw AssertionFailedError(errorMessage);
        }
    }
}

fun hasTextInputLayoutErrorText(stringResId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {

        override fun matchesSafely(view: View): Boolean {
            if (view !is TextInputLayout) {
                return false
            }

            val error = view.error ?: return false

            val hint = error.toString()

            val actualErrorMsg = view.resources.getString(stringResId)

            return actualErrorMsg == hint
        }

        override fun describeTo(description: Description) {}
    }
}

fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        internal var currentIndex = 0

        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}


// A matcher for views whose width is less than the specified amount (in dp),
// but whose height is at least the specified amount.
// This is used to suppress accessibility failures related to overflow menus
// in action bars being to narrow.
fun withOnlyWidthLessThan(dimInDp: Int) : BaseMatcher<AccessibilityViewCheckResult>
{
    val densityDpi = ActivityHelper.currentActivity().resources.displayMetrics.densityDpi
    val dim_f = dimInDp * (densityDpi.toDouble() / DisplayMetrics.DENSITY_DEFAULT.toDouble())
    val dim = dim_f.toInt()
    return object : BaseMatcher<AccessibilityViewCheckResult>() {
        override fun describeTo(description: Description?) {
            description?.appendText("checking whether width < $dim && height >= $dim")
        }

        override fun matches(item: Any): Boolean {
            when(item) {
                is AccessibilityViewCheckResult -> {
                    return item.view.width < dim && item.view.height >= dim
                }
                else -> return false
            }
        }

    }
}
