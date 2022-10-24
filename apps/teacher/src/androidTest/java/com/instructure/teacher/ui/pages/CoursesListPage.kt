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
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import com.instructure.canvasapi2.models.Course
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.WaitForToolbarTitle
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

@Suppress("unused")
class CoursesListPage : BasePage() {

    private val toolbarTitle by WaitForToolbarTitle(R.string.courses)

    private val menuEditFavoritesButton by WaitForViewWithId(R.id.menu_edit_favorite_courses)

    private val coursesTab by OnViewWithId(R.id.tab_courses)

    private val inboxTab by OnViewWithId(R.id.tab_inbox)

    // Only displays if the user has courses
    private val coursesLabel by WaitForViewWithId(R.id.courseLabel)

    // Only displays if the user has favorite courses
    private val seeAllCoursesLabel by WaitForViewWithId(R.id.seeAllTextView)

    // Only displays if the user has no favorite courses
    private val emptyMessageLayout by WaitForViewWithId(R.id.emptyCoursesView)

    private val coursesRecyclerView by WaitForViewWithId(R.id.courseRecyclerView)

    fun assertDisplaysNoCoursesView() {
        emptyMessageLayout.assertDisplayed()
    }

    fun assertHasCourses(mCourses: List<Course>) {
        coursesLabel.assertDisplayed()
        seeAllCoursesLabel.assertDisplayed()

        // Check that the recyclerview count matches course count
        coursesRecyclerView.check(RecyclerViewItemCountAssertion(mCourses.size))

        for (course in mCourses) onView(withText(course.name)).assertDisplayed()
    }

    fun assertDisplaysCourse(courseName: String) {
        val matcher = CoreMatchers.allOf(
            withText(courseName),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        scrollAndAssertDisplayed(matcher)
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        onView(matcher).assertDisplayed()
    }

    fun openAllCoursesList() {
        seeAllCoursesLabel.click()
    }

    fun openCourse(course: CourseApiModel) {
        callOnClick(withText(course.name))
    }

    fun openCourse(course: Course) {
        callOnClick(withText(course.name))
    }

    fun openCourseAtPosition(position: Int) {
        // Add one to the position to account for the header in list
        coursesRecyclerView.perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click()))
    }

    fun openEditFavorites() {
        menuEditFavoritesButton.click()
    }
}
