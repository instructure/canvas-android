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
package com.instructure.parentapp.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.pandautils.utils.AppTheme
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SettingsE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testDarkModeE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG,"Login with user: ${parent.name}, login id: ${parent.loginId}.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openNavigationDrawer()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(STEP_TAG,"Select Dark App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Dark mode).")
        settingsPage.selectAppTheme(AppTheme.DARK)

        Log.d(STEP_TAG,"Navigate back to Dashboard. Assert that the course label has the proper text color (which is used in Dark mode).")
        Espresso.pressBack()
        coursesPage.assertCourseLabelTextColor(course, 0xFFFFFFFF)

        Log.d(STEP_TAG,"Select '${course.name}' course and assert on the Course Browser Page that the assignment label has the proper text color (which is used in Dark mode).")
        coursesPage.clickCourseItem(course.name)
        courseDetailsPage.assertAssignmentLabelTextColor(testAssignment.name,0xFFFFFFFF)

        Log.d(STEP_TAG, "Navigate back and open the Left Side Navigation Drawer menu.")
        Espresso.pressBack()
        dashboardPage.openNavigationDrawer()

        Log.d(STEP_TAG,"Navigate to Settings Page and open App Theme Settings again.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(STEP_TAG,"Select Light App Theme and assert that the App Theme Title and Status has the proper text color (which is used in Light mode).")
        settingsPage.selectAppTheme(AppTheme.LIGHT)

        Log.d(STEP_TAG,"Navigate back to Dashboard. Assert that the course label has the proper text color (which is used in Light mode).")
        Espresso.pressBack()
        coursesPage.assertCourseLabelTextColor(course, 0xFF273540)

        Log.d(STEP_TAG,"Select '${course.name}' course and assert on the Course Browser Page that the assignment label has the proper text color (which is used in Dark mode).")
        coursesPage.clickCourseItem(course.name)
        courseDetailsPage.assertAssignmentLabelTextColor(testAssignment.name,0xFF273540)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testLegalPageE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]

        Log.d(STEP_TAG,"Login with user: ${parent.name}, login id: ${parent.loginId}.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openNavigationDrawer()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(STEP_TAG,"Open Legal Page and assert that all the corresponding buttons are displayed.")
        settingsPage.clickOnSettingsItem("Legal")
        legalPage.assertPageObjects()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testAboutE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]

        Log.d(STEP_TAG,"Login with user: ${parent.name}, login id: ${parent.loginId}.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openNavigationDrawer()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettings()

        Log.d(STEP_TAG, "Click on 'About' link to open About Page. Assert that About Page has opened.")
        settingsPage.clickOnSettingsItem("About")
        aboutPage.assertPageObjects()

        Log.d(STEP_TAG,"Check that domain is equal to: '${parent.domain}' (parent's domain).")
        aboutPage.domainIs(parent.domain)

        Log.d(STEP_TAG,"Check that Login ID is equal to: '${parent.loginId}' (parent's Login ID).")
        aboutPage.loginIdIs(parent.loginId)

        Log.d(STEP_TAG,"Check that e-mail is equal to: '${parent.loginId}' (parent's Login ID).")
        aboutPage.emailIs(parent.loginId)

        Log.d(STEP_TAG,"Assert that the Instructure company logo has been displayed on the About page.")
        aboutPage.assertInstructureLogoDisplayed()
    }
}