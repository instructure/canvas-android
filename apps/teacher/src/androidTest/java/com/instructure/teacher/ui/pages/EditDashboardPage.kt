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
package com.instructure.teacher.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R

@Suppress("unused")
class EditDashboardPage : BasePage() {

    private val favouriteButton by WaitForViewWithId(R.id.favoriteButton)
    private val allCoursesLabel by WaitForViewWithText(R.string.allCourses)

    fun assertHasCourses(mCourses: List<Course>) {
        // Check that the recyclerview count matches course count
        onView(withId(R.id.recyclerView)).check(RecyclerViewItemCountAssertion(mCourses.size))
        for (course in mCourses) Espresso.onView(ViewMatchers.withText(course.name)).assertDisplayed()
    }

    fun assertHasCourse(courseName: String) {
        onView(withText(courseName) + withAncestor(withId(R.id.recyclerView))).assertDisplayed()
    }

    fun assertCourseFavoured(course: Course) {
        onView(withId(R.id.favoriteButton) + hasSibling(withId(R.id.title) + withText(course.name))).check(
            matches(withContentDescription(R.string.a11y_content_description_remove_from_dashboard)))
    }

    fun assertCourseUnfavoured(course: Course) {
        onView(withId(R.id.favoriteButton) + hasSibling(withId(R.id.title) + withText(course.name))).check(
            matches(withContentDescription(R.string.a11y_content_description_add_to_dashboard)))
    }

    fun toggleFavouringCourse(courseName: String) {
        onView(withId(R.id.favoriteButton) + hasSibling(withId(R.id.title) + withText(courseName))).click()
    }

    fun assertMassSelectButtonIsDisplayed(someSelected: Boolean) {
        if (someSelected) onView(withText(R.string.unselect_all)).assertDisplayed()
        else onView(withText(R.string.select_all)).assertDisplayed()
    }

    fun clickOnMassSelectButton(someSelected: Boolean) {
        assertMassSelectButtonIsDisplayed(someSelected)
        if (someSelected) onView(withText(R.string.unselect_all) + withAncestor(R.id.selectButton)).click()
        else onView(withText(R.string.select_all) + withAncestor(R.id.selectButton)).click()
    }
}
