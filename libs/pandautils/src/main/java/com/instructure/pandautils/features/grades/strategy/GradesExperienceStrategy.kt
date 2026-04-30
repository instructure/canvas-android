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
import com.instructure.pandautils.features.grades.GradesFilterUiState
import com.instructure.pandautils.features.grades.GradesUiState

interface GradesExperienceStrategy {

    fun initialFilterState(
        course: Course,
        gradingPeriods: List<GradingPeriod>,
        currentGradingPeriod: GradingPeriod?,
    ): GradesFilterUiState? = null

    fun applyFilters(
        assignments: List<Assignment>,
        uiState: GradesUiState,
    ): List<Assignment> = assignments

    fun onFilterScreenOpenChanged(
        uiState: GradesUiState,
        open: Boolean,
    ): GradesUiState = uiState

    fun onFilterUpdated(
        uiState: GradesUiState,
        selected: AssignmentListSelectedFilters,
    ): GradesUiState = uiState
}
