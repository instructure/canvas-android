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
package com.instructure.teacher.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CourseSettingsE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COURSE, TestCategory.E2E)
    fun testCourseSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 2)
        val teacher = data.teachersList[0]
        val firstCourse = data.coursesList[0]
        val secondCourse = data.coursesList[1]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)

        Log.d(STEP_TAG, "Open '${firstCourse.name}' course and click on Course Settings button.")
        dashboardPage.waitForRender()
        dashboardPage.openCourse(firstCourse)
        courseBrowserPage.clickSettingsButton()

        Log.d(ASSERTION_TAG, "Assert if Course Settings page is displayed correctly.")
        courseSettingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on 'Set Home Page' menu and select another page as home page.")
        courseSettingsPage.clickSetHomePage()
        val newCourseHomePage: String = courseSettingsPage.selectNewHomePage()

        Log.d(ASSERTION_TAG, "Assert if home page has been changed.")
        courseSettingsPage.assertHomePageChanged(newCourseHomePage)

        val newCourseName = "New Course Name"
        Log.d(STEP_TAG, "Click on 'Course Name' menu and edit course's name to be '$newCourseName'.")
        courseSettingsPage.clickCourseName()
        courseSettingsPage.editCourseName(newCourseName)

        Log.d(ASSERTION_TAG, "Assert that the course's name has been changed.")
        courseSettingsPage.assertCourseNameChanged(newCourseName)

        Log.d(STEP_TAG, "Go back to course browser page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the course's name has been changed there as well.")
        courseBrowserPage.assertCourseBrowserPageDisplayed()
        courseBrowserPage.assertCourseTitle(newCourseName)

        Log.d(STEP_TAG, "Navigate back to the courses list page.")
        Espresso.pressBack()
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert if the name of the first course's name has been changed there as well.")
        dashboardPage.assertDisplaysCourse(newCourseName)

        Log.d(STEP_TAG, "Open '${secondCourse.name}' course and click on Course Settings button.")
        dashboardPage.waitForRender()
        dashboardPage.openCourse(secondCourse)
        courseBrowserPage.clickSettingsButton()

        Log.d(ASSERTION_TAG, "Assert if Course Settings page is displayed correctly.")
        courseSettingsPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on 'Set Home Page' menu and select another page as home page.")
        courseSettingsPage.clickSetHomePage()
        val secondCourseNewHomePage: String = courseSettingsPage.selectNewHomePage()

        Log.d(ASSERTION_TAG, "Assert if home page has been changed.")
        courseSettingsPage.assertHomePageChanged(secondCourseNewHomePage)

        Log.d(STEP_TAG, "Go back to course browser page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the course's name has NOT been changed there.")
        courseBrowserPage.assertCourseBrowserPageDisplayed()
        courseBrowserPage.assertCourseTitle(secondCourse.name)

        Log.d(STEP_TAG, "Navigate back to the courses list page.")
        Espresso.pressBack()
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert if the name of the second course's name has NOT been changed there as well.")
        dashboardPage.assertDisplaysCourse(secondCourse.name)

        Log.d(STEP_TAG, "Refresh the courses list page.")
        refresh()
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert if the corresponding course names are displayed (new course name for first course and the original course name of the second course).")
        dashboardPage.assertDisplaysCourse(newCourseName)
        dashboardPage.assertDisplaysCourse(secondCourse.name)
    }
}