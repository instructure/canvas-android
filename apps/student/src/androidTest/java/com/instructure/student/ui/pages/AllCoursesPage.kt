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

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeUp
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class AllCoursesPage : BasePage(R.id.editDashboardPage) {

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
                withContentDescription(containsString("Course $courseName, favorite")),
                hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).click()
    }

    fun favoriteCourse(course: Course) {
        favoriteCourse(course.name)
    }

    fun favoriteCourse(courseName: String) {
        val childMatcher = withContentDescription("Add to dashboard")
        val itemMatcher = allOf(
            withContentDescription(containsString("Course $courseName, not favorite")),
            hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).click()
    }

    fun favoriteGroup(groupName: String) {
        val childMatcher = withContentDescription("Add to dashboard")
        val itemMatcher = allOf(
            withContentDescription(containsString("Group $groupName, not favorite")),
            hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).scrollTo().click()
    }

    fun unfavoriteGroup(groupName: String) {
        val childMatcher = withContentDescription("Remove from dashboard")
        val itemMatcher = allOf(
            withContentDescription(containsString("Group $groupName, favorite")),
            hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).scrollTo().click()
    }

    fun assertCourseFavorited(course: Course) {
        val childMatcher = withContentDescription("Remove from dashboard")
        val itemMatcher = allOf(
                withContentDescription(containsString("Course ${course.name}, favorite")),
                hasDescendant(childMatcher))
        onView(itemMatcher).assertDisplayed()
    }

    fun selectAllCourses() {
        val childMatcher = withContentDescription("Add all to dashboard")
        val itemMatcher = allOf(hasDescendant(withText(R.string.allCoursesCourseHeader)), hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).click()
    }

    fun unselectAllCourses() {
        val childMatcher = withContentDescription("Remove all from dashboard")
        val itemMatcher = allOf(hasDescendant(withText(R.string.allCoursesCourseHeader)), hasDescendant(childMatcher))

        onView(withParent(itemMatcher) + childMatcher).click()
    }

    fun assertCourseMassSelectButtonIsDisplayed(someSelected: Boolean) {

        if (someSelected) {
            val childMatcher = withContentDescription("Remove all from dashboard")
            val itemMatcher = allOf(hasDescendant(withText(R.string.allCoursesCourseHeader)), hasDescendant(childMatcher))

            onView(withParent(itemMatcher) + childMatcher).assertDisplayed()
        }
        else {
            val childMatcher = withContentDescription("Add all to dashboard")
            val itemMatcher = allOf(hasDescendant(withText(R.string.allCoursesCourseHeader)), hasDescendant(childMatcher))

            onView(withParent(itemMatcher) + childMatcher).assertDisplayed()
        }
    }

    fun assertGroupMassSelectButtonIsDisplayed(someSelected: Boolean) {
        if (someSelected) {
            val itemMatcher = withContentDescription("Remove all from dashboard")
            val parentMatcher = allOf(hasDescendant(withText(R.string.allCoursesGroupHeader)), hasDescendant(itemMatcher))
            onView(withParent(parentMatcher) + itemMatcher).scrollTo().assertDisplayed()
        }
        else {
            val itemMatcher = withContentDescription("Add all to dashboard")
            val parentMatcher = allOf(hasDescendant(withText(R.string.allCoursesGroupHeader)), hasDescendant(itemMatcher))
            onView(withParent(parentMatcher) + itemMatcher).scrollTo().assertDisplayed()
        }
    }

    fun swipeUp() {
        onView(withId(R.id.swipeRefreshLayout) + withParent(withId(R.id.editDashboardPage))).swipeUp()
    }

}