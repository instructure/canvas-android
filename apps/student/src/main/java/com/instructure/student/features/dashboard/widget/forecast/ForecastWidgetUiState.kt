/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.dashboard.widget.forecast

import androidx.fragment.app.FragmentActivity
import java.time.LocalDate
import java.util.Date

enum class ForecastSection {
    MISSING,
    DUE,
    RECENT_GRADES
}

data class WeekPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val displayText: String,
    val weekNumber: Int
)

data class AssignmentItem(
    val id: Long,
    val courseId: Long,
    val courseName: String,
    val courseColor: Int,
    val assignmentName: String,
    val dueDate: Date?,
    val gradedDate: Date?,
    val pointsPossible: Double,
    val weight: Double?,
    val iconRes: Int,
    val url: String
)

data class ForecastWidgetUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val weekPeriod: WeekPeriod? = null,
    val canNavigatePrevious: Boolean = true,
    val canNavigateNext: Boolean = true,
    val missingAssignments: List<AssignmentItem> = emptyList(),
    val dueAssignments: List<AssignmentItem> = emptyList(),
    val recentGrades: List<AssignmentItem> = emptyList(),
    val selectedSection: ForecastSection? = null,
    val onNavigatePrevious: () -> Unit = {},
    val onNavigateNext: () -> Unit = {},
    val onSectionSelected: (ForecastSection) -> Unit = {},
    val onAssignmentClick: (FragmentActivity, Long, Long) -> Unit = { _, _, _ -> },
    val onRetry: () -> Unit = {}
)