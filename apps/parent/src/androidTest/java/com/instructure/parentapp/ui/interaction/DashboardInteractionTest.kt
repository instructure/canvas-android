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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.parentapp.utils.ParentTest
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test


@HiltAndroidTest
class DashboardInteractionTest : ParentTest() {

    @Test
    fun testObserverData() {
        val data = initData()

        goToDashboard(data)

        dashboardPage.openNavigationDrawer()
        dashboardPage.assertObserverData(data.parents.first())
    }

    @Test
    fun testChangeStudent() {
        val data = initData()

        goToDashboard(data)

        dashboardPage.assertSelectedStudent(data.students.first().shortName!!)
        dashboardPage.openStudentSelector()
        dashboardPage.selectStudent(data.students.last().shortName!!)
        dashboardPage.assertSelectedStudent(data.students.last().shortName!!)
    }

    private fun initData(): MockCanvas {
        return MockCanvas.init(
            parentCount = 1,
            studentCount = 2,
            courseCount = 1
        )
    }

    private fun goToDashboard(data: MockCanvas) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)
    }

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit
}
