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
 *
 */
package com.instructure.teacher.ui.pages.classic

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.teacher.R

/**
 * A page representing the All Courses screen in the application.
 */
@Suppress("unused")
class EditDashboardPage : BasePage() {

    /**
     * The button for favoriting a course.
     */
    private val favouriteButton by WaitForViewWithId(R.id.favoriteButton)

    /**
     * The label for the "All Courses" section.
     */
    private val allCoursesLabel by WaitForViewWithText(R.string.allCourses)

    /**
     * Asserts that the All Courses screen displays the given list of courses.
     *
     * @param mCourses The list of courses to verify.
     */
    fun assertHasCourses(mCourses: List<Course>) {
        onView(withId(R.id.recyclerView)).check(RecyclerViewItemCountAssertion(mCourses.size))
        for (course in mCourses) {
            Espresso.onView(ViewMatchers.withText(course.name)).assertDisplayed()
        }
    }

    /**
     * Asserts that the All Courses screen displays a specific course.
     *
     * @param courseName The name of the course to verify.
     */
    fun assertHasCourse(courseName: String) {
        onView(withText(courseName) + withAncestor(withId(R.id.recyclerView))).assertDisplayed()
    }

    /**
     * Asserts that a specific course is favored in the All Courses screen.
     *
     * @param course The course to verify.
     */
    fun assertCourseFavoured(course: Course) {
        onView(withId(R.id.favoriteButton) + hasSibling(withId(R.id.title) + withText(course.name))).check(
            matches(withContentDescription(R.string.a11y_content_description_remove_from_dashboard))
        )
    }

    /**
     * Asserts that a specific course is not favored in the All Courses screen.
     *
     * @param course The course to verify.
     */
    fun assertCourseUnfavoured(course: Course) {
        onView(withId(R.id.favoriteButton) + hasSibling(withId(R.id.title) + withText(course.name))).check(
            matches(withContentDescription(R.string.a11y_content_description_add_to_dashboard))
        )
    }

    /**
     * Toggles favoring/unfavoring a specific course in the All Courses screen.
     *
     * @param courseName The name of the course to toggle favoring.
     */
    fun toggleFavouringCourse(courseName: String) {
        onView(withId(R.id.favoriteButton) + hasSibling(withId(R.id.title) + withText(courseName))).click()
    }

    /**
     * Asserts that the mass select button is displayed with the appropriate label based on whether
     * some items are selected or not.
     *
     * @param someSelected Indicates whether some items are selected or not.
     */
    fun assertMassSelectButtonIsDisplayed(someSelected: Boolean) {
        if (someSelected) {
            onView(withText(R.string.unselect_all)).assertDisplayed()
        } else {
            onView(withText(R.string.select_all)).assertDisplayed()
        }
    }

    /**
     * Clicks on the mass select button in the All Courses screen based on the selection state.
     *
     * @param someSelected Indicates whether some items are selected or not.
     */
    fun clickOnMassSelectButton(someSelected: Boolean) {
        assertMassSelectButtonIsDisplayed(someSelected)
        if (someSelected) {
            onView(withText(R.string.unselect_all) + withAncestor(R.id.selectButton)).click()
        } else {
            onView(withText(R.string.select_all) + withAncestor(R.id.selectButton)).click()
        }
    }
}
