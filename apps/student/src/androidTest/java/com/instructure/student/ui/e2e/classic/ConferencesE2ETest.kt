/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.utils.refresh
import com.instructure.dataseeding.api.ConferencesApi
import com.instructure.student.ui.pages.classic.ConferenceWebPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ConferencesE2ETest: StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CONFERENCES, TestCategory.E2E)
    fun testConferencesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to '${course.name}' course's Conferences Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectConferences()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed since we did not make any conference yet.")
        conferenceListPage.assertEmptyView()

        val testConferenceTitle = "E2E test conference"
        val testConferenceDescription = "Nightly E2E Test conference description"
        Log.d(PREPARATION_TAG, "Create a conference with '$testConferenceTitle' title and '$testConferenceDescription' description.")
        ConferencesApi.createCourseConference(course.id, teacher.token, testConferenceTitle, testConferenceDescription, recipientUserIds = listOf(student.id))

        val testConferenceTitle2 = "E2E test conference 2"
        val testConferenceDescription2 = "Nightly E2E Test conference description 2"
        Log.d(PREPARATION_TAG, "Create a conference with '$testConferenceTitle2' title and '$testConferenceDescription2' description.")
        ConferencesApi.createCourseConference(course.id, teacher.token, testConferenceTitle2, testConferenceDescription2, longRunning = true, duration = 120, recipientUserIds = listOf(student.id))

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that '$testConferenceTitle' conference is displayed on the Conference List Page with the corresponding status and description.")
        refresh()
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

        Log.d(STEP_TAG, "Click on the expand/collapse button to COLLAPSE the 'New Conferences' group.")
        conferenceListPage.clickExpandCollapseGroup("New Conferences")

        Log.d(ASSERTION_TAG, "Assert that both '$testConferenceTitle' and '$testConferenceTitle2' conferences are NOT displayed after collapsing the 'New Conferences' group.")
        conferenceListPage.assertConferenceNotDisplayed(testConferenceTitle)
        conferenceListPage.assertConferenceNotDisplayed(testConferenceTitle2)

        Log.d(STEP_TAG, "Click on the expand/collapse button to EXPAND the 'New Conferences' group.")
        conferenceListPage.clickExpandCollapseGroup("New Conferences")

        Log.d(ASSERTION_TAG, "Assert that both '$testConferenceTitle' and '$testConferenceTitle2' conferences are displayed again after expanding the 'New Conferences' group.")
        conferenceListPage.assertConferenceDisplayed(testConferenceTitle)
        conferenceListPage.assertConferenceDisplayed(testConferenceTitle2)

        Log.d(STEP_TAG, "Open '$testConferenceTitle' conference's detailer page.")
        conferenceListPage.openConferenceDetails(testConferenceTitle)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Conference Details' and there is the '${course.name}' course's name displayed as a subtitle.")
        conferenceDetailsPage.assertConferenceDetailsToolbarText(course.name)

        Log.d(ASSERTION_TAG, "Assert that the proper conference title '$testConferenceTitle', status ('Not Started') and description, '$testConferenceDescription' are displayed.")
        conferenceDetailsPage.assertConferenceTitleDisplayed(testConferenceTitle)
        conferenceDetailsPage.assertConferenceStatus("Not Started")
        conferenceDetailsPage.assertDescription(testConferenceDescription)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CONFERENCES, TestCategory.E2E)
    fun testConferenceLifecycleE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        val conferenceWebPage = ConferenceWebPage(device)
        val conferenceName = "E2E lifecycle conference"

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to '${course.name}' course's Conferences Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectConferences()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed since no conference has been created yet.")
        conferenceListPage.assertEmptyView()

        Log.d(STEP_TAG, "Tap 'Open Externally' to open the conferences web page in a Chrome Custom Tab.")
        conferenceListPage.clickOnOpenExternallyButton()
        conferenceWebPage.waitForPageLoaded()

        Log.d(STEP_TAG, "Tap 'Add Conference' on the web page to open the create modal.")
        conferenceWebPage.clickAddConference()

        Log.d(ASSERTION_TAG, "Assert that the 'New Video Conference' modal is displayed with the expected fields.")
        conferenceWebPage.assertCreateModalDisplayed()

        Log.d(STEP_TAG, "Enter '$conferenceName' as the conference name and tap 'Create'.")
        conferenceWebPage.setConferenceName(conferenceName)
        conferenceWebPage.clickCreate()

        Log.d(ASSERTION_TAG, "Assert that '$conferenceName' appears under 'New Conferences' in the web list.")
        conferenceWebPage.assertConferenceInWebList(conferenceName)

        Log.d(STEP_TAG, "Tap 'Start' on the conference. Do not join the BigBlueButton call, close the Custom Tab and return to the app and refresh the native Conference List page.")
        conferenceWebPage.clickStart()
        conferenceWebPage.closeCustomTab()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that '$conferenceName' status is now 'In Progress' under 'New Conferences'.")
        conferenceListPage.assertConferenceStatus(conferenceName, "In Progress")
        conferenceListPage.assertNewConferencesDisplayed()

        Log.d(STEP_TAG, "Open '$conferenceName' conference details page.")
        conferenceListPage.openConferenceDetails(conferenceName)

        Log.d(ASSERTION_TAG, "Assert that the Conference Details page shows 'In Progress' status and the 'Join' button.")
        conferenceDetailsPage.assertConferenceTitleDisplayed(conferenceName)
        conferenceDetailsPage.assertConferenceInProgress()
        conferenceDetailsPage.assertJoinButtonDisplayed()

        Log.d(STEP_TAG, "Press back to return to the Conference List page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open 'Open Externally' again, end the conference, confirm the dialog, then return to the app and refresh the native Conference List page.")
        conferenceListPage.clickOnOpenExternallyButton()
        conferenceWebPage.waitForPageLoaded()
        conferenceWebPage.clickEnd()
        conferenceWebPage.confirmEndDialog()
        conferenceWebPage.closeCustomTab()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that '$conferenceName' has moved to the 'Concluded Conferences' group with a status starting with 'Concluded'.")
        conferenceListPage.assertConferenceStatusStartsWith(conferenceName, "Concluded")
        conferenceListPage.assertConcludedConferencesGroupDisplayed()

        Log.d(STEP_TAG, "Open '$conferenceName' conference details page.")
        conferenceListPage.openConferenceDetails(conferenceName)

        Log.d(ASSERTION_TAG, "Assert that the Conference Details page shows a status starting with 'Concluded'.")
        conferenceDetailsPage.assertConferenceStatusStartsWith("Concluded")
    }

}