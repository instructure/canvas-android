/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.ui.interaction

import com.instructure.canvas.espresso.common.interaction.GradesInteractionTest
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignmentsToGroups
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.features.login.LoginActivity
import com.instructure.parentapp.ui.pages.compose.CoursesPage
import com.instructure.parentapp.utils.ParentActivityTestRule
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules


@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class ParentGradesInteractionTest : GradesInteractionTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    private val coursesPage = CoursesPage(composeTestRule)

    override val isTesting = BuildConfig.IS_TESTING

    override val activityRule = ParentActivityTestRule(LoginActivity::class.java)

    override fun initData(addAssignmentGroups: Boolean): MockCanvas {
        return MockCanvas.init(
            parentCount = 1,
            studentCount = 1,
            courseCount = 1,
            withGradingPeriods = true
        ).apply {
            if (addAssignmentGroups) {
                addAssignmentsToGroups(this.courses.values.first(), 3)
            }
        }
    }

    override fun goToGrades(data: MockCanvas, courseName: String) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)
        coursesPage.clickCourseItem(courseName)
    }
}
