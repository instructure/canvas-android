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
 *
 */

package com.instructure.teacher.ui

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.espresso.TestRail
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DashboardPageTest : TeacherTest() {

    @Test
    @TestRail(ID = "C3108898")
    override fun displaysPageObjects() {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3109494")
    fun displaysNoCoursesView() {
        val data = MockCanvas.init(teacherCount = 1, pastCourseCount = 1)
        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.assertEmptyView()
    }

    @Test
    @TestRail(ID = "C3108898")
    fun displaysCourseList() {
        val data = MockCanvas.init(teacherCount = 1, favoriteCourseCount = 3, courseCount = 3)
        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.assertHasCourses(data.courses.values.toList())
    }

    @Test
    fun displaysCourseListWithOnlyUnpublishedCourses() {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, publishCourses = false)
        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.assertHasCourses(data.courses.values.toList())
    }
}
