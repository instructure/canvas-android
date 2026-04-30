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
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.grades.GradesUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class AcademicGradesStrategyTest {

    private val strategy = AcademicGradesStrategy()

    @Test
    fun `initialFilterState returns null`() {
        assertNull(
            strategy.initialFilterState(
                course = Course(id = 1L),
                gradingPeriods = emptyList(),
                currentGradingPeriod = null,
            )
        )
    }

    @Test
    fun `applyFilters returns assignments unchanged`() {
        val assignments = listOf(Assignment(id = 1L), Assignment(id = 2L))
        val result = strategy.applyFilters(assignments, GradesUiState())
        assertEquals(assignments, result)
    }

    @Test
    fun `onFilterScreenOpenChanged returns state unchanged`() {
        val state = GradesUiState(filter = null)
        assertSame(state, strategy.onFilterScreenOpenChanged(state, true))
    }

    @Test
    fun `onFilterUpdated returns state unchanged`() {
        val state = GradesUiState(filter = null)
        assertSame(
            state,
            strategy.onFilterUpdated(state, AssignmentListSelectedFilters())
        )
    }
}
