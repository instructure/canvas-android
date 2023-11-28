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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.CourseBrowserPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class OfflineCourseBrowserE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testOfflineCourseBrowserPageUnavailableE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 1)
        val student = data.studentsList[0]
        val course1 = data.coursesList[0]
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.openGlobalManageOfflineContentPage()

        Log.d(STEP_TAG, "Select the entire '${course1.name}' course for sync. Click on the 'Sync' button.")
        Log.d(STEP_TAG, "Expand '${course1.name}' course. Select only the 'Announcements' of the '${course1.name}' course. Click on the 'Sync' button and confirm the sync process.")
        manageOfflineContentPage.expandCollapseItem(course1.name)
        manageOfflineContentPage.changeItemSelectionState("Announcements")
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

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course1.name}' course and open 'Announcements' menu.")
        sleep(5000) //Need to wait a bit here because of a UI glitch that when network state change, the dashboard page 'pops' a bit and it can confuse the automation script.
        dashboardPage.selectCourse(course1)

        Log.d(STEP_TAG, "Assert that only the 'Announcements' tab is enabled because it is the only one which has been synced, and assert that all the other, previously synced tabs are disabled, because they weren't synced now.")
        var enabledTabs = arrayOf("Announcements")
        var disabledTabs = arrayOf("Discussions", "Grades", "People", "Syllabus", "BigBlueButton")
        assertTabsEnabled(courseBrowserPage, enabledTabs)
        assertTabsDisabled(courseBrowserPage, disabledTabs)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.Turn back on the Wi-Fi and Mobile Data on the device, and wait for it to come online.")
        Espresso.pressBack()
        turnOnConnectionViaADB()
        dashboardPage.waitForNetworkComeBack()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.openGlobalManageOfflineContentPage()

        Log.d(STEP_TAG, "Deselect the entire '${course1.name}' course for sync.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)
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

        Log.d(STEP_TAG, "Select '${course1.name}' course and open 'Announcements' menu.")
        sleep(10000) //Need to wait a bit here because of a UI glitch that when network state change, the dashboard page 'pops' a bit and it can confuse the automation script.
        device.waitForIdle()
        device.waitForWindowUpdate(null, 10000)
        dashboardPage.selectCourse(course1)

        Log.d(STEP_TAG, "Assert that the 'Google Drive' and 'Collaborations' tabs are disabled because they aren't supported in offline mode, but the rest of the tabs are enabled because the whole course has been synced.")
        enabledTabs = arrayOf("Announcements", "Discussions", "Grades", "People", "Syllabus", "BigBlueButton")
        disabledTabs = arrayOf("Google Drive", "Collaborations")
        assertTabsEnabled(courseBrowserPage, enabledTabs)
        assertTabsDisabled(courseBrowserPage, disabledTabs)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

    private fun assertTabsEnabled(courseBrowserPage: CourseBrowserPage, tabs: Array<String>) {
        tabs.forEach { tab ->
            courseBrowserPage.assertTabEnabled(tab)
        }
    }

    private fun assertTabsDisabled(courseBrowserPage: CourseBrowserPage, tabs: Array<String>) {
        tabs.forEach { tab ->
            courseBrowserPage.assertTabDisabled(tab)
        }
    }
}