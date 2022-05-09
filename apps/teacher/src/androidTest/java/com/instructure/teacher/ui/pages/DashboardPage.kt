/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

class DashboardPage : BasePage() {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val editFavoriteCourses by WaitForViewWithId(R.id.menu_edit_favorite_courses)
    private val coursesPageLabel by WaitForViewWithStringText("Courses")
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val coursesView by OnViewWithId(R.id.swipeRefreshLayout, autoAssert = false)
    private val coursesHeaderWrapper by OnViewWithId(R.id.coursesHeaderWrapper, autoAssert = false)
    private val courseLabel by WaitForViewWithId(R.id.courseLabel)
    private val seeAllCoursesButton by WaitForViewWithId(R.id.seeAllTextView)
    private val bottomBar by OnViewWithId(R.id.bottomBar)
    private val coursesTab by WaitForViewWithId(R.id.tab_courses)
    private val todoTab by WaitForViewWithId(R.id.tab_todo)
    private val inboxTab by WaitForViewWithId(R.id.tab_inbox)

    private val hamburgerButtonMatcher =
        allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())

    fun assertDisplaysCourse(course: CourseApiModel) {
        val matcher = allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        scrollAndAssertDisplayed(matcher)
    }

    fun assertDisplaysCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.courses)).assertDisplayed()
        coursesView.assertDisplayed()
        seeAllCoursesButton.assertDisplayed()
    }

    fun assertOpensCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        openCourse(courseName = course.name)
        onView(withId(R.id.courseBrowserTitle)).assertContainsText(course.name)
    }

    fun clickSeeAll() {
        onView(withId(R.id.seeAllTextView)).click()
    }

    fun openEditCoursesListPage() {
        onView(withId(R.id.menu_edit_favorite_courses)).click()
    }

    fun openCourse(courseName: String) {
        onView(withText(courseName)).click()
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        onView(matcher).assertDisplayed()
    }

    fun waitForRender() {
        onView(hamburgerButtonMatcher).waitForCheck(matches(isDisplayed()))
    }

    fun openInbox() {
        inboxTab.click()
    }

    fun openTodo() {
        todoTab.click()
    }

    fun gotoGlobalFiles() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_files).click()
    }
}