/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class DashboardE2EOfflineTest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testOfflineDashboardE2E() {
        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2, announcements = 1)
        val student = data.studentsList[0]
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]
        val testAnnouncement = data.announcementsList[0]

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.openGlobalManageOfflineContentPage()

        Log.d(STEP_TAG, "Select the entire '${course1.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.selectEntireCourseForSync(course1.name)
        manageOfflineContentPage.clickOnSyncButton()

        Log.d(STEP_TAG, "Wait for the 'Download Started' dashboard notification to be displayed, and the to disappear.")
        manageOfflineContentPage.waitForSyncProgressDownloadStartedNotification()
        manageOfflineContentPage.waitForSyncProgressDownloadStartedNotificationToDisappear()

        Log.d(STEP_TAG, "Wait for the 'Syncing Offline Content' dashboard notification to be displayed, and the to disappear. (It should be displayed after the 'Download Started' notification immediately.)")
        manageOfflineContentPage.waitForSyncProgressStartingNotification()
        manageOfflineContentPage.waitForSyncProgressStartingNotificationToDisappear()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        OfflineTestUtils.turnOffConnectionViaADB()

        Log.d(STEP_TAG, "Press the 'Home' button on the device.")
        device.pressHome()
        Log.d(STEP_TAG, "Click 'Recent Apps' device button and bring Canvas Student into the foreground again." +
                "Assert that the Dashboard Page is displayed.")
        device.pressRecentApps()
        device.findObject(UiSelector().descriptionContains("Canvas")).click()

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered.")
        dashboardPage.waitForRender()

        //TODO: https://instructure.atlassian.net/browse/MBL-17103?atlOrigin=eyJpIjoiNDAzMzc0ZWRjOTQxNGMwYTk0NzMzYTc0OTYzYjkyMGMiLCJwIjoiaiJ9
        //After the above ticket has been developed, this test should be refactored because the offline sync icon will be visible without relogging.
        Log.d(STEP_TAG, "Log out with ${student.name} student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG, "Assert that the offline sync icon only displayed on the synced course's cours card.")
        dashboardPage.assertCourseOfflineSyncIcon(course1.name, ViewMatchers.Visibility.VISIBLE)
        dashboardPage.assertCourseOfflineSyncIcon(course2.name, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${course1.name}' course and open 'Announcements' menu.")
        dashboardPage.selectCourse(course1)
        courseBrowserPage.selectAnnouncements()

        Log.d(STEP_TAG,"Assert that the '${testAnnouncement.title}' titled announcement is displayed, so the user is able to see it in offline mode because it was synced.")
        announcementListPage.assertTopicDisplayed(testAnnouncement.title)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        OfflineTestUtils.turnOnConnectionViaADB()
    }

}