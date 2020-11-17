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

import com.instructure.canvas.espresso.E2E
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class DashboardE2ETest: TeacherTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testDashboardE2E() {

        val data = seedData(teachers = 1, courses = 2, favoriteCourses = 1)
        val teacher = data.teachersList[0]

        tokenLogin(teacher)

        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertDisplaysCourse(data.coursesList[0])

        dashboardPage.clickSeeAll()
        for(course in data.coursesList) {
            dashboardPage.assertDisplaysCourse(course)
        }
        dashboardPage.navigateBack()
        
        dashboardPage.editFavoriteCoursesWithCourse(data.coursesList[1])
        for(course in data.coursesList) {
            dashboardPage.assertDisplaysCourse(course)
        }

        dashboardPage.assertOpensCourse(data.coursesList[0])
    }
}