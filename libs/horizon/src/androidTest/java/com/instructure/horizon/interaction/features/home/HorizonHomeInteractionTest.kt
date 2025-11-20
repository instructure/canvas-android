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
package com.instructure.horizon.interaction.features.home

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetHorizonCourseManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetProgramsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetSkillsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetWidgetsManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.graphql.GetCoursesModule
import com.instructure.canvasapi2.di.graphql.JourneyModule
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetSkillsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetWidgetsManager
import com.instructure.horizon.espresso.HorizonTest
import com.instructure.horizon.pages.HorizonHomePage
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(GetCoursesModule::class, JourneyModule::class)
class HorizonHomeInteractionTest : HorizonTest() {
    private val fakeGetHorizonCourseManager = FakeGetHorizonCourseManager()
    private val fakeGetProgramsManager = FakeGetProgramsManager()
    private val fakeGetWidgetsManager = FakeGetWidgetsManager()
    private val fakeGetSkillsManager = FakeGetSkillsManager()

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

    private val homePage: HorizonHomePage by lazy { HorizonHomePage(composeTestRule) }

    @Test
    fun testBottomNavigationAfterLogin() {
        val data = MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 1
        )
        val student = data.students.first()
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        composeTestRule.waitForIdle()

        homePage.assertBottomNavigationVisible()
    }

    @Test
    fun testNavigationBetweenTabs() {
        val data = MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 1
        )
        val student = data.students.first()
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        composeTestRule.waitForIdle()

        homePage.assertBottomNavigationVisible()
        homePage.clickLearnTab()
        homePage.clickHomeTab()
        homePage.clickHomeTab()
    }
}
