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
package com.instructure.teacher.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.utils.checkToastText
import com.instructure.pandautils.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test


@HiltAndroidTest
class AnnouncementsE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    //Because of naming conventions, we are using 'announcementDetailsPage' naming in this class to make the code more readable and straightforward.
    private val announcementDetailsPage = discussionDetailsPage

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.E2E)
    fun testAnnouncementsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 2)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val announcement = data.announcementsList[0]
        val announcement2 = data.announcementsList[1]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert '${course.name}' course is displayed.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to it's Announcements Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openAnnouncementsTab()

        Log.d(ASSERTION_TAG, "Assert that the '${announcement.title}' announcement has been displayed.")
        announcementsListPage.assertHasAnnouncement(announcement)

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '${announcement2.title}', one of the announcements' name to the search input field.")
        announcementsListPage.searchable.clickOnSearchButton()
        announcementsListPage.searchable.typeToSearchBar(announcement2.title)

        Log.d(ASSERTION_TAG, "Assert that only 1 announcement matches for the search text, and it is '${announcement2.title}'.")
        announcementsListPage.assertSearchResultCount(1)
        announcementsListPage.assertHasAnnouncement(announcement2)

        Log.d(STEP_TAG, "Click on 'Reset' search (cross) icon.")
        announcementsListPage.searchable.clickOnClearSearchButton()

        Log.d(ASSERTION_TAG, "Assert that all the announcements are displayed (2).")
        announcementsListPage.assertSearchResultCount(2)

        Log.d(STEP_TAG, "Open the '${announcement.title}' announcement.")
        announcementsListPage.clickAnnouncement(announcement)

        Log.d(ASSERTION_TAG, "Assert that it's title is displayed on the (web view) page's toolbar title and the 'Reply' button is also displayed.")
        announcementDetailsPage.assertToolbarDiscussionTitle(announcement.title)
        announcementDetailsPage.waitForReplyButtonDisplayed()
        announcementDetailsPage.assertReplyButtonDisplayed()

        Log.d(STEP_TAG, "Click on the more menu of the announcement.")
        announcementDetailsPage.clickOnDiscussionMoreMenu()

        Log.d(ASSERTION_TAG, "Assert if the more menu items are all displayed.")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Mark All as Read")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Mark All as Unread")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Edit")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Delete")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Send To...")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Copy To...")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Share to Commons")

        Log.d(STEP_TAG, "Click on 'Mark All as Unread' menu item to mark the main announcement topic as unread.")
        announcementDetailsPage.clickOnMarkAllAsUnreadMenuButton()

        Log.d(STEP_TAG, "Navigate back to the Announcements List Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the unread status indicator is displayed on the '${announcement.title}' announcement's row because the main topic has been marked as unread.")
        announcementsListPage.assertUnreadIndicatorVisibility(announcement.title, true)

        Log.d(STEP_TAG, "Open the '${announcement.title}' announcement again and click on the more menu of the announcement.")
        announcementsListPage.clickAnnouncement(announcement)
        announcementDetailsPage.waitForReplyButtonDisplayed()
        announcementDetailsPage.clickOnDiscussionMoreMenu()

        Log.d(ASSERTION_TAG, "Assert that the 'Mark All as Read' menu item is displayed.")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Mark All as Read")

        Log.d(STEP_TAG, "Click on 'Mark All as Read' to mark the announcement's main topic as read.")
        announcementDetailsPage.clickOnMarkAllAsReadMenuButton()

        Log.d(STEP_TAG, "Navigate back to the Announcements List Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the unread status indicator is no longer displayed on the '${announcement.title}' announcement's row after marking it as read.")
        announcementsListPage.assertUnreadIndicatorVisibility(announcement.title, false)

        Log.d(STEP_TAG, "Open the '${announcement.title}' announcement again and click on the more menu of the announcement.")
        announcementsListPage.clickAnnouncement(announcement)
        announcementDetailsPage.waitForReplyButtonDisplayed()
        announcementDetailsPage.clickOnDiscussionMoreMenu()

        Log.d(ASSERTION_TAG, "Assert that the 'Delete' menu item is displayed.")
        announcementDetailsPage.assertMoreMenuButtonDisplayed("Delete")

        Log.d(STEP_TAG, "Click on 'Delete' and confirm the deletion in the (WebView) confirmation dialog.")
        announcementDetailsPage.clickOnDeleteMenuButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the 'Announcement deleted.' confirmation toast is displayed.")
        checkToastText(R.string.utils_announcementDeleted, activityRule.activity)

        Log.d(ASSERTION_TAG, "Assert that we returned to the native Announcement list, the deleted '${announcement.title}' announcement is not displayed any more, but '${announcement2.title}' still is.")
        announcementsListPage.assertHasAnnouncement(announcement2)
        announcementsListPage.assertAnnouncementNotDisplayed(announcement.title)
    }
}