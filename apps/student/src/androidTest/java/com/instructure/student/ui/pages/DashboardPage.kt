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

import android.os.SystemClock.sleep
import android.view.View
import android.widget.TextView
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
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher

class DashboardPage : BasePage(R.id.dashboardPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val listView by WaitForViewWithId(R.id.listView, autoAssert = false)
    private val selectFavorites by WaitForViewWithId(R.id.selectFavorites)
    private val seeAllCoursesButton by WaitForViewWithId(R.id.seeAllTextView)
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
        val matcher = allOf(withText(course.name), withId(R.id.titleTextView),  withAncestor(R.id.dashboardPage))
        scrollAndAssertDisplayed(matcher)
    }

    fun assertDisplaysCourse(course: Course) {
        val matcher = withText(containsString(course.originalName!!)) + withId(R.id.titleTextView) + isDisplayed()
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
        //onView(allOf(withId(R.id.listView),  withAncestor(R.id.dashboardPage))).waitForCheck(matches(isDisplayed()))
        //listView.waitForCheck(matches(isDisplayed()))
        onView(hamburgerButtonMatcher).waitForCheck(matches(isDisplayed()))
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        // Arggghhh... This scrolling logic on the recycler view is really unreliable and seems
        // to fail for nonsensical reasons.  For now, "scrollAndAssertDisplayed"" is just going to
        // have to be "assertDisplayed".
        // Scroll RecyclerView item into view, if necessary
//        onView(allOf(withId(R.id.listView), withAncestor(R.id.dashboardPage))) // There may be other listViews
//                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))

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

    fun launchSettingsPage() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerSettings).click()
    }

    fun selectCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        onView(withText(course.name)).click()
    }

    fun assertAnnouncementShowing(announcement: AccountNotification) {
        onView(withId(R.id.announcementIcon)).assertDisplayed()
        onView(withId(R.id.announcementTitle) + withText(announcement.subject)).assertDisplayed()
    }

    fun assertAnnouncementsGone() {
        onView(withId(R.id.announcementIcon)).check(doesNotExist())
    }

    // Assumes that a single announcement is showing
    fun tapAnnouncementAndAssertDisplayed(announcement: AccountNotification) {
        onView(withId(R.id.tapToView)).assertDisplayed().click()
        WaitForViewWithId(R.id.canvasWebView)
        // Include isDisplayed() in the matcher to differentiate from other views with this text
        onView(withText(announcement.subject) + isDisplayed()).assertDisplayed()
    }

    fun dismissAnnouncement() {
        onView(withId(R.id.dismissImageButton)).click()
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout) + withAncestor(R.id.dashboardPage)).swipeDown()
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

class ContainsSubtextOf(val superString: String, val minMatchChars: Int) : BaseMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.appendText("matches text that is contained  in $superString")
    }

    override fun matches(item: Any?): Boolean {
        if(item is TextView) {
            val itemText = item.text.toString()
            return itemText.length >= minMatchChars && superString.contains(itemText, true)
        }
        return false
    }

}

