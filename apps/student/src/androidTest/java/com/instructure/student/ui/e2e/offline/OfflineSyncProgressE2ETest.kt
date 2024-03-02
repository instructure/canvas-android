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
import androidx.test.espresso.Espresso
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineSyncProgressE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYNC_PROGRESS, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineGlobalCourseSyncProgressE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2, announcements = 1)
        val student = data.studentsList[0]
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]
        val testAnnouncement = data.announcementsList[0]

        Log.d(STEP_TAG,"Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.openGlobalManageOfflineContentPage()

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Select the entire '${course1.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(STEP_TAG, "Wait for the Dashboard to be rendered.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Click on the Dashboard notification to open the Sync Progress Page.")
        dashboardPage.clickOnSyncProgressNotification()

        Log.d(STEP_TAG, "Assert that the Sync Progress has been successful (so to have the success title and the course success indicator).")
        syncProgressPage.assertDownloadProgressSuccessDetails()
        syncProgressPage.assertCourseSyncedSuccessfully(course1.name)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page and wait for it to be rendered.")
        Espresso.pressBack()
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course1.name)
        dashboardPage.assertCourseOfflineSyncIconGone(course2.name)

        Log.d(STEP_TAG, "Select '${course1.name}' course and open 'Announcements' menu.")
        dashboardPage.selectCourse(course1)
        courseBrowserPage.selectAnnouncements()

        Log.d(STEP_TAG,"Assert that the '${testAnnouncement.title}' titled announcement is displayed, so the user is able to see it in offline mode because it was synced.")
        announcementListPage.assertTopicDisplayed(testAnnouncement.title)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device via ADB, so it will come back online.")
        turnOnConnectionViaADB()
    }

}