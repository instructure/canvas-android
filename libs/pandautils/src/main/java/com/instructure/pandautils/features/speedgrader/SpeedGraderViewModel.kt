/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.speedgrader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderRepository
) : ViewModel() {

    private val assignmentId: Long = savedStateHandle[Const.ASSIGNMENT_ID]
        ?: throw IllegalStateException("Assignment ID is required")

    private val submissionIds: LongArray = savedStateHandle[SpeedGraderFragment.FILTERED_SUBMISSION_IDS]
        ?: throw IllegalStateException("Submission IDs are required")

    private val _uiState = MutableStateFlow(SpeedGraderUiState(assignmentId, submissionIds.toList()))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fetchData()
        }
    }

    private suspend fun fetchData() {
        val assignmentDetails = repository.getAssignmentDetails(assignmentId)
        _uiState.update {
            it.copy(
                assignmentName = assignmentDetails.assignment?.title.orEmpty(),
                courseName = assignmentDetails.assignment?.course?.name.orEmpty(),
                courseColor = CanvasContext.emptyCourseContext(
                    assignmentDetails.assignment?.course?._id?.toLong().orDefault()
                ).color
            )
        }
    }
}