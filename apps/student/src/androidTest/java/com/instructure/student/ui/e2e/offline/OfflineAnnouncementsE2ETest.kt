/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e.offline

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class OfflineAnnouncementsE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineAnnouncementsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val announcement = data.announcementsList[0]

        val lockedAnnouncement = DiscussionTopicsApi.createAnnouncement(course.id, teacher.token, locked = true)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Expand '${course.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course.name)

        Log.d(STEP_TAG, "Select the 'Announcements' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState("Announcements")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        uiDevice.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(uiDevice)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG, "Select '${course.name}' course and open 'Announcements' menu.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAnnouncements()

        Log.d(ASSERTION_TAG, "Assert that '${announcement.title}' announcement is displayed.")
        announcementListPage.assertTopicDisplayed(announcement.title)

        Log.d(ASSERTION_TAG, "Assert that '${lockedAnnouncement.title}' announcement is really locked so that the 'locked' icon is displayed.")
        announcementListPage.assertAnnouncementLocked(lockedAnnouncement.title)

        Log.d(STEP_TAG, "Select '${lockedAnnouncement.title}' announcement.")
        announcementListPage.selectTopic(lockedAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert if we are landing on the Discussion Details Page.")
        nativeDiscussionDetailsPage.assertTitleText(lockedAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert that the 'Reply' button is not available on a locked announcement.")
        nativeDiscussionDetailsPage.assertReplyButtonNotDisplayed()

        Log.d(STEP_TAG, "Navigate back to Announcement List page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Select '${announcement.title}' announcement.")
        announcementListPage.selectTopic(announcement.title)

        Log.d(ASSERTION_TAG, "Assert if we are landing on the Discussion Details Page.")
        nativeDiscussionDetailsPage.assertTitleText(announcement.title)

        Log.d(STEP_TAG, "Click on the 'Reply' button.")
        nativeDiscussionDetailsPage.clickReply()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog has displayed.")
        OfflineTestUtils.assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        OfflineTestUtils.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Navigate back to Announcement List page. Click on Search button and type '${announcement.title}' to the search input field.")
        Espresso.pressBack()
        announcementListPage.searchable.clickOnSearchButton()
        announcementListPage.searchable.typeToSearchBar(announcement.title)

        Log.d(ASSERTION_TAG, "Assert that only the matching announcement is displayed on the Discussion List Page.")
        announcementListPage.pullToUpdate()
        announcementListPage.assertTopicDisplayed(announcement.title)
        announcementListPage.assertTopicNotDisplayed(lockedAnnouncement.title)

        Log.d(STEP_TAG, "Clear search input field value.")
        announcementListPage.searchable.clickOnClearSearchButton()
        announcementListPage.waitForDiscussionTopicToDisplay(lockedAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert if all the announcements are displayed again on the Discussion List Page.")
        announcementListPage.assertTopicDisplayed(announcement.title)

        Log.d(STEP_TAG, "Type the '${announcement.title}' announcement's title as search value to the search input field.")
        announcementListPage.searchable.typeToSearchBar(announcement.title)

        sleep(3000) //We need this wait here to let make sure the search process has finished.

        Log.d(ASSERTION_TAG, "Assert the the '${announcement.title}' announcement, but only that displayed as result.")
        announcementListPage.assertTopicDisplayed(announcement.title)
        announcementListPage.assertTopicNotDisplayed(lockedAnnouncement.title)

        Log.d(STEP_TAG, "Clear search input field value.")
        announcementListPage.searchable.clickOnClearSearchButton()
        announcementListPage.waitForDiscussionTopicToDisplay(lockedAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert if both the announcements are displayed again on the Announcement List Page.")
        announcementListPage.assertTopicDisplayed(announcement.title)
      }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }
}