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
import com.instructure.pandautils.R
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineSyncSettingsE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.OFFLINE_CONTENT, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun offlineSyncSettingsE2ETest() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2, announcements = 1)
        val student = data.studentsList[0]

        Log.d(STEP_TAG,"Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Settings page from the Left Side menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG, "Assert that the Offline Sync Settings related information is displayed properly on the Settings Page ('Daily' is the default status).")
        settingsPage.assertOfflineContentDisplayed()
        settingsPage.assertOfflineSyncSettingsStatus("Daily")

        Log.d(STEP_TAG, "Open Offline Sync Settings page and wait for it to be loaded.")
        settingsPage.clickOnSyncSettingsItem()
        offlineSyncSettingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that further settings, such as the toolbar title is displayed and correct, and both the Auto Content Sync and Wi-Fi Only Sync toggles are displayed and checked by default.")
        offlineSyncSettingsPage.assertFurtherSettingsIsDisplayed()
        offlineSyncSettingsPage.assertSyncSettingsToolbarTitle()
        offlineSyncSettingsPage.assertAutoSyncSwitchIsChecked()
        offlineSyncSettingsPage.assertWifiOnlySwitchIsChecked()

        Log.d(STEP_TAG, "Assert that all the descriptions of how these settings are working are displayed.")
        offlineSyncSettingsPage.assertSyncSettingsPageDescriptions()

        Log.d(STEP_TAG, "Assert that the sync frequency label is 'Daily', because that is the default setting.")
        offlineSyncSettingsPage.assertSyncFrequencyLabelText(R.string.daily)

        Log.d(STEP_TAG, "Switch off the 'Auto Content Sync' toggle, and assert if that the further settings below will disappear.")
        offlineSyncSettingsPage.clickAutoSyncSwitch()

        Log.d(STEP_TAG, "Assert that further settings, such as the toolbar title, Auto Content Sync and Wi-Fi Only Sync toggles are NOT displayed.")
        offlineSyncSettingsPage.assertFurtherSettingsNotDisplayed()

        Log.d(STEP_TAG, "Switch back the 'Auto Content Sync' toggle, and assert if that the further settings below will be displayed again.")
        offlineSyncSettingsPage.clickAutoSyncSwitch()

        Log.d(STEP_TAG, "Assert that further settings, such as the toolbar title is displayed and correct, and both the Auto Content Sync and Wi-Fi Only Sync toggles are displayed again.")
        offlineSyncSettingsPage.assertFurtherSettingsIsDisplayed()

        Log.d(STEP_TAG, "Switch off the 'Sync Content Wi-Fi Only' toggle and assert that the confirmation dialog (with the proper texts) is displayed.")
        offlineSyncSettingsPage.clickWifiOnlySwitch()
        offlineSyncSettingsPage.assertTurnOffWifiOnlyDialogTexts()

        Log.d(STEP_TAG, "Click on the 'TURN OFF' button on the dialog to really turn off the 'Sync Content Wi-Fi Only' switch.")
        offlineSyncSettingsPage.clickTurnOff()

        Log.d(STEP_TAG, "Assert that the 'Sync Content Wi-Fi Only' switch is not checked any more.")
        offlineSyncSettingsPage.assertWifiOnlySwitchIsNotChecked()

        Log.d(STEP_TAG, "Open the Sync Frequency Settings dialog and select 'Weekly' option.")
        offlineSyncSettingsPage.openSyncFrequencySettingsDialog()
        offlineSyncSettingsPage.clickSyncFrequencyDialogOption(R.string.weekly)

        Log.d(STEP_TAG, "Assert that the sync frequency label became 'Weekly' (without any manual refresh).")
        offlineSyncSettingsPage.assertSyncFrequencyLabelText(R.string.weekly)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page and logout.")
        ViewUtils.pressBackButton(2)
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG, "Enter domain: '${student.domain}'.")
        loginFindSchoolPage.enterDomain(student.domain)

        Log.d(STEP_TAG, "Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        loginSignInPage.loginAs(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Settings page from the Left Side menu.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG, "Assert that the Offline Sync Settings frequency text is 'Weekly' (because we set it previously).")
        settingsPage.assertOfflineSyncSettingsStatus("Weekly")

        Log.d(STEP_TAG, "Open Offline Sync Settings page and wait for it to be loaded.")
        settingsPage.clickOnSyncSettingsItem()
        offlineSyncSettingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the Offline Sync Settings frequency text is 'Weekly' (because we set it previously) and the 'Sync Content Wi-Fi Only' switch is switched off.")
        offlineSyncSettingsPage.assertSyncFrequencyLabelText(R.string.weekly)
        offlineSyncSettingsPage.assertWifiOnlySwitchIsNotChecked()
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device via ADB, so it will come back online.")
        turnOnConnectionViaADB()
    }

}