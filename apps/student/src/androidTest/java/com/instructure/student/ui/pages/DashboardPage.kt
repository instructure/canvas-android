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
import androidx.test.espresso.Espresso
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
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
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matcher

class DashboardPage : BasePage(R.id.dashboardPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val listView by WaitForViewWithId(R.id.listView, autoAssert = false)
    private val selectFavorites by WaitForViewWithId(R.id.editDashboardTextView)
    private val hamburgerButton by OnViewWithContentDescription(R.string.navigation_drawer_open)

    // Sometimes when we navigate back to the dashboard page, there can be several hamburger buttons
    // in the UI stack.  We want to choose the one that is displayed.
    private val hamburgerButtonMatcher = allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())

    fun assertDisplaysCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.dashboard)).assertDisplayed()
        listView.assertDisplayed()
        onViewWithText("Courses").assertDisplayed()
        onViewWithText("Edit Dashboard").assertDisplayed()
    }

    fun assertDisplaysCourse(course: CourseApiModel) {
        val matcher = allOf(withText(course.name), withId(R.id.titleTextView),  withAncestor(R.id.dashboardPage))
        scrollAndAssertDisplayed(matcher)
    }

    fun assertDisplaysCourse(course: Course) {
        val matcher = withText(containsString(course.originalName!!)) + withId(R.id.titleTextView) + isDisplayed()
        try {
            // This is the RIGHT way to do it, but it inexplicably fails most of the time.
            scrollRecyclerView(R.id.listView, matcher)
            onView(matcher).assertDisplayed()
        }
        catch(pe: PerformException) {
            // Revert to this weaker operation if the one above fails.
            scrollAndAssertDisplayed(matcher)
        }
    }

    fun assertCourseNotShown(course: Course) {
        onView(withText(course.originalName)).check(doesNotExist())
    }

    fun assertDisplaysGroup(group: GroupApiModel, course: CourseApiModel) {
        assertDisplaysGroupCommon(group.name, course.name)
    }

    fun assertDisplaysGroup(group: Group, course: Course) {
        assertDisplaysGroupCommon(group.name!!, course.name)
    }

    private fun assertDisplaysGroupCommon(groupName: String, courseName: String) {
        val groupNameMatcher = allOf(withText(groupName), withId(R.id.groupNameView))
        onView(groupNameMatcher).scrollTo().assertDisplayed()
        val groupDescriptionMatcher = allOf(withText(courseName), withId(R.id.groupCourseView))
        onView(groupDescriptionMatcher).scrollTo().assertDisplayed()
    }

    fun assertDisplaysAddCourseMessage() {
        emptyView.assertDisplayed()
        onViewWithText(R.string.welcome).assertDisplayed()
        onViewWithText(R.string.emptyCourseListMessage).assertDisplayed()
        onViewWithId(R.id.addCoursesButton).assertDisplayed()
    }

    fun signOut() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_logout).scrollTo().click()
        onViewWithText(android.R.string.yes).click()
        // It can potentially take a long time for the sign-out to take effect, especially on
        // slow FTL devices.  So let's pause for a bit until we see the canvas logo.
        waitForMatcherWithSleeps(ViewMatchers.withId(R.id.canvasLogo), 20000).check(matches(isDisplayed()))

    }

    fun pressChangeUser() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_changeUser).scrollTo().click()
    }

    fun goToHelp() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_help).scrollTo().click()
    }

    fun gotoGlobalFiles() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_files).click()
    }

    fun gotoBookmarks() {
        onView(hamburgerButtonMatcher).click()
        onViewWithId(R.id.navigationDrawerItem_bookmarks).click()
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

    fun assertUnreadEmails(count: Int) {
        onView(allOf(withParent(R.id.bottomNavigationInbox), withId(R.id.badge), withText(count.toString()))).assertDisplayed()
    }

    fun clickCalendarTab() {
        onView(withId(R.id.bottomNavigationCalendar)).click()
    }

    fun clickTodoTab() {
        onView(withId(R.id.bottomNavigationToDo)).click()
    }

    fun clickNotificationsTab() {
        onView(withId(R.id.bottomNavigationNotifications)).click()
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
        onView(withText(course.originalName)).perform(withCustomConstraints(click(), isDisplayingAtLeast(10)))
    }

    fun selectGroup(group: Group) {
        val groupNameMatcher = allOf(withText(group.name), withId(R.id.groupNameView))
        onView(groupNameMatcher).scrollTo().click()
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
    fun assertAnnouncementDetailsDisplayed(announcement: AccountNotification) {
        WaitForViewWithId(R.id.canvasWebView)
        // Include isDisplayed() in the matcher to differentiate from other views with this text
        onView(withText(announcement.subject) + isDisplayed()).assertDisplayed()
    }

    fun tapAnnouncement() {
        onView(withId(R.id.tapToView)).assertDisplayed().click()
    }

    fun dismissAnnouncement() {
        onView(withId(R.id.dismissImageButton)).click()
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout) + withAncestor(R.id.dashboardPage)).swipeDown()
    }

    fun assertAnnouncementGoneAndCheckAfterRefresh() {
        assertAnnouncementsGone()
        refresh()
        assertAnnouncementsGone()
    }

    fun assertInviteShowing(courseName: String) {
        onView(withText(courseName) + withAncestor(R.id.dashboardNotifications)).assertDisplayed()
    }

    fun acceptInvite() {
        onView(withId(R.id.acceptButtonWrapper)).click()
    }

    fun declineInvite() {
        onView(withId(R.id.declineButtonWrapper)).click()
    }

    fun assertInviteAccepted() {
        onView(withText("Invite accepted!") + withAncestor(R.id.dashboardNotifications)).assertDisplayed()
    }

    fun assertInviteDeclined() {
        onView(withText("Invite declined.") + withAncestor(R.id.dashboardNotifications)).assertDisplayed()
    }

    fun assertInviteGone(courseName: String) {
        onView(withText(courseName) + withAncestor(R.id.dashboardNotifications)).check(doesNotExist())
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

