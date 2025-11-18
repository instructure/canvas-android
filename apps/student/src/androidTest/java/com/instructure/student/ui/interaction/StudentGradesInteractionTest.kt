/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.common.interaction.GradesInteractionTest
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignmentsToGroups
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Tab
import com.instructure.student.BuildConfig
import com.instructure.student.activity.LoginActivity
import com.instructure.student.ui.pages.classic.CourseBrowserPage
import com.instructure.student.ui.pages.classic.DashboardPage
import com.instructure.student.ui.utils.StudentActivityTestRule
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class StudentGradesInteractionTest : GradesInteractionTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    private val dashboardPage = DashboardPage()
    private val courseBrowserPage = CourseBrowserPage()

    override val isTesting = BuildConfig.IS_TESTING

    override val activityRule = StudentActivityTestRule(LoginActivity::class.java)

    override fun initData(addAssignmentGroups: Boolean): MockCanvas {
        return MockCanvas.init(
            studentCount = 1,
            courseCount = 1,
            withGradingPeriods = true
        ).apply {
            val gradesTab = Tab(position = 2, label = "Grades", visibility = "public", tabId = Tab.GRADES_ID)
            courseTabs[courses.values.first().id]!! += gradesTab

            if (addAssignmentGroups) {
                addAssignmentsToGroups(this.courses.values.first(), 3)
            }
        }
    }

    override fun goToGrades(data: MockCanvas, courseName: String) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        val course = data.courses.values.first()
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectGrades()
    }
}
