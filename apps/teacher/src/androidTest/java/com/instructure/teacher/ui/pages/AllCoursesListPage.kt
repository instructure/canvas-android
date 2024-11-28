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

import android.view.View
import com.instructure.canvasapi2.models.Course
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

/**
 * All courses list page
 *
 * @constructor Create empty All courses list page
 */
@Suppress("unused")
class AllCoursesListPage : BasePage() {

    private val backButton by OnViewWithContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description)

    private val toolbarTitle by OnViewWithText(R.string.all_courses)

    private val coursesTab by OnViewWithId(R.id.tab_courses)

    private val inboxTab by OnViewWithId(R.id.tab_inbox)

    private val coursesRecyclerView by WaitForViewWithId(R.id.recyclerView)

    /**
     * Assert that all the courses given in the parameter list is displayed in the corresponding recycler view.
     *
     * @param mCourses: The Course object list parameter.
     */
    fun assertHasCourses(mCourses: List<Course>) {
        coursesRecyclerView.check(RecyclerViewItemCountAssertion(mCourses.size))
        for (course in mCourses) onView(withText(course.name)).assertDisplayed()
    }

    /**
     * Navigate back
     *
     */
    fun navigateBack() {
        backButton.click()
    }

    /**
     * Assert that the course given in the parameter is displayed.
     *
     * @param course: The CourseApiModel object parameter.
     */
    fun assertDisplaysCourse(course: CourseApiModel) {
        val matcher = CoreMatchers.allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        scrollAndAssertDisplayed(matcher)
    }

    /**
     * Scroll to and assert that the View matcher given in the parameter is displayed.
     *
     * @param matcher: The View matcher parameter.
     */
    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        onView(matcher).scrollTo().assertDisplayed()
    }

}
