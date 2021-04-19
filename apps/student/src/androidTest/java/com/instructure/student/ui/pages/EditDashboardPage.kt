/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.ui.pages

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withParent
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class EditDashboardPage : BasePage(R.id.editDashboardPage) {

    fun assertCourseDisplayed(course: Course) {
        val recyclerView = onView(withId(R.id.recyclerView))
//        val itemMatcher = allOf(withText(containsString(course.name)), withId(R.id.title))
//        onView(itemMatcher).assertDisplayed()
    }

    fun assertCourseNotFavorited(course: Course) {
        val itemMatcher = allOf(
                withContentDescription(containsString(", not favorite")),
                withText(containsString(course.name)),
                withId(R.id.title),
                withId(R.id.favoriteButton))
        waitForView(itemMatcher)
        onView(allOf(withId(R.id.recyclerView), withAncestor(R.id.editDashboardPage)))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(itemMatcher)))
        onView(itemMatcher).assertDisplayed()
    }

    fun toggleCourse(course: Course) {

    }

    fun assertCourseFavorited(course: Course) {
        val itemMatcher = allOf(
                withContentDescription(containsString(", favorite")),
                withText(containsString(course.name)),
                withId(R.id.title),
                withId(R.id.favoriteButton))
        onView(allOf(withId(R.id.recyclerView), withAncestor(R.id.editDashboardPage)))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(itemMatcher)))
        onView(itemMatcher).assertDisplayed()
    }
}