/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.ui

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class DashboardPageTest : StudentTest() {

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    override fun displaysPageObjects() {
        getToDashboard()
        dashboardPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun displaysAddCourseMessage() {
        getToDashboard(courseCount = 0, pastCourseCount = 1)
        dashboardPage.assertDisplaysAddCourseMessage()
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DASHBOARD, TestCategory.INTERACTION)
    fun displaysCourses() {
        getToDashboard()
        dashboardPage.assertDisplaysCourses()
    }

    private fun getToDashboard(courseCount: Int = 1, pastCourseCount: Int = 0): MockCanvas {
        val data = MockCanvas.init(studentCount = 1, courseCount = courseCount, pastCourseCount = pastCourseCount)
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        return data
    }

}
