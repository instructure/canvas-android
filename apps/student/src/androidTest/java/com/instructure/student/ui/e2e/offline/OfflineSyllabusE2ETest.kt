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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineSyllabusE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYLLABUS, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineSyllabusE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val testSyllabusBody = "This is the syllabus body."
        val data = seedData(students = 1, teachers = 1, courses = 1, syllabusBody = testSyllabusBody)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, submissionTypes = listOf(
            SubmissionType.ON_PAPER), withDescription = true, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = {

            refresh()

            Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
            dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

            Log.d(STEP_TAG, "Expand '${course.name}' course.")
            manageOfflineContentPage.expandCollapseItem(course.name)

            Log.d(STEP_TAG, "Select the 'Syllabus' of '${course.name}' course for sync. Click on the 'Sync' button.")
            manageOfflineContentPage.changeItemSelectionState("Syllabus")
            manageOfflineContentPage.clickOnSyncButtonAndConfirm()

            Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
            dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
            device.waitForIdle()
        }) {

            Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
            dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

            Log.d(STEP_TAG, "Expand '${course.name}' course.")
            manageOfflineContentPage.expandCollapseItem(course.name)

            Log.d(STEP_TAG, "Select the 'Syllabus' of '${course.name}' course for sync. Click on the 'Sync' button.")
            manageOfflineContentPage.changeItemSelectionState("Syllabus")
            manageOfflineContentPage.clickOnSyncButtonAndConfirm()

            Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
            dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
            device.waitForIdle()
        }

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select '${course.name}' course.")
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to Syllabus Page.")
        courseBrowserPage.selectSyllabus()

        Log.d(ASSERTION_TAG, "Assert that the syllabus body string is displayed and that the toolbar subtitle is the '${course.name}' course name.")
        syllabusPage.assertSyllabusBody(testSyllabusBody)
        syllabusPage.assertToolbarCourseTitle(course.name)

        Log.d(STEP_TAG, "Navigate to 'Summary' tab.")
        syllabusPage.selectSummaryTab()

        Log.d(ASSERTION_TAG, "Assert that all of the items, so '${assignment.name}' assignment is displayed.")
        syllabusPage.assertItemDisplayed(assignment.name)

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Page List Page.")
        OfflineTestUtils.assertOfflineIndicator()
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }
}