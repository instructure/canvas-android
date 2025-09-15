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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.espresso.randomString
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CourseSettingsInteractionTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        navigateToCourseSettings()
        courseSettingsPage.assertPageObjects()
    }

    @Test
    fun editCourseName() {
        navigateToCourseSettings()
        courseSettingsPage.clickCourseName()
        val newCourseName = randomString()
        courseSettingsPage.editCourseName(newCourseName)
        courseSettingsPage.assertCourseNameChanged(newCourseName)
    }

    @Test
    fun editCourseHomePage() {
        navigateToCourseSettings()
        courseSettingsPage.clickSetHomePage()
        val newCourseHomePage: String = courseSettingsPage.selectNewHomePage()
        courseSettingsPage.assertHomePageChanged(newCourseHomePage)
    }

    private fun navigateToCourseSettings() {
        val data = MockCanvas.init(
                teacherCount = 1,
                favoriteCourseCount = 1,
                courseCount = 1
        )
        val teacher = data.teachers[0]
        val course = data.courses.values.first()
        val token = data.tokenFor(teacher)!!

        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.clickSettingsButton()
    }
}
