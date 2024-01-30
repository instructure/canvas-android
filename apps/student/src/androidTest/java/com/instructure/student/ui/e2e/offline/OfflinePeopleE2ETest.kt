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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
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
class OfflinePeopleE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.PEOPLE, TestCategory.E2E)
    fun testOfflinePeopleE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Get the device to be able to perform app-independent actions on it.")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        manageOfflineContentPage.expandCollapseItem(course.name)
        Log.d(STEP_TAG, "Select the entire '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState("People")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(STEP_TAG, "Wait for the 'Download Started' dashboard notification to be displayed, and the to disappear.")
        dashboardPage.waitForRender()
        dashboardPage.waitForSyncProgressDownloadStartedNotification()
        dashboardPage.waitForSyncProgressDownloadStartedNotificationToDisappear()

        Log.d(STEP_TAG, "Wait for the 'Syncing Offline Content' dashboard notification to be displayed, and the to disappear. (It should be displayed after the 'Download Started' notification immediately.)")
        dashboardPage.waitForSyncProgressStartingNotification()
        dashboardPage.waitForSyncProgressStartingNotificationToDisappear()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        Thread.sleep(10000) //Need to wait a bit here because of a UI glitch that when network state change, the dashboard page 'pops' a bit and it can confuse the automation script.
        device.waitForIdle()
        device.waitForWindowUpdate(null, 10000)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)

        Log.d(STEP_TAG, "Select '${course.name}' course and open 'People' menu.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectPeople()

        Log.d(STEP_TAG, "Select '${teacher.name}' teacher.")
        peopleListPage.selectPerson(teacher)

        Log.d(STEP_TAG, "Assert that the compose message icon is not displayed (GONE) because it is not supported in offline mode.")
        personDetailsPage.assertComposeMessageIcon(ViewMatchers.Visibility.GONE)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

}