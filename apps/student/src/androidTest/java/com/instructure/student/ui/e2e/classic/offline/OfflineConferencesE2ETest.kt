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

import android.util.Log
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.OfflineE2E
import com.instructure.dataseeding.api.ConferencesApi
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import com.instructure.student.ui.utils.offline.OfflineTestUtils.assertOfflineIndicator
import com.instructure.student.ui.utils.offline.OfflineTestUtils.waitForNetworkToGoOffline
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineConferencesE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CONFERENCES, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineConferencesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val testConferenceTitle = "E2E test conference"
        val testConferenceDescription = "Nightly E2E Test conference description"
        Log.d(PREPARATION_TAG, "Create a conference with '$testConferenceTitle' title and '$testConferenceDescription' description.")
        ConferencesApi.createCourseConference(course.id, teacher.token, testConferenceTitle, testConferenceDescription, recipientUserIds = listOf(student.id))

        val testConferenceTitle2 = "E2E test conference 2"
        val testConferenceDescription2 = "Nightly E2E Test conference description 2"
        Log.d(PREPARATION_TAG, "Create a conference with '$testConferenceTitle2' title and '$testConferenceDescription2' description.")
        ConferencesApi.createCourseConference(course.id, teacher.token, testConferenceTitle2, testConferenceDescription2, longRunning = true, duration = 120, recipientUserIds = listOf(student.id))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Expand the course. Select the 'BigBlueButton' (Conferences) of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.changeItemSelectionState("BigBlueButton")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course and click on 'BigBlueButton' (Conferences) tab to navigate to the Conferences List Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectConferences()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Page List Page.")
        assertOfflineIndicator()

        Log.d(ASSERTION_TAG, "Assert that '$testConferenceTitle' conference is displayed on the Conference List Page with the corresponding status and description.")
        conferenceListPage.assertConferenceDisplayed(testConferenceTitle)
        conferenceListPage.assertConferenceStatus(testConferenceTitle,"Not Started")
        conferenceListPage.assertConferenceDescription(testConferenceTitle, testConferenceDescription)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Conferences' and there is the '${course.name}' course's name displayed as a subtitle.")
        conferenceListPage.assertConferencesToolbarText(course.name)

        Log.d(ASSERTION_TAG, "Assert that the 'New Conferences' text is displayed as a conference group type on the Conferences List Page.")
        conferenceListPage.assertNewConferencesDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the 'Open Externally' icon (button) is displayed on the top-right corner of the Conferences List Page.")
        conferenceListPage.assertOpenExternallyButtonDisplayed()

        Log.d(ASSERTION_TAG, "Assert that '$testConferenceTitle2' conference is displayed on the Conference List Page with the corresponding status and description.")
        conferenceListPage.assertConferenceDisplayed(testConferenceTitle2)
        conferenceListPage.assertConferenceStatus(testConferenceTitle2,"Not Started")
        conferenceListPage.assertConferenceDescription(testConferenceTitle2, testConferenceDescription2)

        Log.d(STEP_TAG, "Open '$testConferenceTitle' conference's detailer page.")
        conferenceListPage.openConferenceDetails(testConferenceTitle)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Conference Details' and there is the '${course.name}' course's name displayed as a subtitle.")
        conferenceDetailsPage.assertConferenceDetailsToolbarText(course.name)

        Log.d(ASSERTION_TAG, "Assert that the proper conference title '$testConferenceTitle', status ('Not Started') and description, '$testConferenceDescription' are displayed.")
        conferenceDetailsPage.assertConferenceTitleDisplayed(testConferenceTitle)
        conferenceDetailsPage.assertConferenceStatus("Not Started")
        conferenceDetailsPage.assertDescription(testConferenceDescription)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

}