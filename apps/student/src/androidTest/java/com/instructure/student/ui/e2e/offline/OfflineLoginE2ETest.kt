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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils.assertNoInternetConnectionDialog
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils.dismissNoInternetConnectionDialog
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils.waitForNetworkToGoOffline
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class OfflineLoginE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.LOGIN, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineChangeUserE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        Log.d(STEP_TAG, "Login with user: '${student1.name}', login id: '${student1.loginId}'.")
        loginWithUser(student1)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the Offline indicator is not displayed because we are in online mode yet.")
        dashboardPage.assertOfflineIndicatorNotDisplayed()

        Log.d(STEP_TAG, "Click on 'Change User' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(STEP_TAG, "Login with user: '${student2.name}', login id: '${student2.loginId}'.")
        loginWithUser(student2, true)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that the Offline indicator is not displayed because we are in online mode yet.")
        dashboardPage.assertOfflineIndicatorNotDisplayed()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Click on 'Change User' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(ASSERTION_TAG, "Assert that the previously logins has been displayed. Assert that '${student1.name}' and '${student2.name}' students are displayed within the previous login section.")
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.assertPreviousLoginUserDisplayed(student1.name)
        loginLandingPage.assertPreviousLoginUserDisplayed(student2.name)

        Log.d(STEP_TAG, "Try to click on the last saved school's button.")
        loginLandingPage.clickOnLastSavedSchoolButton()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is popping-up.")
        assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Try to click on the 'Find another school' button.")
        loginLandingPage.clickFindAnotherSchoolButton()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is popping-up.")
        assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Login with the previous user, '${student1.name}', with one click, by clicking on the user's name on the bottom.")
        loginLandingPage.loginWithPreviousUser(student1)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered.")
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the offline indicator is displayed to ensure we are in offline mode, and change user function is supported.")
        dashboardPage.assertOfflineIndicatorDisplayed()

        Log.d(STEP_TAG, "Click on 'Change User' button on the left-side menu.")
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        Log.d(STEP_TAG, "Login with the previous user, '${student2.name}', with one click, by clicking on the user's name on the bottom.")
        loginLandingPage.loginWithPreviousUser(student2)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered.")
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the offline indicator is displayed to ensure we are in offline mode, and change user function is supported.")
        dashboardPage.assertOfflineIndicatorDisplayed()
    }

    private fun loginWithUser(user: CanvasUserApiModel, lastSchoolSaved: Boolean = false) {

        sleep(5100) //Need to wait > 5 seconds before each login attempt because of new 'too many attempts' login policy on web.

        if(lastSchoolSaved) {
            Log.d(STEP_TAG, "Click 'Find Another School' button.")
            loginLandingPage.clickFindAnotherSchoolButton()
        }
        else {
            Log.d(STEP_TAG, "Click 'Find My School' button.")
            loginLandingPage.clickFindMySchoolButton()
        }

        Log.d(STEP_TAG, "Enter domain: '${user.domain}'.")
        loginFindSchoolPage.enterDomain(user.domain)

        Log.d(STEP_TAG, "Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(user)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

}