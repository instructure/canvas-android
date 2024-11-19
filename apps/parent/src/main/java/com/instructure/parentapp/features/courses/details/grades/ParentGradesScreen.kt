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

package com.instructure.parentapp.features.courses.details.grades

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instructure.pandautils.features.grades.GradesScreen
import com.instructure.pandautils.features.grades.GradesViewModel
import com.instructure.pandautils.features.grades.GradesViewModelAction


@Composable
internal fun ParentGradesScreen(
    navigateToAssignmentDetails: (Long, Long) -> Unit
) {
    val gradesViewModel: GradesViewModel = viewModel()
    val gradeUiState by remember { gradesViewModel.uiState }.collectAsState()
    val events = gradesViewModel.events
    LaunchedEffect(events) {
        events.collect { action ->
            when (action) {
                is GradesViewModelAction.NavigateToAssignmentDetails -> {
                    navigateToAssignmentDetails(action.courseId, action.assignmentId)
                }
            }
        }
    }

    GradesScreen(gradeUiState, gradesViewModel::handleAction)
}
