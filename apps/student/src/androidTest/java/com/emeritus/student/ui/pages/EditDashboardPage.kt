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

package com.emeritus.student.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.emeritus.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class EditDashboardPage : BasePage(R.id.editDashboardPage) {

    fun assertCourseDisplayed(course: Course) {
        val itemMatcher = allOf(withText(containsString(course.name)), withId(R.id.title))
        onView(itemMatcher).assertDisplayed()
    }

    fun assertCourseNotFavorited(course: Course) {
        val childMatcher = withContentDescription("Add to dashboard")
        val itemMatcher = allOf(
                withContentDescription(containsString(", not favorite")),
                withContentDescription(containsString(course.name)),
                hasDescendant(childMatcher))
        onView(itemMatcher).assertDisplayed()
    }

    fun unfavoriteCourse(course: Course) {
        unfavoriteCourse(course.name)
    }

    fun unfavoriteCourse(courseName: String) {
        val childMatcher = withContentDescription("Remove from dashboard")
        val itemMatcher = allOf(
                withContentDescription(containsString(", favorite")),
                withContentDescription(containsString(courseName)),
                hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).click()
    }

    fun favoriteCourse(course: Course) {
        favoriteCourse(course.name)
    }

    fun favoriteCourse(courseName: String) {
        val childMatcher = withContentDescription("Add to dashboard")
        val itemMatcher = allOf(
            withContentDescription(containsString(", not favorite")),
            withContentDescription(containsString(courseName)),
            hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).click()
    }

    fun assertCourseFavorited(course: Course) {
        val childMatcher = withContentDescription("Remove from dashboard")
        val itemMatcher = allOf(
                withContentDescription(containsString(", favorite")),
                withContentDescription(containsString(course.name)),
                hasDescendant(childMatcher))
        onView(itemMatcher).assertDisplayed()
    }

    fun selectAllCourses() {
        val childMatcher = withContentDescription("Add all to dashboard")
        val itemMatcher = allOf(hasDescendant(withText("All courses")), hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).click()
    }

    fun unselectAllCourses() {
        val childMatcher = withContentDescription("Remove all from dashboard")
        val itemMatcher = allOf(hasDescendant(withText("All courses")), hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).click()
    }

    fun assertCourseMassSelectButtonIsDisplayed(someSelected: Boolean) {

        if (someSelected) {
            val childMatcher = withContentDescription("Remove all from dashboard")
            val itemMatcher = allOf(hasDescendant(withText("All courses")), hasDescendant(childMatcher))

            onView(withParent(itemMatcher) + childMatcher).assertDisplayed()
        }
        else {
            val childMatcher = withContentDescription("Add all to dashboard")
            val itemMatcher = allOf(hasDescendant(withText("All courses")), hasDescendant(childMatcher))

            onView(withParent(itemMatcher) + childMatcher).assertDisplayed()
        }
    }

}