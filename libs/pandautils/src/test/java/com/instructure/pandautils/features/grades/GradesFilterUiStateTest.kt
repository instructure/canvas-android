/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.features.grades

import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterData
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOptions
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GradesFilterUiStateTest {

    private val allFilters = listOf(
        AssignmentFilter.NotYetSubmitted,
        AssignmentFilter.ToBeGraded,
        AssignmentFilter.Graded,
        AssignmentFilter.Other,
    )

    @Test
    fun `isFilterApplied is false when no assignment filters are selected`() {
        val state = filterState(selected = emptyList())

        assertFalse(state.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is false when all assignment filter options are selected`() {
        val state = filterState(selected = allFilters)

        assertFalse(state.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when a proper subset of assignment filters is selected`() {
        val state = filterState(selected = listOf(AssignmentFilter.Graded))

        assertTrue(state.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when filterOptions is null but filters are selected`() {
        val state = GradesFilterUiState(
            selectedFilters = AssignmentListSelectedFilters(selectedAssignmentFilters = allFilters),
            filterOptions = null,
        )

        assertTrue(state.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is false when default assignment filters and All Grading Periods are selected`() {
        val state = filterState(selected = allFilters, gradingPeriod = null)

        assertFalse(state.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when a specific grading period is selected`() {
        val state = filterState(selected = allFilters, gradingPeriod = GradingPeriod(id = 1L, title = "Fall 2026"))

        assertTrue(state.isFilterApplied)
    }

    @Test
    fun `isFilterApplied is true when both an assignment filter subset and a grading period are selected`() {
        val state = filterState(
            selected = listOf(AssignmentFilter.Graded),
            gradingPeriod = GradingPeriod(id = 1L, title = "Fall 2026"),
        )

        assertTrue(state.isFilterApplied)
    }

    private fun filterState(
        selected: List<AssignmentFilter>,
        gradingPeriod: GradingPeriod? = null,
    ): GradesFilterUiState = GradesFilterUiState(
        selectedFilters = AssignmentListSelectedFilters(
            selectedAssignmentFilters = selected,
            selectedGradingPeriodFilter = gradingPeriod,
        ),
        filterOptions = AssignmentListFilterOptions(
            assignmentFilters = AssignmentListFilterData(
                assignmentFilterOptions = allFilters,
                assignmentFilterType = AssignmentListFilterType.MultiChoice,
            ),
            assignmentStatusFilters = null,
            groupByOptions = emptyList(),
            gradingPeriodOptions = listOf(null, gradingPeriod),
        ),
    )
}
