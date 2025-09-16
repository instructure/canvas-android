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
package com.instructure.student.ui.e2e.classic.offline

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.OfflineE2E
import com.instructure.canvas.espresso.refresh
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import com.instructure.student.ui.utils.offline.OfflineTestUtils
import com.instructure.student.ui.utils.offline.OfflineTestUtils.assertNoInternetConnectionDialog
import com.instructure.student.ui.utils.offline.OfflineTestUtils.assertOfflineIndicator
import com.instructure.student.ui.utils.offline.OfflineTestUtils.dismissNoInternetConnectionDialog
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineLeftSideMenuE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LEFT_SIDE_MENU, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineLeftSideMenuUnavailableFunctionsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        assertOfflineIndicator()

        Log.d(STEP_TAG, "Open Left Side Menu by clicking on the 'hamburger/kebab icon' on the Dashboard Page.")
        dashboardPage.openLeftSideMenu()

        Log.d(ASSERTION_TAG, "Assert that the offline indicator is displayed below the user info within the header.")
        leftSideNavigationDrawerPage.assertOfflineIndicatorDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the 'Files, Bookmarks, Studio, Color Overlay, Help' menus are disabled in offline mode.")
        leftSideNavigationDrawerPage.assertOfflineDisabledMenus(0.5f)

        Log.d(ASSERTION_TAG, "Assert that the 'Settings, Show Grades, Change User, Log Out' menus are enabled in offline mode because they are supported.")
        leftSideNavigationDrawerPage.assertOfflineEnabledMenus(1.0f)

        Log.d(STEP_TAG, "Click on 'Files' menu.")
        leftSideNavigationDrawerPage.clickFilesMenu()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is popping-up.")
        assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Click on 'Bookmarks' menu.")
        leftSideNavigationDrawerPage.clickBookmarksMenu()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is popping-up.")
        assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Click on 'Studio' menu.")
        leftSideNavigationDrawerPage.clickStudioMenu()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is popping-up.")
        assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Click on 'Help' menu.")
        leftSideNavigationDrawerPage.clickHelpMenu()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is popping-up.")
        assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        dismissNoInternetConnectionDialog()
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device via ADB, so it will come back online.")
        turnOnConnectionViaADB()
    }

}