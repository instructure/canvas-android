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
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

@Suppress("unused")
class AllCoursesListPage : BasePage() {

    private val backButton by OnViewWithContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description)

    private val toolbarTitle by OnViewWithText(R.string.all_courses)

    private val coursesTab by OnViewWithId(R.id.tab_courses)

    private val inboxTab by OnViewWithId(R.id.tab_inbox)

    private val coursesRecyclerView by WaitForViewWithId(R.id.recyclerView)

    fun assertHasCourses(mCourses: List<Course>) {
        coursesRecyclerView.check(RecyclerViewItemCountAssertion(mCourses.size))
        for (course in mCourses) onView(withText(course.name)).assertDisplayed()
    }

    fun navigateBack() {
        backButton.click()
    }

    fun assertDisplaysCourse(course: CourseApiModel) {
        val matcher = CoreMatchers.allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        scrollAndAssertDisplayed(matcher)
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        onView(matcher).assertDisplayed()
    }

}
