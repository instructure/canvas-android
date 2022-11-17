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
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
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

    fun assertCourseNotDisplayed(course: CourseApiModel) {
        val matcher = allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        onView(matcher).check(doesNotExist())
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

    fun openUserSettingsPage() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerSettings).click()
    }

    fun gotoGlobalFiles() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_files).click()
    }

    fun assertCourseLabelTextColor(expectedTextColor: String) {
        onView(withId(R.id.courseLabel)).check(TextViewColorAssertion(expectedTextColor))
    }

    fun logOut() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_logout).scrollTo().click()
        onViewWithText(android.R.string.yes).click()
        // It can potentially take a long time for the sign-out to take effect, especially on
        // slow FTL devices.  So let's pause for a bit until we see the canvas logo.
        waitForMatcherWithSleeps(ViewMatchers.withId(R.id.canvasLogo), 10000).check(matches(isDisplayed()))
    }

    fun assertUserLoggedIn(user: CanvasUserApiModel) {
        onView(hamburgerButtonMatcher).click()
        onViewWithText(user.shortName).assertDisplayed()
        Espresso.pressBack()
    }

    fun assertUserLoggedIn(user: User) {
        onView(hamburgerButtonMatcher).click()
        onViewWithText(user.shortName!!).assertDisplayed()
        Espresso.pressBack()
    }

    fun assertUserLoggedIn(userName: String) {
        onView(hamburgerButtonMatcher).click()
        onViewWithText(userName).assertDisplayed()
        Espresso.pressBack()
    }

    fun pressChangeUser() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_changeUser).scrollTo().click()
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
}