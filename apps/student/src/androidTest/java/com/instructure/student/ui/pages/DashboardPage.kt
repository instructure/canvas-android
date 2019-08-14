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
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Course
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
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matcher

class DashboardPage : BasePage(R.id.dashboardPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val listView by WaitForViewWithId(R.id.listView, autoAssert = false)
    private val selectFavorites by WaitForViewWithId(R.id.selectFavorites)
    private val seeAllCoursesButton by WaitForViewWithId(R.id.seeAllTextView)
    private val hamburgerButton by OnViewWithContentDescription(R.string.navigation_drawer_open)

    fun assertDisplaysCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.dashboard)).assertDisplayed()
        listView.assertDisplayed()
        onViewWithText("Courses").assertDisplayed()
        onViewWithText("See All").assertDisplayed()
    }

    fun assertDisplaysCourse(course: CourseApiModel) {
        // Odd to specify isDisplayed() when I'm about to assert that it is displayed,
        // but it serves to differentiate the "all courses" version of the course from
        // the "favorites" version of the course.  We'll select whichever is currently showing.
        val matcher = allOf(withText(course.name), withId(R.id.titleTextView), isDisplayed())
        scrollAndAssertDisplayed(matcher)
    }

    fun assertDisplaysCourse(course: Course) {
        val matcher = withText(containsString(course.originalName!!)) + withId(R.id.titleTextView) // + isDisplayed()
        scrollAndAssertDisplayed(matcher)
    }

    fun assertCourseNotShown(course: Course) {
        onView(withText(course.originalName)).check(doesNotExist())
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

    fun assertSeeAllDisplayed() {
        seeAllCoursesButton.assertDisplayed()
    }

    fun clickSeeAll() {
        seeAllCoursesButton.click()
    }

    fun signOut() {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerItem_logout).click()
        onViewWithText(android.R.string.yes).click()
    }

    fun pressChangeUser() {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerItem_changeUser).click()
    }

    fun assertUserLoggedIn(user: CanvasUserApiModel) {
        hamburgerButton.click()
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

    fun waitForRender() {
        listView.waitForCheck(matches(isDisplayed()))
        //listView.assertDisplayed() // Oddly, this seems sufficient as a wait-for-render mechanism
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        // Scroll RecyclerView item into view, if necessary
        onView(allOf(withId(R.id.listView), isDisplayed())) // There may be other listViews
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))

        // Now make sure that it is displayed
        Espresso.onView(matcher).assertDisplayed()
    }

    fun editFavorites() {
        selectFavorites.click()
    }

    fun setShowGrades(showGrades: Boolean) {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerShowGradesSwitch).perform(SetSwitchCompat(showGrades))
        Espresso.pressBack()
    }

    // Assumes one course, which is favorited
    fun assertShowsGrades() {
        onView(withId(R.id.gradeTextView)).assertDisplayed()
    }

    // Assumes one course, which is favorited
    fun assertHidesGrades() {
        onView(withId(R.id.gradeTextView)).assertNotDisplayed()
    }

    fun selectCourse(course: Course) {
        assertDisplaysCourse(course)
        onView(withText(course.originalName)).click()
    }

    fun selectCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        onView(withText(course.name)).click()
    }

}

/**
 * Custom ViewAction to set a SwitchCompat to the desired on/off position
 * [position]: true -> "on", false -> "off"
 */
class SetSwitchCompat(val position: Boolean) : ViewAction {
    override fun getDescription(): String {
        val desiredPosition =  if(position) "On" else "Off"
        return "Set SwitchCompat to $desiredPosition"
    }

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(SwitchCompat::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        val switch = view as SwitchCompat
        if(switch != null) {
            switch.isChecked = position
        }
    }

}

