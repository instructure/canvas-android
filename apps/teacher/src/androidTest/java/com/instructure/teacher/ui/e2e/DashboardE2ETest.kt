/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DashboardE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testDashboardE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 2)
        val teacher = data.teachersList[0]
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG,"Assert that the '${course1.name}' and '${course2.name}' courses are displayed.")
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)

        Log.d(STEP_TAG, "Switch to List View and assert that all the courses are displayed.")
        dashboardPage.switchCourseView()
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)

        Log.d(STEP_TAG, "Switch back to Card View and assert that all the courses are displayed.")
        dashboardPage.switchCourseView()
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)

        Log.d(STEP_TAG,"Click on 'All Courses' button. Assert that the All Courses Page is loaded.")
        dashboardPage.clickEditDashboard()
        editDashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Toggle on favourite star icon of '${course2.name}' course." +
                "Assert that the 'mass' select button's label is 'Unselect All'.")
        editDashboardPage.toggleFavouringCourse(course2.name)
        editDashboardPage.assertMassSelectButtonIsDisplayed(true)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that only the favoured course '${course2.name}' is displayed." +
                "Assert that the other course '${course1.name}' is not displayed since it's not favoured.")
        dashboardPage.assertDisplaysCourse(course2)
        dashboardPage.assertCourseNotDisplayed(course1)

        Log.d(STEP_TAG,"Opens '${course2.name}' course and assert if Course Details Page has been opened. Navigate back to Dashboard Page.")
        dashboardPage.assertOpensCourse(course2)
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on 'All Courses' button. Assert that the All Courses Page is loaded.")
        dashboardPage.clickEditDashboard()
        editDashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Toggle off favourite star icon of '${course2.name}' course." +
                "Assert that the 'mass' select button's label is 'Select All'.")
        editDashboardPage.toggleFavouringCourse(course2.name)
        editDashboardPage.assertMassSelectButtonIsDisplayed(false)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that both of the courses, '${course1.name}' and '${course2.name}' are displayed.")
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)

        Log.d(STEP_TAG,"Click on 'All Courses' button. Assert that the All Courses Page is loaded.")
        dashboardPage.clickEditDashboard()
        editDashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on 'Select All' button.")
        editDashboardPage.clickOnMassSelectButton(false)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that both of the courses, '${course1.name}' and '${course2.name}' are displayed.")
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)

        Log.d(STEP_TAG, "Click on 'Edit nickname' menu of '${course1.name}' course.")
        dashboardPage.clickCourseOverflowMenu(course1.name, "Edit nickname")

        val newNickname = "New course nickname"
        Log.d(STEP_TAG, "Change '${course1.name}' course's nickname to: '$newNickname'.")
        dashboardPage.changeCourseNickname(newNickname)

        Log.d(STEP_TAG, "Wait for Dashboard Page to be reloaded and assert that the course's name has been changed to '$newNickname'.")
        dashboardPage.assertPageObjects()
        dashboardPage.assertCourseTitle(newNickname)

        Log.d(STEP_TAG, "Click on 'Edit nickname' menu of '$newNickname' course.")
        dashboardPage.clickCourseOverflowMenu(newNickname, "Edit nickname")

        Log.d(STEP_TAG, "Make the course nickname empty.")
        dashboardPage.changeCourseNickname(EMPTY_STRING)

        Log.d(STEP_TAG, "Wait for Dashboard Page to be reloaded. Assert that if there is no nickname for a course, the course's full name, '${course1.name}' will be displayed.")
        dashboardPage.assertPageObjects()
        dashboardPage.assertCourseTitle(course1.name)
    }

    @E2E
    @Test
    @TestMetaData(Priority.NICE_TO_HAVE, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testHelpMenuE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG,"Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Help Menu.")
        leftSideNavigationDrawerPage.clickHelpMenu()

        Log.d(STEP_TAG, "Assert Help Menu Dialog is displayed.")
        helpPage.assertHelpMenuDisplayed()

        Log.d(STEP_TAG, "Assert that all the corresponding Help menu content are displayed.")
        helpPage.assertHelpMenuContent()

        Log.d(STEP_TAG, "Click on 'Report a problem' menu and assert that it is possible to write into the input fields and the corresponding buttons are displayed as well.")
        helpPage.verifyReportAProblem("Test Subject", "Test Description")
        helpPage.assertReportProblemDialogDisplayed()

        Log.d(STEP_TAG, "Assert that when clicking on the different help menu items then the corresponding intents will be fired and has the proper URLs.")
        Intents.init()

        try {
            helpPage.assertHelpMenuURL("Search the Canvas Guides", "https://community.canvaslms.com/t5/Canvas/ct-p/canvas")
            helpPage.assertHelpMenuURL("Submit a Feature Idea", "https://community.canvaslms.com/t5/Idea-Conversations/idb-p/ideas")
            helpPage.assertHelpMenuURL("Ask the Community", "https://community.canvaslms.com/community/answers")
            helpPage.assertHelpMenuURL("Training Services Portal", "https://training-portal-beta-pdx.insproserv.net?canvas_domain=mobileqa.instructure.com&sf_id=")
            helpPage.assertHelpMenuURL("Conference Guides for Remote Classrooms", "https://community.canvaslms.com/docs/DOC-18572-conferences-resources")
            helpPage.assertHelpMenuURL("Share Your Love for the App", "https://community.canvaslms.com/t5/Canvas/ct-p/canvas")
        }
        finally {
            Intents.release()
        }
    }
}