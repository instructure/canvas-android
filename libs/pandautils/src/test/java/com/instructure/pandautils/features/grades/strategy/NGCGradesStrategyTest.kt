/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.grades.strategy

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.grades.GradesFilterUiState
import com.instructure.pandautils.features.grades.GradesUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NGCGradesStrategyTest {

    private val strategy = NGCGradesStrategy()

    @Test
    fun `initialFilterState returns multi-choice defaults`() {
        val course = Course(id = 1L, name = "Biology 101")

        val state = strategy.initialFilterState(course, emptyList(), null)

        assertNotNull(state)
        assertEquals("Biology 101", state!!.courseName)
        assertFalse(state.isFilterScreenOpen)
        assertEquals(
            listOf(
                AssignmentFilter.NotYetSubmitted,
                AssignmentFilter.ToBeGraded,
                AssignmentFilter.Graded,
                AssignmentFilter.Other,
            ),
            state.selectedFilters.selectedAssignmentFilters
        )
        assertEquals(AssignmentGroupByOption.AssignmentGroup, state.selectedFilters.selectedGroupByOption)
        assertEquals(AssignmentListFilterType.MultiChoice, state.filterOptions!!.assignmentFilters.assignmentFilterType)
    }

    @Test
    fun `initialFilterState wraps grading periods with null option`() {
        val period = GradingPeriod(id = 10L, title = "Q1")
        val state = strategy.initialFilterState(Course(id = 1L), listOf(period), period)

        assertEquals(listOf(null, period), state!!.filterOptions!!.gradingPeriodOptions)
        assertEquals(period, state.selectedFilters.selectedGradingPeriodFilter)
    }

    @Test
    fun `applyFilters returns assignments unchanged when filter is null`() {
        val assignments = listOf(Assignment(id = 1L), Assignment(id = 2L))
        val result = strategy.applyFilters(assignments, GradesUiState(filter = null))
        assertEquals(assignments, result)
    }

    @Test
    fun `applyFilters limits to graded when only Graded selected`() {
        val gradedAssignment = makeAssignment(
            id = 1L,
            online = true,
            submission = Submission(
                workflowState = "graded",
                grade = "A",
                postedAt = java.util.Date(),
            ),
        )
        val ungradedAssignment = makeAssignment(id = 2L, online = true)
        val state = GradesUiState(
            filter = GradesFilterUiState(
                selectedFilters = AssignmentListSelectedFilters(
                    selectedAssignmentFilters = listOf(AssignmentFilter.Graded),
                ),
            ),
        )

        val result = strategy.applyFilters(listOf(gradedAssignment, ungradedAssignment), state)

        assertEquals(listOf(gradedAssignment), result)
    }

    @Test
    fun `onFilterScreenOpenChanged toggles isFilterScreenOpen`() {
        val state = GradesUiState(filter = GradesFilterUiState(isFilterScreenOpen = false))
        val updated = strategy.onFilterScreenOpenChanged(state, true)
        assertTrue(updated.filter!!.isFilterScreenOpen)
    }

    @Test
    fun `onFilterScreenOpenChanged is no-op when filter is null`() {
        val state = GradesUiState(filter = null)
        assertSame(state, strategy.onFilterScreenOpenChanged(state, true))
    }

    @Test
    fun `onFilterUpdated replaces selectedFilters`() {
        val state = GradesUiState(filter = GradesFilterUiState(selectedFilters = AssignmentListSelectedFilters()))
        val newFilters = AssignmentListSelectedFilters(
            selectedAssignmentFilters = listOf(AssignmentFilter.Graded),
            selectedGroupByOption = AssignmentGroupByOption.DueDate,
        )

        val updated = strategy.onFilterUpdated(state, newFilters)

        assertEquals(newFilters, updated.filter!!.selectedFilters)
    }

    private fun makeAssignment(
        id: Long,
        online: Boolean = false,
        submission: Submission? = null,
    ): Assignment {
        val submissionTypes = if (online) {
            listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
        } else {
            emptyList()
        }
        return Assignment(
            id = id,
            submissionTypesRaw = submissionTypes,
            submission = submission,
        )
    }
}
