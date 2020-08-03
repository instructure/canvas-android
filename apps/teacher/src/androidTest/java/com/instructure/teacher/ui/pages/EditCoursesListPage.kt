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

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Course
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R
import java.util.*

@Suppress("unused")
class EditCoursesListPage : BasePage() {

    private val favoritesRecyclerView by WaitForViewWithId(R.id.favoritesRecyclerView)

    private val star by WaitForViewWithId(R.id.star)

    fun assertHasCourses(mCourses: List<Course>) {

        // Check that the recyclerview count matches course count
        favoritesRecyclerView.check(RecyclerViewItemCountAssertion(mCourses.size))

        for (course in mCourses) onView(withText(course.name)).assertDisplayed()
    }

    fun assertCourseFavorited(course: Course) {
        val resources = InstrumentationRegistry.getTargetContext()
        val match = String.format(Locale.getDefault(), resources.getString(R.string.favorited_content_description), course.name, resources.getString(R.string.content_description_favorite))
        onViewWithText(course.name).check(matches(withContentDescription(match)))
    }

    fun assertCourseUnfavorited(course: Course) {
        val resources = InstrumentationRegistry.getTargetContext()
        val match = String.format(Locale.getDefault(), resources.getString(R.string.favorited_content_description), course.name, resources.getString(R.string.content_description_not_favorite))
        onViewWithText(course.name).check(matches(withContentDescription(match)))
    }

    fun toggleFavoritingCourse(course: Course) {
        waitForViewWithText(course.name).click()
    }
}
