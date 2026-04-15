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
package com.instructure.student.ui.e2e.classic.offline

import android.os.SystemClock
import android.util.Log
import androidx.media3.ui.R
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.OfflineE2E
import com.instructure.canvas.espresso.utils.refresh
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.espresso.getVideoPosition
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import com.instructure.student.ui.utils.offline.OfflineTestUtils
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class OfflineAnnouncementsE2ETest : StudentComposeTest() {

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
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

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

    @OfflineE2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ANNOUNCEMENTS, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineAnnouncementWithVideoAttachmentE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Read video file from assets.")
        val videoFileName = "test_video.mp4"
        val context = InstrumentationRegistry.getInstrumentation().context
        val videoBytes = context.assets.open(videoFileName).use { it.readBytes() }

        Log.d(PREPARATION_TAG, "Seed an announcement with video attachment for '${course.name}' course.")
        val announcementTitle = "Announcement with Video Attachment"
        val announcementWithVideo = DiscussionTopicsApi.createAnnouncement(courseId = course.id, token = teacher.token, announcementTitle = announcementTitle, fileBytes = videoBytes, fileName = videoFileName)

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
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG, "Select '${course.name}' course and navigate to Announcements page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAnnouncements()

        Log.d(ASSERTION_TAG, "Assert that '${announcementWithVideo.title}' announcement is displayed.")
        announcementListPage.assertTopicDisplayed(announcementWithVideo.title)

        Log.d(STEP_TAG, "Select '${announcementWithVideo.title}' announcement.")
        announcementListPage.selectTopic(announcementWithVideo.title)

        Log.d(ASSERTION_TAG, "Assert that we are on the Discussion Details Page with the correct title.")
        nativeDiscussionDetailsPage.assertTitleText(announcementWithVideo.title)

        Log.d(ASSERTION_TAG, "Assert that the attachment icon is displayed on the announcement details page.")
        nativeDiscussionDetailsPage.assertMainAttachmentDisplayed()

        Log.d(STEP_TAG, "Click on the attachment icon to view the video attachment.")
        nativeDiscussionDetailsPage.clickAttachmentIcon()

        Log.d(ASSERTION_TAG, "Wait for the video to start and assert that the video player controls are displayed.")
        videoPlayerPage.waitForVideoToStart(device)
        videoPlayerPage.assertPlayPauseButtonDisplayed()

        Log.d(STEP_TAG, "Click play/pause button to pause the video.")
        videoPlayerPage.clickPlayPauseButton()

        Log.d(STEP_TAG, "Get the current video position.")
        val firstVideoPositionText = getVideoPosition(R.id.exo_position)
        Log.d(ASSERTION_TAG, "First video position: $firstVideoPositionText")

        Log.d(STEP_TAG, "Click play/pause button to resume video playback, wait for video to play for 2 seconds then click play/pause button to pause again.")
        videoPlayerPage.clickPlayPauseButton()
        SystemClock.sleep(2000)
        videoPlayerPage.clickPlayPauseButton()

        Log.d(STEP_TAG, "Get the video position again.")
        val secondVideoPositionText = getVideoPosition(R.id.exo_position)
        Log.d(ASSERTION_TAG, "Second video position: $secondVideoPositionText")

        Log.d(ASSERTION_TAG, "Assert that the video position has changed, confirming video is playing.")
        assert(firstVideoPositionText != secondVideoPositionText) {
            "Video position did not change. First: $firstVideoPositionText, Second: $secondVideoPositionText"
        }

        Log.d(STEP_TAG, "Navigate back to the announcement details page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that we are back on the announcement details page.")
        nativeDiscussionDetailsPage.assertTitleText(announcementWithVideo.title)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }
}