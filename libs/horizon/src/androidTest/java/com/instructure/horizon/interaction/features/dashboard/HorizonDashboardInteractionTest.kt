/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.interaction.features.dashboard

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addItemToModule
import com.instructure.canvas.espresso.mockcanvas.addModuleToCourse
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetHorizonCourseManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetProgramsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetSkillsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetWidgetsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeRedwoodApiManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.graphql.GetCoursesModule
import com.instructure.canvasapi2.di.graphql.JourneyModule
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetSkillsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetWidgetsManager
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.canvasapi2.models.Page
import com.instructure.horizon.espresso.HorizonTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Test

@HiltAndroidTest
@UninstallModules(GetCoursesModule::class, JourneyModule::class)
class HorizonDashboardInteractionTest: HorizonTest() {
    private val fakeGetHorizonCourseManager = FakeGetHorizonCourseManager()
    private val fakeGetProgramsManager = FakeGetProgramsManager()
    private val fakeGetWidgetsManager = FakeGetWidgetsManager()
    private val fakeGetSkillsManager = FakeGetSkillsManager()
    private val fakeRedwoodApiManager = FakeRedwoodApiManager()

    @BindValue
    @JvmField
    val getProgramsManager: GetProgramsManager = fakeGetProgramsManager

    @BindValue
    @JvmField
    val getWidgetsManager: GetWidgetsManager = fakeGetWidgetsManager

    @BindValue
    @JvmField
    val getSkillsManager: GetSkillsManager = fakeGetSkillsManager

    @BindValue
    @JvmField
    val getCoursesManager: HorizonGetCoursesManager = fakeGetHorizonCourseManager

    @BindValue
    @JvmField
    val redwoodApiManager: RedwoodApiManager = fakeRedwoodApiManager

    @Test
    fun testDashboardCards() {
        val data = MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 3
        )
        val course1 = data.courses.values.toList()[0]
        val course2 = data.courses.values.toList()[1]
        val module1 = data.addModuleToCourse(course1, "Module 0")
        val module2 = data.addModuleToCourse(course2, "Module 1")
        val moduleItem1 = data.addItemToModule(course1, module1.id, Page(title = "Module Item 1"))
        val moduleItem2 = data.addItemToModule(course2, module2.id, Page(title = "Module Item 2"))
        val student = data.students.first()
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        val programs = runBlocking { fakeGetProgramsManager.getPrograms(false) }
        dashboardPage.assertNotStartedProgramDisplayed(programs[1].name)
        dashboardPage.assertCourseCardDisplayed(
            course1.name,
            listOf(programs[0].name),
            fakeGetHorizonCourseManager.getCourses().first().progress,
            moduleItem1.title
        )
    }
}