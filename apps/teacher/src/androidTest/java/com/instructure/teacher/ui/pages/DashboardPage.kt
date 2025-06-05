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
import com.instructure.espresso.page.waitForViewWithText
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

/**
 * Represents a page for the dashboard.
 *
 * This class extends the `BasePage` class and provides methods for interacting with the dashboard,
 * such as asserting the display of courses, empty view, and course title; opening and switching courses;
 * clicking on the All Courses button and course overflow menu; changing the course nickname;
 * asserting the display of notifications; and opening the inbox and todo tabs.
 *
 * @constructor Creates an instance of the `DashboardPage` class.
 */
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
    private val calendarTab by WaitForViewWithId(R.id.tab_calendar)
    private val inboxTab by WaitForViewWithId(R.id.tab_inbox)
    private val previousLoginTitleText by OnViewWithId(
        R.id.previousLoginTitleText,
        autoAssert = false
    )

    private val hamburgerButtonMatcher =
        allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())

    /**
     * Asserts that the course specified by [course] is displayed in the dashboard.
     *
     * @param course The course to be displayed.
     */
    fun assertDisplaysCourse(course: CourseApiModel) {
        val matcher = allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        scrollAndAssertDisplayed(matcher)
    }

    /**
     * Asserts that the course with the specified [courseName] is displayed in the dashboard.
     *
     * @param courseName The name of the course to be displayed.
     */
    fun assertDisplaysCourse(courseName: String) {
        val matcher = allOf(
            withText(courseName),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        scrollAndAssertDisplayed(matcher)
    }

    /**
     * Asserts that all the courses in the specified [mCourses] list are displayed in the dashboard.
     *
     * @param mCourses The list of courses to be displayed.
     */
    fun assertHasCourses(mCourses: List<Course>) {
        for (course in mCourses) onView(
            withId(R.id.titleTextView) + withText(course.name) + withAncestor(
                R.id.swipeRefreshLayout
            )
        ).assertDisplayed()
    }

    /**
     * Asserts that the course specified by [course] is not displayed in the dashboard.
     *
     * @param course The course to be checked.
     */
    fun assertCourseNotDisplayed(course: CourseApiModel) {
        val matcher = allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        onView(matcher).check(doesNotExist())
    }

    /**
     * Asserts that the empty view is displayed in the dashboard.
     */
    fun assertEmptyView() {
        emptyView.assertDisplayed()
    }

    /**
     * Asserts that the course title specified by [courseTitle] is displayed in the dashboard.
     *
     */

    /**
     * Asserts that the course title specified by [courseTitle] is displayed in the dashboard.
     *
     * @param courseTitle The title of the course to be displayed.
     */
    fun assertCourseTitle(courseTitle: String) {
        onView(withId(R.id.titleTextView) + withText(courseTitle) + withAncestor(R.id.swipeRefreshLayout)).assertDisplayed()
    }

    /**
     * Asserts that the dashboard displays the courses, including the toolbar, courses view, and All Courses button.
     */
    fun assertDisplaysCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.dashboard)).assertDisplayed()
        coursesView.assertDisplayed()
        editDashboardButton.assertDisplayed()
    }

    /**
     * Asserts that the specified course is displayed and opens it.
     *
     * @param course The course to be displayed and opened.
     */
    fun assertOpensCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        openCourse(courseName = course.name)
        onView(withId(R.id.courseBrowserTitle)).assertContainsText(course.name)
    }

    /**
     * Clicks on the All Courses button.
     */
    fun clickEditDashboard() {
        onView(withId(R.id.editDashboardTextView)).click()
    }

    /**
     * Opens the course with the specified [courseName].
     *
     * @param courseName The name of the course to be opened.
     */
    fun openCourse(courseName: String) {
        onView(withText(courseName)).click()
    }

    /**
     * Opens the specified [course].
     *
     * @param course The course to be opened.
     */
    fun openCourse(course: CourseApiModel) {
        onView(withText(course.name)).click()
    }

    /**
     * Opens the specified [course].
     *
     * @param course The course to be opened.
     */
    fun openCourse(course: Course) {
        onView(withText(course.name)).click()
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        onView(matcher).assertDisplayed()
    }

    /**
     * Waits for the dashboard to finish rendering.
     */
    fun waitForRender() {
        onView(hamburgerButtonMatcher).waitForCheck(matches(isDisplayed()))
    }

    /**
     * Opens the inbox tab.
     */
    fun openInbox() {
        inboxTab.click()
    }

    fun openCalendar() {
        calendarTab.click()
    }

    /**
     * Opens the todo tab.
     */
    fun openTodo() {
        todoTab.click()
    }

    /**
     * Asserts that the course label text color matches the specified [expectedTextColor].
     *
     * @param expectedTextColor The expected text color for the course label.
     */
    fun assertCourseLabelTextColor(expectedTextColor: String) {
        onView(withId(R.id.courseLabel)).check(TextViewColorAssertion(expectedTextColor))
    }

    /**
     * Selects the specified [course] in the dashboard.
     *
     * @param course The course to be selected.
     */
    fun selectCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        onView(withText(course.name)).click()
    }

    /**
     * Asserts that the 'Login Required' dialog is displayed.
     */
    fun assertLoginRequiredDialog() {
        waitForViewWithText(R.string.loginRequired).assertDisplayed()
    }

    /**
     * Clicks on the 'LOG IN' button on the 'Login Required' dialog.
     */
    fun clickLogInOnLoginRequiredDialog() {
        onView(withText("LOG IN")).click()
    }

    /**
     * Switches the course view in the dashboard.
     */
    fun switchCourseView() {
        onView(withId(R.id.menu_dashboard_cards)).click()
    }

    /**
     * Clicks on the overflow menu of the specified [courseTitle] and selects the menu item with the specified [menuTitle].
     *
     * @param courseTitle The title of the course.
     * @param menuTitle The title of the menu item.
     */
    fun clickCourseOverflowMenu(courseTitle: String, menuTitle: String) {
        val courseOverflowMatcher = withId(R.id.overflow)+withAncestor(withId(R.id.cardView)+withDescendant(withId(R.id.titleTextView)+withText(courseTitle)))
        onView(courseOverflowMatcher).click()
        waitForView(withId(R.id.title) + withText(menuTitle)).click()
    }

    /**
     * Changes the course nickname to the specified [changeTo].
     *
     * @param changeTo The new nickname for the course.
     */
    fun changeCourseNickname(changeTo: String) {
        onView(withId(R.id.newCourseNickname)).replaceText(changeTo)
        onView(withText(android.R.string.ok) + withAncestor(R.id.buttonPanel)).click()
    }

    /**
     * Asserts that the specified [accountNotification] is displayed in the dashboard.
     *
     * @param accountNotification The account notification to be displayed.
     */
    fun assertNotificationDisplayed(accountNotification: AccountNotification) {
        onView(
            withId(R.id.announcementTitle) + withAncestor(R.id.announcementContainer) + withText(
                accountNotification.subject
            )
        ).assertDisplayed()
    }

    /**
     * Clicks on the specified [accountNotification] in the dashboard.
     *
     * @param accountNotification The account notification to be clicked.
     */
    fun clickOnNotification(accountNotification: AccountNotification) {
        onView(
            withId(R.id.announcementTitle) + withAncestor(R.id.announcementContainer) + withText(
                accountNotification.subject
            )
        ).click()
    }
}