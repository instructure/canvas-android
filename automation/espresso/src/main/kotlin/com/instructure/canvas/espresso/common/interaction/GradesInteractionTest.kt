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

package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.GradesPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.orDefault
import org.junit.Test


abstract class GradesInteractionTest : CanvasComposeTest() {

    private val gradesPage = GradesPage(composeTestRule)

    @Test
    fun groupHeaderCollapsesAndExpandsOnClick() {
        val data = initData()
        val course = data.courses.values.first()
        val assignment = data.assignments.values.find { it.dueAt == null }

        goToGrades(data, course.name)

        composeTestRule.waitForIdle()

        gradesPage.assertAssignmentIsDisplayed(assignment?.name.orEmpty())
        gradesPage.clickGroupHeader("Undated Assignments")
        gradesPage.assertAssignmentIsNotDisplayed(assignment?.name.orEmpty())
        gradesPage.clickGroupHeader("Undated Assignments")
        gradesPage.assertAssignmentIsDisplayed(assignment?.name.orEmpty())
    }

    @Test
    fun basedOnGradedAssignmentsSwitchChangesGrade() {
        val data = initData()
        val course = data.courses.values.first()
        val enrollment = data.enrollments.values.first { it.isStudent }

        goToGrades(data, course.name)

        composeTestRule.waitForIdle()

        gradesPage.assertGradeText(
            formatGrade(
                enrollment.currentScore.orDefault(),
                enrollment.currentGrade.orEmpty()
            )
        )
        gradesPage.clickBasedOnGradedAssignments()
        gradesPage.assertGradeText("N/A")
    }

    @Test
    fun changeSortBy() {
        val data = initData()
        val course = data.courses.values.first()

        goToGrades(data, course.name)

        composeTestRule.waitForIdle()

        gradesPage.assertGroupHeaderIsDisplayed("Overdue Assignments")
        gradesPage.clickFilterButton()
        gradesPage.clickFilterOption("Group")
        gradesPage.clickSaveButton()
        gradesPage.assertGroupHeaderIsNotDisplayed("Overdue Assignments")
        gradesPage.assertGroupHeaderIsDisplayed("overdue")
    }

    @Test
    fun openAssignmentDetails() {
        val data = initData()
        val course = data.courses.values.first()
        val assignment = data.assignments.values.find { it.dueAt == null }

        goToGrades(data, course.name)

        composeTestRule.waitForIdle()

        gradesPage.clickAssignment(assignment?.name.orEmpty())

        // TODO: Check that the assignment details page is displayed
    }

    @Test
    fun emptyState() {
        val data = initData(false)
        val course = data.courses.values.first()

        goToGrades(data, course.name)

        composeTestRule.waitForIdle()

        gradesPage.assertEmptyStateIsDisplayed()
    }

    private fun formatGrade(score: Double, grade: String): String {
        return "${NumberHelper.doubleToPercentage(score)} $grade"
    }

    abstract fun initData(addAssignmentGroups: Boolean = true): MockCanvas

    abstract fun goToGrades(data: MockCanvas, courseName: String)

    override fun displaysPageObjects() = Unit
}
