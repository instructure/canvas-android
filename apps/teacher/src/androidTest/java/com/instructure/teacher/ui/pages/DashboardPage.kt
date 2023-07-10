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
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.TextViewColorAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

class DashboardPage : BasePage() {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val coursesPageLabel by WaitForViewWithText(R.string.dashboard)
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val coursesView by OnViewWithId(R.id.swipeRefreshLayout, autoAssert = false)
    private val coursesHeaderWrapper by OnViewWithId(R.id.coursesHeaderWrapper, autoAssert = false)
    private val courseLabel by WaitForViewWithId(R.id.courseLabel)
    private val editDashboardButton by WaitForViewWithId(R.id.editDashboardTextView)
    private val bottomBar by OnViewWithId(R.id.bottomBar)
    private val coursesTab by WaitForViewWithId(R.id.tab_courses)
    private val todoTab by WaitForViewWithId(R.id.tab_todo)
    private val inboxTab by WaitForViewWithId(R.id.tab_inbox)
    private val previousLoginTitleText by OnViewWithId(R.id.previousLoginTitleText, autoAssert = false)

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

    fun assertDisplaysCourse(courseName: String) {
        val matcher = allOf(
            withText(courseName),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        scrollAndAssertDisplayed(matcher)
    }

    fun assertHasCourses(mCourses: List<Course>) {
        for (course in mCourses) onView(withId(R.id.titleTextView) + withText(course.name) + withAncestor(R.id.swipeRefreshLayout)).assertDisplayed()
    }

    fun assertCourseNotDisplayed(course: CourseApiModel) {
        val matcher = allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        onView(matcher).check(doesNotExist())
    }

    fun assertEmptyView() {
        emptyView.assertDisplayed()
    }

    fun assertCourseTitle(courseTitle: String) {
        onView(withId(R.id.titleTextView) + withText(courseTitle) + withAncestor(R.id.swipeRefreshLayout)).assertDisplayed()
    }

    fun assertDisplaysCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.dashboard)).assertDisplayed()
        coursesView.assertDisplayed()
        editDashboardButton.assertDisplayed()
    }

    fun assertOpensCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        openCourse(courseName = course.name)
        onView(withId(R.id.courseBrowserTitle)).assertContainsText(course.name)
    }

    fun clickEditDashboard() {
        onView(withId(R.id.editDashboardTextView)).click()
    }

    fun openCourse(courseName: String) {
        onView(withText(courseName)).click()
    }

    fun openCourse(course: CourseApiModel) {
        onView(withText(course.name)).click()
    }

    fun openCourse(course: Course) {
        onView(withText(course.name)).click()
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

    fun assertCourseLabelTextColor(expectedTextColor: String) {
        onView(withId(R.id.courseLabel)).check(TextViewColorAssertion(expectedTextColor))
    }

    fun selectCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        onView(withText(course.name)).click()
    }

    fun switchCourseView() {
        onView(withId(R.id.menu_dashboard_cards)).click()
    }

    fun clickCourseOverflowMenu(courseTitle: String, menuTitle: String) {
        val courseOverflowMatcher = withId(R.id.overflow) + withAncestor(withId(R.id.cardView) + withDescendant(withId(R.id.titleTextView) + withText(courseTitle)))
        onView(courseOverflowMatcher).click()
        waitForView(withId(R.id.title) + withText(menuTitle)).click()
    }

    fun changeCourseNickname(changeTo: String) {
        onView(withId(R.id.newCourseNickname)).replaceText(changeTo)
        onView(withText(R.string.ok) + withAncestor(R.id.buttonPanel)).click()
    }

    fun assertNotificationDisplayed(accountNotification: AccountNotification) {
        onView(withId(R.id.announcementTitle) + withAncestor(R.id.announcementContainer) + withText(accountNotification.subject)).assertDisplayed()
    }

    fun clickOnNotification(accountNotification: AccountNotification) {
        onView(withId(R.id.announcementTitle) + withAncestor(R.id.announcementContainer) + withText(accountNotification.subject)).click()
    }

}