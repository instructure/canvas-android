/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.ui.pages.compose

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.instructure.composetest.isTagDisplayed
import com.instructure.composetest.waitForTag
import com.instructure.espresso.page.BasePage
import com.instructure.pandautils.R

class NewDashboardPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    private val targetContext get() = InstrumentationRegistry.getInstrumentation().targetContext

    fun assertDashboardDisplayed() {
        composeTestRule.waitForTag(DASHBOARD_PULL_REFRESH_INDICATOR_TAG)
        composeTestRule.onNodeWithTag(DASHBOARD_PULL_REFRESH_INDICATOR_TAG).assertExists()
    }

    fun refreshDashboard() {
        Thread.sleep(500)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val x = device.displayWidth / 2
        val startY = (device.displayHeight * 0.3).toInt()
        val endY = (device.displayHeight * 0.8).toInt()
        device.swipe(x, startY, x, endY, 20)
        composeTestRule.waitForIdle()
    }

    fun assertWelcomeGreetingDisplayed(expectedGreeting: String, timeoutMillis: Long = 20000) {
        val matcher = hasTestTag(WELCOME_GREETING_TAG).and(hasText(expectedGreeting))
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(matcher).assertExists()
    }

    fun assertWelcomeMessageDisplayed() {
        composeTestRule.waitForTag(WELCOME_MESSAGE_TAG)
        composeTestRule.onNodeWithTag(WELCOME_MESSAGE_TAG).assertIsDisplayed()
    }

    fun assertCourseInvitationsHeaderCount(count: Int) {
        val expected = targetContext.getString(R.string.courseInvitationsTitle, count)
        composeTestRule.onNodeWithText(expected).assertIsDisplayed()
    }

    fun assertCoursesHeaderCount(count: Int) {
        val expected = targetContext.getString(R.string.collapsibleSectionTitle, targetContext.getString(R.string.courses), count)
        composeTestRule.onNodeWithText(expected).performScrollTo().assertIsDisplayed()
    }

    fun assertCourseInvitationDisplayed(courseId: Long, courseName: String) {
        composeTestRule.waitForTag("$COURSE_INVITATION_CARD_TAG_PREFIX$courseId")
        bringCourseInvitationIntoView(courseId)
        composeTestRule.onNodeWithTag("$COURSE_INVITATION_NAME_TAG_PREFIX$courseId")
            .assertIsDisplayed()
            .assert(hasText(courseName))
    }

    fun acceptCourseInvitation(courseId: Long) {
        bringCourseInvitationIntoView(courseId)
        composeTestRule.onNodeWithTag("$COURSE_INVITATION_ACCEPT_BUTTON_TAG_PREFIX$courseId").performClick()
        composeTestRule.waitForIdle()
    }

    fun declineCourseInvitation(courseId: Long) {
        bringCourseInvitationIntoView(courseId)
        composeTestRule.onNodeWithTag("$COURSE_INVITATION_DECLINE_BUTTON_TAG_PREFIX$courseId").performClick()
        composeTestRule.waitForTag(COURSE_INVITATION_CONFIRM_DECLINE_BUTTON_TAG)
        composeTestRule.onNodeWithTag(COURSE_INVITATION_CONFIRM_DECLINE_BUTTON_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    private fun bringCourseInvitationIntoView(courseId: Long) {
        bringPagerItemIntoView(COURSE_INVITATIONS_PAGER_TAG, "$COURSE_INVITATION_ACCEPT_BUTTON_TAG_PREFIX$courseId")
    }

    private fun bringPagerItemIntoView(pagerTag: String, displayedTag: String, useUnmergedTree: Boolean = false) {
        if (composeTestRule.isTagDisplayed(displayedTag, useUnmergedTree)) return

        repeat(MAX_PAGER_SWIPES) { swipePager(pagerTag, forward = false) }

        repeat(MAX_PAGER_SWIPES) {
            if (composeTestRule.isTagDisplayed(displayedTag, useUnmergedTree)) return
            swipePager(pagerTag, forward = true)
        }
    }

    private fun swipePager(pagerTag: String, forward: Boolean) {
        composeTestRule.onNodeWithTag(pagerTag).performTouchInput {
            if (forward) {
                swipeLeft(startX = centerX + (width / 4), endX = centerX - (width / 4))
            } else {
                swipeRight(startX = centerX - (width / 4), endX = centerX + (width / 4))
            }
        }
        composeTestRule.waitForIdle()
    }

    fun assertCourseInvitationGone(courseId: Long, timeoutMillis: Long = 20000) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodesWithTag("$COURSE_INVITATION_CARD_TAG_PREFIX$courseId").fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag("$COURSE_INVITATION_CARD_TAG_PREFIX$courseId").assertDoesNotExist()
    }

    fun assertCourseDisplayed(courseId: Long) {
        composeTestRule.waitForTag("$COURSE_CARD_TAG_PREFIX$courseId")
        composeTestRule.onNodeWithTag("$COURSE_CARD_TAG_PREFIX$courseId").performScrollTo().assertIsDisplayed()
    }

    fun assertCourseNotDisplayed(courseId: Long) {
        composeTestRule.onNodeWithTag("$COURSE_CARD_TAG_PREFIX$courseId").assertDoesNotExist()
    }

    fun clickCourse(courseId: Long) {
        composeTestRule.waitForTag("$COURSE_CARD_TAG_PREFIX$courseId")
        composeTestRule.onNodeWithTag("$COURSE_CARD_TAG_PREFIX$courseId").performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    fun assertGlobalAnnouncementsHeaderCount(count: Int) {
        val expected = targetContext.getString(R.string.institutionalAnnouncementsTitle, count)
        composeTestRule.onNodeWithTag(GLOBAL_ANNOUNCEMENT_HEADER_TAG).assertIsDisplayed().assert(hasText(expected))
    }

    fun assertGlobalAnnouncementDisplayed(announcementId: Long, subject: String) {
        composeTestRule.waitForTag("$GLOBAL_ANNOUNCEMENT_CARD_TAG_PREFIX$announcementId")
        bringGlobalAnnouncementIntoView(announcementId)
        composeTestRule.onNodeWithTag("$GLOBAL_ANNOUNCEMENT_CARD_TAG_PREFIX$announcementId").assertIsDisplayed()
        composeTestRule.onNodeWithTag("$GLOBAL_ANNOUNCEMENT_SUBJECT_TAG_PREFIX$announcementId", useUnmergedTree = true)
            .assertIsDisplayed()
            .assert(hasText(subject))
    }

    fun clickGlobalAnnouncement(announcementId: Long) {
        composeTestRule.waitForTag("$GLOBAL_ANNOUNCEMENT_CARD_TAG_PREFIX$announcementId")
        bringGlobalAnnouncementIntoView(announcementId)
        composeTestRule.onNodeWithTag("$GLOBAL_ANNOUNCEMENT_CARD_TAG_PREFIX$announcementId").performClick()
        composeTestRule.waitForIdle()
    }

    fun dismissGlobalAnnouncement(announcementId: Long) {
        composeTestRule.waitForTag("$GLOBAL_ANNOUNCEMENT_DISMISS_TAG_PREFIX$announcementId")
        bringGlobalAnnouncementIntoView(announcementId)
        composeTestRule.onNodeWithTag("$GLOBAL_ANNOUNCEMENT_DISMISS_TAG_PREFIX$announcementId").performClick()
        composeTestRule.waitForIdle()
    }

    private fun bringGlobalAnnouncementIntoView(announcementId: Long) {
        bringPagerItemIntoView(
            GLOBAL_ANNOUNCEMENT_PAGER_TAG,
            "$GLOBAL_ANNOUNCEMENT_SUBJECT_TAG_PREFIX$announcementId",
            useUnmergedTree = true
        )
    }

    fun assertGlobalAnnouncementGone(announcementId: Long, timeoutMillis: Long = 20000) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodesWithTag("$GLOBAL_ANNOUNCEMENT_CARD_TAG_PREFIX$announcementId").fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag("$GLOBAL_ANNOUNCEMENT_CARD_TAG_PREFIX$announcementId").assertDoesNotExist()
    }

    fun assertConferencesHeaderCount(count: Int) {
        val expected = targetContext.getString(R.string.conferencesWidgetTitle, count)
        composeTestRule.onNodeWithTag(CONFERENCES_HEADER_TAG).assertIsDisplayed().assert(hasText(expected))
    }

    fun assertConferenceDisplayed(courseId: Long, courseName: String) {
        composeTestRule.waitForTag("$CONFERENCE_CARD_TAG_PREFIX$courseId")
        bringConferenceIntoView(courseId)
        composeTestRule.onNodeWithTag("$CONFERENCE_CARD_TAG_PREFIX$courseId").assertIsDisplayed()
        composeTestRule.onNode(
            hasText(targetContext.getString(R.string.conferencesWidgetConferenceInProgress))
                .and(hasAnyAncestor(hasTestTag("$CONFERENCE_CARD_TAG_PREFIX$courseId"))),
            useUnmergedTree = true
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag("$CONFERENCE_SUBTITLE_TAG_PREFIX$courseId", useUnmergedTree = true)
            .assertIsDisplayed()
            .assert(hasText(courseName))
    }

    fun clickConference(courseId: Long) {
        composeTestRule.waitForTag("$CONFERENCE_CARD_TAG_PREFIX$courseId")
        bringConferenceIntoView(courseId)
        composeTestRule.onNodeWithTag("$CONFERENCE_CARD_TAG_PREFIX$courseId").performClick()
        composeTestRule.waitForIdle()
    }

    fun dismissConference(courseId: Long) {
        composeTestRule.waitForTag("$CONFERENCE_DISMISS_TAG_PREFIX$courseId")
        bringConferenceIntoView(courseId)
        composeTestRule.onNodeWithTag("$CONFERENCE_DISMISS_TAG_PREFIX$courseId").performClick()
        composeTestRule.waitForIdle()
    }

    private fun bringConferenceIntoView(courseId: Long) {
        bringPagerItemIntoView(CONFERENCE_PAGER_TAG, "$CONFERENCE_CARD_TAG_PREFIX$courseId")
    }

    fun assertConferenceGone(courseId: Long, timeoutMillis: Long = 20000) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodesWithTag("$CONFERENCE_CARD_TAG_PREFIX$courseId").fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag("$CONFERENCE_CARD_TAG_PREFIX$courseId").assertDoesNotExist()
    }

    fun assertSnackbar(message: String, timeoutMillis: Long = 10000) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }

    fun assertCourseSynced(courseId: Long) {
        val syncedIconTag = "$COURSE_CARD_SYNCED_ICON_TAG_PREFIX$courseId"
        waitForUnmergedTag(syncedIconTag)
        composeTestRule.onNodeWithTag(syncedIconTag, useUnmergedTree = true).assertIsDisplayed()
    }

    fun waitForCourseSynced(courseId: Long, timeoutMillis: Long = 60000) {
        waitForUnmergedTag("$COURSE_CARD_SYNCED_ICON_TAG_PREFIX$courseId", timeoutMillis)
        assertCourseSynced(courseId)
    }

    fun assertCourseNotSynced(courseId: Long) {
        composeTestRule.onNodeWithTag(
            "$COURSE_CARD_SYNCED_ICON_TAG_PREFIX$courseId",
            useUnmergedTree = true
        ).assertDoesNotExist()
    }

    fun openCourseCardMenu(courseId: Long) {
        val menuTag = "$COURSE_CARD_MENU_TAG_PREFIX$courseId"
        waitForUnmergedTag(menuTag)
        composeTestRule.onNodeWithTag(menuTag, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
    }

    private fun waitForUnmergedTag(tag: String, timeoutMillis: Long = 20000) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun openGlobalManageOfflineContent() {
        composeTestRule.waitForTag(OVERFLOW_MENU_TAG)
        composeTestRule.onNodeWithTag(OVERFLOW_MENU_TAG).performClick()
        composeTestRule.waitForTag(DASHBOARD_MANAGE_OFFLINE_CONTENT_TAG)
        composeTestRule.onNodeWithTag(DASHBOARD_MANAGE_OFFLINE_CONTENT_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    fun clickCustomizeDashboardButton() {
        composeTestRule.waitForTag(CUSTOMIZE_DASHBOARD_BUTTON_TAG)
        composeTestRule.onNodeWithTag(CUSTOMIZE_DASHBOARD_BUTTON_TAG).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    fun openOverflowMenuCustomizeDashboard() {
        composeTestRule.waitForTag(OVERFLOW_MENU_TAG)
        composeTestRule.onNodeWithTag(OVERFLOW_MENU_TAG).performClick()
        composeTestRule.waitForTag(DASHBOARD_CUSTOMIZE_DASHBOARD_TAG)
        composeTestRule.onNodeWithTag(DASHBOARD_CUSTOMIZE_DASHBOARD_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertNoInternetConnectionDialogDisplayed() {
        composeTestRule.waitForTag(NO_INTERNET_CONNECTION_DIALOG_TAG)
        composeTestRule.onNodeWithTag(NO_INTERNET_CONNECTION_DIALOG_TAG).assertIsDisplayed()
    }

    fun dismissNoInternetConnectionDialog() {
        composeTestRule.waitForTag(NO_INTERNET_CONNECTION_DIALOG_CONFIRM_BUTTON_TAG)
        composeTestRule.onNodeWithTag(NO_INTERNET_CONNECTION_DIALOG_CONFIRM_BUTTON_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    companion object {
        private const val DASHBOARD_PULL_REFRESH_INDICATOR_TAG = "dashboardPullRefreshIndicator"
        private const val WELCOME_GREETING_TAG = "welcomeGreetingText"
        private const val WELCOME_MESSAGE_TAG = "welcomeMessageText"
        private const val COURSE_INVITATION_CARD_TAG_PREFIX = "courseInvitationCard_"
        private const val COURSE_INVITATION_NAME_TAG_PREFIX = "courseInvitationName_"
        private const val COURSE_INVITATION_ACCEPT_BUTTON_TAG_PREFIX = "courseInvitationAcceptButton_"
        private const val COURSE_INVITATION_DECLINE_BUTTON_TAG_PREFIX = "courseInvitationDeclineButton_"
        private const val COURSE_INVITATION_CONFIRM_DECLINE_BUTTON_TAG = "courseInvitationConfirmDeclineButton"
        private const val COURSE_INVITATIONS_PAGER_TAG = "courseInvitationsPager"
        private const val COURSE_CARD_TAG_PREFIX = "CourseCard_"
        private const val GLOBAL_ANNOUNCEMENT_HEADER_TAG = "institutionalAnnouncementsWidget_header"
        private const val GLOBAL_ANNOUNCEMENT_PAGER_TAG = "institutionalAnnouncementsWidget_pager"
        private const val GLOBAL_ANNOUNCEMENT_CARD_TAG_PREFIX = "institutionalAnnouncementsWidget_announcementCard_"
        private const val GLOBAL_ANNOUNCEMENT_SUBJECT_TAG_PREFIX = "institutionalAnnouncementsWidget_announcementSubject_"
        private const val GLOBAL_ANNOUNCEMENT_DISMISS_TAG_PREFIX = "institutionalAnnouncementsWidget_dismissAnnouncement_"
        private const val CONFERENCES_HEADER_TAG = "conferencesWidget_header"
        private const val CONFERENCE_PAGER_TAG = "conferencesWidget_pager"
        private const val CONFERENCE_CARD_TAG_PREFIX = "conferencesWidget_card_"
        private const val CONFERENCE_SUBTITLE_TAG_PREFIX = "conferencesWidget_conferenceSubtitle_"
        private const val CONFERENCE_DISMISS_TAG_PREFIX = "conferencesWidget_dismissConference_"
        private const val MAX_PAGER_SWIPES = 3
        private const val COURSE_CARD_SYNCED_ICON_TAG_PREFIX = "courseCardSyncedIcon_"
        private const val COURSE_CARD_MENU_TAG_PREFIX = "courseCardMenu_"
        private const val OVERFLOW_MENU_TAG = "overflowMenu"
        private const val DASHBOARD_MANAGE_OFFLINE_CONTENT_TAG = "dashboardManageOfflineContent"
        private const val CUSTOMIZE_DASHBOARD_BUTTON_TAG = "customizeDashboardButton"
        private const val DASHBOARD_CUSTOMIZE_DASHBOARD_TAG = "dashboardCustomizeDashboard"
        private const val NO_INTERNET_CONNECTION_DIALOG_TAG = "noInternetConnectionDialog"
        private const val NO_INTERNET_CONNECTION_DIALOG_CONFIRM_BUTTON_TAG = "noInternetConnectionDialogConfirmButton"
    }
}
