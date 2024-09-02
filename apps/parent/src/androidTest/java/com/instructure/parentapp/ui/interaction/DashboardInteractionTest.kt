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

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.loginapi.login.R
import com.instructure.parentapp.utils.ParentTest
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
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

        val students = data.students.sortedBy { it.sortableName }
        dashboardPage.assertSelectedStudent(students.first().shortName!!)
        dashboardPage.openStudentSelector()
        dashboardPage.selectStudent(data.students.last().shortName!!)
        dashboardPage.assertSelectedStudent(students.last().shortName!!)
    }

    @Test
    fun testLogout() {
        val data = initData()

        goToDashboard(data)

        dashboardPage.openNavigationDrawer()
        dashboardPage.tapLogout()
        dashboardPage.assertLogoutDialog()
        dashboardPage.tapOk()
        waitForMatcherWithSleeps(ViewMatchers.withId(R.id.canvasLogo), 20000).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }

    @Test
    fun testSwitchUsers() {
        val data = initData()

        goToDashboard(data)

        dashboardPage.openNavigationDrawer()
        dashboardPage.tapSwitchUsers()
        waitForMatcherWithSleeps(ViewMatchers.withId(R.id.canvasLogo), 20000).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
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

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheck(
                SpeakableTextPresentCheck::class.java
            ),
            AccessibilityCheckResultUtils.matchesViews(
                ViewMatchers.withParent(
                    ViewMatchers.withClassName(
                        Matchers.equalTo(ComposeView::class.java.name)
                    )
                )
            )
        )

        super.enableAndConfigureAccessibilityChecks()
    }
}
