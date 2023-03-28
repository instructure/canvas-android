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
import androidx.test.espresso.matcher.ViewMatchers.*
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class DashboardPage : BasePage(R.id.dashboardPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val listView by WaitForViewWithId(R.id.listView, autoAssert = false)
    private val selectFavorites by WaitForViewWithId(R.id.editDashboardTextView)

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
        assertDisplaysCourse(course.name)
    }

    fun assertDisplaysCourse(courseName: String) {
        val matcher = allOf(withText(courseName), withId(R.id.titleTextView),  withAncestor(R.id.dashboardPage))
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

    fun assertCourseLabelTextColor(expectedTextColor: String) {
        onView(withId(R.id.courseLabel)).check(TextViewColorAssertion(expectedTextColor))
    }

    fun assertUnreadEmails(count: Int) {
        onView(withId(R.id.bottomBar)).check(NotificationBadgeAssertion(R.id.bottomNavigationInbox, count))
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
        Espresso.onView(matcher).scrollTo().assertDisplayed()
    }

    fun editFavorites() {
        selectFavorites.click()
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
        onView(withId(R.id.titleTextView) + withText(course.originalName)).perform(withCustomConstraints(click(), isDisplayingAtLeast(10)))
    }

    fun selectGroup(group: Group) {
        val groupNameMatcher = allOf(withText(group.name), withId(R.id.groupNameView))
        onView(groupNameMatcher).scrollTo().click()
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
        WaitForViewWithId(R.id.contentWebView)
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

    fun switchCourseView() {
        onView(ViewMatchers.withId(R.id.menu_dashboard_cards)).click()
    }

    fun clickEditDashboard() {
        onView(withId(R.id.editDashboardTextView)).click()
    }

    fun assertCourseNotDisplayed(course: CourseApiModel) {
        val matcher = allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        onView(matcher).check(doesNotExist())
    }

    fun changeCourseNickname(changeTo: String) {
        onView(withId(R.id.newCourseNickname)).replaceText(changeTo)
        onView(withText(R.string.ok) + withAncestor(R.id.buttonPanel)).click()
    }

    fun clickCourseOverflowMenu(courseTitle: String, menuTitle: String) {
        val courseOverflowMatcher = withId(R.id.overflow) + withAncestor(withId(R.id.cardView) + withDescendant(withId(R.id.titleTextView) + withText(courseTitle)))
        onView(courseOverflowMatcher).click()
        waitForView(withId(R.id.title) + withText(menuTitle)).click()
    }

    fun assertCourseGrade(courseName: String, courseGrade: String) {
        val siblingMatcher = allOf(withId(R.id.textContainer), withDescendant(withId(R.id.titleTextView) + withText(courseName)))
        val matcher = allOf(withId(R.id.gradeLayout), withDescendant(withId(R.id.gradeTextView) + withText(courseGrade)), hasSibling(siblingMatcher))

        onView(matcher).assertDisplayed()
    }

    fun assertCourseGradeNotDisplayed(courseName: String, courseGrade: String) {
        val siblingMatcher = allOf(withId(R.id.textContainer), withDescendant(withId(R.id.titleTextView) + withText(courseName)))
        val matcher = allOf(withId(R.id.gradeLayout), withDescendant(withId(R.id.gradeTextView) + withText(courseGrade)), hasSibling(siblingMatcher))

        onView(matcher).check(matches(Matchers.not(isDisplayed())))
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

