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
package com.instructure.teacher.ui.utils

import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object SwipeRefreshLayoutMatchers {
    fun isRefreshing(isRefreshing: Boolean): Matcher<View> {
        return object : BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout::class.java) {

            override fun describeTo(description: Description) {
                description.appendText(if (isRefreshing) "is refreshing" else "is not refreshing")
            }

            override fun matchesSafely(view: SwipeRefreshLayout): Boolean {
                return view.isRefreshing == isRefreshing
            }
        }
    }
}

object ViewSizeMatcher {
    fun hasWidth(pixels: Int): Matcher<View> = object : TypeSafeMatcher<View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has a width of ${pixels}px")
        }

        override fun matchesSafely(view: View): Boolean = view.width == pixels
    }

    fun hasHeight(pixels: Int): Matcher<View> = object : TypeSafeMatcher<View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has a height of ${pixels}px")
        }

        override fun matchesSafely(view: View): Boolean = view.height == pixels
    }

    fun hasMinWidth(pixels: Int): Matcher<View> = object : TypeSafeMatcher<View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has a minimum width of ${pixels}px")
        }

        override fun matchesSafely(view: View): Boolean = view.width >= pixels
    }

    fun hasMinHeight(pixels: Int): Matcher<View> = object : TypeSafeMatcher<View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has a minimum height of ${pixels}px")
        }

        override fun matchesSafely(view: View): Boolean = view.height >= pixels
    }
}
