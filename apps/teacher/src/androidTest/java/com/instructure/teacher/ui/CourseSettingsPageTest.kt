/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 */
package com.instructure.teacher.ui

import com.instructure.espresso.TestRail
import com.instructure.espresso.randomString
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class CourseSettingsPageTest : TeacherTest() {

    @Test
    @Ditto
    @TestRail(ID = "C3108914")
    override fun displaysPageObjects() {
        navigateToCourseSettings()
        courseSettingsPage.assertPageObjects()
    }

    @Test
    @Ditto(sequential = true)
    @TestRail(ID = "C3108915")
    fun editCourseName() {
        navigateToCourseSettings()
        courseSettingsPage.clickCourseName()
        val newCourseName = mockableString("new-course-name") { randomString() }
        courseSettingsPage.editCourseName(newCourseName)
        courseSettingsPage.assertCourseNameChanged(newCourseName)
    }

    @Test
    @Ditto
    @TestRail(ID = "C3108916")
    fun editCourseHomePage() {
        navigateToCourseSettings()
        courseSettingsPage.clickSetHomePage()
        val newCourseHomePage: String = courseSettingsPage.selectNewHomePage()
        courseSettingsPage.assertHomePageChanged(newCourseHomePage)
    }

    private fun navigateToCourseSettings() {
        val data = seedData(teachers = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        tokenLogin(teacher)
        coursesListPage.openCourseAtPosition(0)
        courseBrowserPage.clickSettingsButton()
    }
}
