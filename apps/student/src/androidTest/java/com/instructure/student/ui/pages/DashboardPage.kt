/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
@file:Suppress("unused")

package com.instructure.student.ui.pages

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf

class DashboardPage : BasePage(R.id.dashboardPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val listView by WaitForViewWithId(R.id.listView, autoAssert = false)
    private val hamburgerButton by OnViewWithContentDescription(R.string.navigation_drawer_open)

    // Sometimes when we navigate back to the dashboard page, there can be several hamburger buttons
    // in the UI stack.  We want to choose the one that is displayed.
    private val hamburgerButtonMatcher = allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())

    fun assertDisplaysCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.dashboard)).assertDisplayed()
        listView.assertDisplayed()
        onViewWithText("Courses").assertDisplayed()
        onViewWithText("See All").assertDisplayed()
    }

    fun assertDisplaysCourse(course: CourseApiModel) {
        val matcher = allOf(withText(course.name), withId(R.id.titleTextView))
        scrollAndAssertDisplayed(matcher)
    }

    fun assertDisplaysGroup(group: GroupApiModel, course: CourseApiModel) {
        val groupNameMatcher = allOf(withText(group.name), withId(R.id.groupNameView))
        scrollAndAssertDisplayed(groupNameMatcher)
        val groupDescriptionMatcher = allOf(withText(course.name), withId(R.id.groupCourseView))
        scrollAndAssertDisplayed(groupDescriptionMatcher)
    }

    fun assertDisplaysAddCourseMessage() {
        emptyView.assertDisplayed()
        onViewWithText(R.string.welcome).assertDisplayed()
        onViewWithText(R.string.emptyCourseListMessage).assertDisplayed()
        onViewWithId(R.id.addCoursesButton).assertDisplayed()
    }

    fun signOut() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_logout).click()
        onViewWithText(android.R.string.yes).click()
    }

    fun pressChangeUser() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_changeUser).click()
    }

    fun assertUserLoggedIn(user: CanvasUserApiModel) {
        onView(hamburgerButtonMatcher).click()
        onViewWithText(user.shortName).assertDisplayed()
        Espresso.pressBack()
    }

    fun assertUnreadEmails(count: Int) {
        onView(allOf(withParent(R.id.bottomNavigationInbox), withId(R.id.badge), withText(count.toString()))).assertDisplayed()
    }

    fun clickCalendarTab() {
        onView(withId(R.id.bottomNavigationCalendar)).click()
    }

    fun clickTodoTab() {
        onView(withId(R.id.bottomNavigationToDo)).click()
    }

    fun clickInboxTab() {
        onView(withId(R.id.bottomNavigationInbox)).click()
    }

    fun waitForRender() {
        WaitForViewWithId(R.id.listView)
        onView(hamburgerButtonMatcher).waitForCheck(matches(isDisplayed()))
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        // Scroll RecyclerView item into view, if necessary
        onView(AllOf.allOf(ViewMatchers.withId(R.id.listView), ViewMatchers.isDisplayed())) // The drawer (not displayed) also has a listView
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))

        // Now make sure that it is displayed
        Espresso.onView(matcher).assertDisplayed()
    }

    fun launchSettingsPage() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerSettings).click()
    }

    fun selectCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        onView(withText(course.name)).click()
    }
}
