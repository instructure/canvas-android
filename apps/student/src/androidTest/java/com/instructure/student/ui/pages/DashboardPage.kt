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
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.espresso.NotificationBadgeAssertion
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.TextViewColorAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForViewToBeCompletelyDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.espresso.retry
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import com.instructure.student.ui.utils.ViewUtils
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
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
        onViewWithText("All Courses").assertDisplayed()
    }

    fun assertDisplaysCourse(course: CourseApiModel) {
        assertDisplaysCourse(course.name)
    }

    fun assertDisplaysCourse(courseName: String) {
        val matcher = allOf(withText(courseName), withId(R.id.titleTextView), withAncestor(R.id.dashboardPage))
        waitForView(matcher).scrollTo().assertDisplayed()
    }

    fun assertDisplaysCourse(course: Course) {
        val matcher = withText(containsString(course.originalName!!)) + withId(R.id.titleTextView) + isDisplayed()
        try {
            // This is the RIGHT way to do it, but it inexplicably fails most of the time.
            scrollRecyclerView(R.id.listView, matcher)
            onView(matcher).assertDisplayed()
        } catch (pe: PerformException) {
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

    fun assertDisplaysGroup(group: GroupApiModel, courseName: String) {
        assertDisplaysGroupCommon(group.name, courseName)
    }

    fun assertDisplaysGroup(group: Group, course: Course) {
        assertDisplaysGroupCommon(group.name!!, course.name)
    }

    private fun assertDisplaysGroupCommon(groupName: String, courseName: String) {
        val groupNameMatcher = allOf(withText(groupName), withId(R.id.groupNameView))
        waitForView(groupNameMatcher).scrollTo().assertDisplayed()
        val groupDescriptionMatcher = allOf(withText(courseName), withId(R.id.groupCourseView), hasSibling(groupNameMatcher))
        waitForView(groupDescriptionMatcher).scrollTo().assertDisplayed()
    }

    fun assertDisplaysAddCourseMessage() {
        emptyView.assertDisplayed()
        waitForViewWithText(R.string.welcome).assertDisplayed()
        waitForViewWithText(R.string.emptyCourseListMessage).assertDisplayed()
        waitForViewWithId(R.id.addCoursesButton).assertDisplayed()
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

    fun openLeftSideMenu() {
        onView(hamburgerButtonMatcher).click()
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

    fun assertGradeText(gradeText: String) {
        onViewWithId(R.id.gradeTextView).assertHasText(gradeText)
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
        waitForView(groupNameMatcher).scrollTo().click()
    }

    fun selectCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        onView(withText(course.name) + withId(R.id.titleTextView)).click()
    }

    fun selectGroup(group: GroupApiModel) {
        val groupNameMatcher = allOf(withText(group.name), withId(R.id.groupNameView))
        onView(groupNameMatcher).scrollTo().click()
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
        clickDashboardGlobalOverflowButton()
        onView(withText(containsString("Switch to")))
            .perform(click());
    }

    fun openGlobalManageOfflineContentPage() {
       clickDashboardGlobalOverflowButton()
        // We need this, because sometimes after sync we have a sync notification that covers the text for a couple of seconds.
        retry(times = 10) {
            onView(withText(containsString("Manage Offline Content")))
                .perform(click());
        }
    }

    private fun clickDashboardGlobalOverflowButton() {
        waitForViewToBeCompletelyDisplayed(withContentDescription("More options") + withAncestor(R.id.toolbar))
        // We need this, because sometimes after sync we have a sync notification that covers the overflow button for a couple of seconds.
        retry(times = 10) {
            Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        }
    }

    fun openAllCoursesPage() {
        waitForView(withId(R.id.editDashboardTextView)).scrollTo().click()
    }

    fun assertCourseNotDisplayed(course: CourseApiModel) {
        val matcher = allOf(
            withText(course.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        onView(matcher).check(doesNotExist())
    }

    fun assertGroupNotDisplayed(group: GroupApiModel) {
        val matcher = allOf(
            withText(group.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        onView(matcher).check(doesNotExist())
    }

    fun assertGroupNotDisplayed(group: Group) {
        val matcher = allOf(
            withText(group.name),
            withId(R.id.titleTextView),
            withAncestor(R.id.swipeRefreshLayout)
        )
        onView(matcher).check(doesNotExist())
    }

    fun changeCourseNickname(changeTo: String) {
        onView(withId(R.id.newCourseNickname)).replaceText(changeTo)
        onView(withText(android.R.string.ok) + withAncestor(R.id.buttonPanel)).click()
    }

    fun clickCourseOverflowMenu(courseTitle: String, menuTitle: String) {
        clickOnCourseOverflowButton(courseTitle)
        waitForView(withId(R.id.title) + withText(menuTitle)).click()
    }

    fun clickOnCourseOverflowButton(courseTitle: String) {
        val courseOverflowMatcher = withId(R.id.overflow) + withAncestor(
            withId(R.id.cardView)
                    + withDescendant(withId(R.id.titleTextView) + withText(courseTitle))
        )
        waitForView(courseOverflowMatcher).scrollTo().click()
    }

    fun assertCourseGrade(courseName: String, courseGrade: String) {
        val siblingMatcher = allOf(withId(R.id.textContainer), withDescendant(withId(R.id.titleTextView) + withText(courseName)))
        val matcher = allOf(withId(R.id.gradeLayout), withDescendant(withId(R.id.gradeTextView) + withText(courseGrade)), hasSibling(siblingMatcher))

        onView(matcher).scrollTo().assertDisplayed()
    }

    fun assertCourseGradeNotDisplayed(courseName: String, courseGrade: String) {
        val siblingMatcher = allOf(withId(R.id.textContainer), withDescendant(withId(R.id.titleTextView) + withText(courseName)))
        val matcher = allOf(withId(R.id.gradeLayout), withDescendant(withId(R.id.gradeTextView) + withText(courseGrade)), hasSibling(siblingMatcher))

        onView(matcher).check(matches(Matchers.not(isDisplayed())))
    }

    fun assertDashboardNotificationDisplayed(title: String, subTitle: String) {
        onView(withId(R.id.uploadTitle) + withText(title)).assertDisplayed()
        onView(withId(R.id.uploadSubtitle) + withText(subTitle)).assertDisplayed()
    }

    fun clickOnDashboardNotification(subTitle: String) {
        onView(withId(R.id.uploadSubtitle) + withText(subTitle)).click()
    }

    //OfflineMethod
    fun assertOfflineIndicatorDisplayed() {
        waitForView(withId(R.id.offlineIndicator)).assertDisplayed()
    }

    //OfflineMethod
    fun assertOfflineIndicatorNotDisplayed() {
        onView(withId(R.id.offlineIndicator)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    //OfflineMethod
    fun waitForOfflineIndicatorNotDisplayed() {
        assertDisplaysCourses()
        retry(times = 5, delay = 2000) {
            assertOfflineIndicatorNotDisplayed()
        }
    }

    //OfflineMethod
    fun waitForOfflineIndicatorDisplayed() {
        assertDisplaysCourses()
        retry(times = 5, delay = 2000) {
            assertOfflineIndicatorDisplayed()
        }
    }

    //OfflineMethod
    fun waitForOfflineSyncDashboardNotifications() {
        waitForSyncProgressDownloadStartedNotification()
        waitForSyncProgressDownloadStartedNotificationToDisappear()

        waitForSyncProgressStartingNotification()
        waitForSyncProgressStartingNotificationToDisappear()
    }

    //OfflineMethod
    fun assertCourseOfflineSyncIconVisible(courseName: String) {
        WaitForViewMatcher.waitForView(withId(R.id.offlineSyncIcon) + hasSibling(withId(R.id.titleTextView) + withText(courseName)), 20).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    //OfflineMethod
    fun assertCourseOfflineSyncIconGone(courseName: String) {
        onView(withId(R.id.offlineSyncIcon) + hasSibling(withId(R.id.titleTextView) + withText(courseName))).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    //OfflineMethod
    fun clickOnSyncProgressNotification() {
        Thread.sleep(2500)
        onView(withId(R.id.syncProgressTitle) + anyOf(withText(R.string.syncProgress_syncQueued),withText(R.string.syncProgress_downloadStarting), withText(R.string.syncProgress_syncingOfflineContent))).click()
    }

    //OfflineMethod
    fun waitForSyncProgressDownloadStartedNotificationToDisappear() {
        ViewUtils.waitForViewToDisappear(withText(com.instructure.pandautils.R.string.syncProgress_downloadStarting), 30)
    }

    //OfflineMethod
    fun waitForSyncProgressDownloadStartedNotification() {
        waitForView(withText(com.instructure.pandautils.R.string.syncProgress_downloadStarting)).assertDisplayed()
    }

    //OfflineMethod
    fun waitForSyncProgressStartingNotification() {
        waitForView(withText(com.instructure.pandautils.R.string.syncProgress_syncingOfflineContent)).assertDisplayed()
    }

    //OfflineMethod
    fun waitForSyncProgressStartingNotificationToDisappear() {
        ViewUtils.waitForViewToDisappear(withText(com.instructure.pandautils.R.string.syncProgress_syncingOfflineContent), 30)
    }

    //OfflineMethod
    fun assertBottomMenusAreDisabled() {
        onView(withId(R.id.bottomNavigationCalendar)).check(matches(isNotEnabled()))
        onView(withId(R.id.bottomNavigationToDo)).check(matches(isNotEnabled()))
        onView(withId(R.id.bottomNavigationNotifications)).check(matches(isNotEnabled()))
        onView(withId(R.id.bottomNavigationInbox)).check(matches(isNotEnabled()))
    }

    fun goToDashboard() {
        onView(withId(R.id.bottomNavigationHome)).click()
    }

}

/**
 * Custom ViewAction to set a SwitchCompat to the desired on/off position
 * [position]: true -> "on", false -> "off"
 */
class SetSwitchCompat(val position: Boolean) : ViewAction {
    override fun getDescription(): String {
        val desiredPosition = if (position) "On" else "Off"
        return "Set SwitchCompat to $desiredPosition"
    }

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(SwitchCompat::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        val switch = view as SwitchCompat
        if (switch != null) {
            switch.isChecked = position
        }
    }

}

