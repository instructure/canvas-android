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
package com.instructure.student.ui.e2e.classic.offline

import android.util.Log
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.RemoteConfigOverrides
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.OfflineE2E
import com.instructure.canvas.espresso.utils.pressBackButton
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.dataseeding.api.FeatureFlagsApi
import com.instructure.pandautils.utils.FEATURE_FLAG_WIDGET_DASHBOARD
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLoginWithoutDashboardWait
import com.instructure.student.ui.utils.offline.OfflineTestUtils
import com.instructure.student.ui.utils.offline.OfflineTestUtils.waitForNetworkToGoOffline
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class OfflineNewDashboardE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Before
    fun setUp() {
        Log.d(PREPARATION_TAG, "Enable the '$FEATURE_FLAG_WIDGET_DASHBOARD' Canvas feature flag on the account.")
        FeatureFlagsApi.setAccountFeatureFlag(feature = FEATURE_FLAG_WIDGET_DASHBOARD, state = "on")

        Log.d(PREPARATION_TAG, "Override the DASHBOARD_REDESIGN remote config flag (default 'false') to 'true'.")
        RemoteConfigOverrides.set(RemoteConfigParam.DASHBOARD_REDESIGN, true)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()

        // Re-enabling the connection via ADB doesn't mean the network is immediately usable
        // (DNS resolution needs a moment to come back), so wait before making the network
        // call below, otherwise it fails with an UnknownHostException.
        Thread.sleep(10000)

        try {
            FeatureFlagsApi.setAccountFeatureFlag(feature = FEATURE_FLAG_WIDGET_DASHBOARD, state = "off")
        } finally {
            RemoteConfigOverrides.clear()
        }
    }

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NEW_DASHBOARD, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testNewDashboardOfflineE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2, announcements = 1)
        val student = data.studentsList[0]
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]
        val testAnnouncement = data.announcementsList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLoginWithoutDashboardWait(student)

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the overflow menu of the redesigned Dashboard Page.")
        newDashboardPage.openGlobalManageOfflineContent()

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Select the entire '${course1.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the redesigned (Compose) dashboard is displayed again.")
        newDashboardPage.assertDashboardDisplayed()

        Log.d(STEP_TAG, "Wait until the offline sync icon appears on the synced course's course card.")
        newDashboardPage.waitForCourseSynced(course1.id)

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the redesigned Dashboard Page to be rendered and refresh it.")
        newDashboardPage.assertDashboardDisplayed()
        newDashboardPage.refreshDashboard()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the redesigned Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        newDashboardPage.assertCourseSynced(course1.id)
        newDashboardPage.assertCourseNotSynced(course2.id)

        Log.d(STEP_TAG, "Select '${course1.name}' course and open 'Announcements' menu.")
        newDashboardPage.clickCourse(course1.id)
        courseBrowserPage.selectAnnouncements()

        Log.d(ASSERTION_TAG, "Assert that the '${testAnnouncement.title}' titled announcement is displayed, so the user is able to see it in offline mode because it was synced.")
        announcementListPage.assertTopicDisplayed(testAnnouncement.title)

        Log.d(STEP_TAG, "Navigate back to the redesigned dashboard.")
        pressBackButton(2)
        newDashboardPage.assertDashboardDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the bottom menus (except Dashboard) are disabled and unavailable in offline mode.")
        dashboardPage.assertBottomMenusAreDisabled()

        Log.d(STEP_TAG, "Try to tap the 'Customize Dashboard' button on the redesigned Dashboard Page.")
        newDashboardPage.clickCustomizeDashboardButton()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is displayed.")
        newDashboardPage.assertNoInternetConnectionDialogDisplayed()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        newDashboardPage.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Try to tap 'Customize Dashboard' via the overflow menu of the redesigned Dashboard Page.")
        newDashboardPage.openOverflowMenuCustomizeDashboard()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is displayed.")
        newDashboardPage.assertNoInternetConnectionDialogDisplayed()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        newDashboardPage.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Try to open the '${course1.name}' course's offline content menu on the redesigned Dashboard Page.")
        newDashboardPage.openCourseCardMenu(course1.id)

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is displayed.")
        newDashboardPage.assertNoInternetConnectionDialogDisplayed()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        newDashboardPage.dismissNoInternetConnectionDialog()
    }
}
