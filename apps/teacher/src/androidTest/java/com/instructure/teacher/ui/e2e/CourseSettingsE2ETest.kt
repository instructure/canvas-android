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
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

private const val STEP_TAG = "CourseSettingsE2ETest #STEP# "
private const val PREPARATION_TAG = "CourseSettingsE2ETest #PREPARATION# "

@HiltAndroidTest
class CourseSettingsE2ETest : TeacherTest() {

    override fun displaysPageObjects() {
        TODO("not implemented")
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //Intentionally empty, because we don't check accessibility in E2E tests.
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COURSE, TestCategory.E2E)
    fun testCourseSettingsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 2)
        val teacher = data.teachersList[0]
        val firstCourse = data.coursesList[0]
        val secondCourse = data.coursesList[1]

        Log.d(
            STEP_TAG,
            "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}"
        )
        tokenLogin(teacher)

        Log.d(STEP_TAG, "Open ${firstCourse.name} course and click on Course Settings button.")
        dashboardPage.waitForRender()
        coursesListPage.openCourse(firstCourse)
        courseBrowserPage.clickSettingsButton()

        Log.d(
            STEP_TAG,
            "Click on 'Set Home Page' menu and select another page as home page. Assert if home page has been changed."
        )
        courseSettingsPage.assertPageObjects()
        courseSettingsPage.clickSetHomePage()
        val newCourseHomePage: String = courseSettingsPage.selectNewHomePage()
        courseSettingsPage.assertHomePageChanged(newCourseHomePage)

        val newCourseName = "New Course Name"
        Log.d(
            STEP_TAG,
            "Click on 'Course Name' menu and edit course's name to be $newCourseName. Assert that the course's name has been changed."
        )
        courseSettingsPage.clickCourseName()
        courseSettingsPage.editCourseName(newCourseName)
        courseSettingsPage.assertCourseNameChanged(newCourseName)

        Log.d(
            STEP_TAG,
            "Go back to course browser page and assert that the course's name has been changed there as well."
        )
        Espresso.pressBack()
        courseBrowserPage.assertCourseBrowserPageDisplayed()
        courseBrowserPage.assertCourseTitle(newCourseName)

        Log.d(
            STEP_TAG,
            "Navigate back to the courses list page and assert if the name of the first course's name has been changed there as well."
        )
        Espresso.pressBack()
        dashboardPage.waitForRender()
        coursesListPage.assertDisplaysCourse(newCourseName)

        Log.d(STEP_TAG, "Open ${secondCourse.name} course and click on Course Settings button.")
        dashboardPage.waitForRender()
        coursesListPage.openCourse(secondCourse)
        courseBrowserPage.clickSettingsButton()

        Log.d(
            STEP_TAG,
            "Click on 'Set Home Page' menu and select another page as home page. Assert if home page has been changed."
        )
        courseSettingsPage.assertPageObjects()
        courseSettingsPage.clickSetHomePage()
        val secondCourseNewHomePage: String = courseSettingsPage.selectNewHomePage()
        courseSettingsPage.assertHomePageChanged(secondCourseNewHomePage)

        Log.d(
            STEP_TAG,
            "Go back to course browser page and assert that the course's name has NOT been changed there."
        )
        Espresso.pressBack()
        courseBrowserPage.assertCourseBrowserPageDisplayed()
        courseBrowserPage.assertCourseTitle(secondCourse.name)

        Log.d(
            STEP_TAG,
            "Navigate back to the courses list page and assert if the name of the second course's name has NOT been changed there as well."
        )
        Espresso.pressBack()
        dashboardPage.waitForRender()
        coursesListPage.assertDisplaysCourse(secondCourse.name)

        Log.d(
            STEP_TAG,
            "Refresh the courses list page and assert if the corresponding course names are displayed (new course name for first course and the original course name of the second course)."
        )
        refresh()
        dashboardPage.waitForRender()
        coursesListPage.assertDisplaysCourse(newCourseName)
        coursesListPage.assertDisplaysCourse(secondCourse.name)
    }
}