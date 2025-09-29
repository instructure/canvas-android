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

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderRepository,
    private val assignmentSubmissionRepository: AssignmentSubmissionRepository,
    private val postPolicyRouter: SpeedGraderPostPolicyRouter,
    private val speedGraderErrorHolder: SpeedGraderErrorHolder,
    private val resources: Resources
) : ViewModel() {

    private val assignmentId: Long = savedStateHandle[Const.ASSIGNMENT_ID]
        ?: throw IllegalStateException("Assignment ID is required")

    private val submissionIds: LongArray =
        savedStateHandle[SpeedGraderFragment.FILTERED_SUBMISSION_IDS] ?: longArrayOf()

    private val courseId: Long = savedStateHandle[Const.COURSE_ID]
        ?: throw IllegalStateException("Course ID is required")

    private val selectedItem: Int = savedStateHandle[Const.SELECTED_ITEM] ?: 0

    private var assignment: Assignment? = null

    private val _uiState = MutableStateFlow(
        SpeedGraderUiState(
            courseId,
            assignmentId,
            submissionIds.toList(),
            selectedItem,
            onPageChange = this::onPageChange,
            navigateToPostPolicy = this::navigateToPostPolicy,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        fetchData()
    }

    private fun navigateToPostPolicy(context: Context) {
        assignment?.let {
            postPolicyRouter.navigateToPostPolicies(
                context,
                courseId,
                it,
            )
        }
    }

    private fun fetchData() = viewModelScope.launch {
        try {
            _uiState.update {
                it.copy(loading = true)
            }
            assignment = assignmentSubmissionRepository.getAssignment(
                assignmentId = assignmentId,
                courseId = courseId,
                forceNetwork = false
            )
            val ids = if (submissionIds.isEmpty()) {
                assignmentSubmissionRepository.getGradeableStudentSubmissions(
                    assignmentId,
                    courseId,
                    false
                ).map { it.id }
            } else {
                submissionIds.toList()
            }
            val assignmentDetails = repository.getAssignmentDetails(assignmentId)
            _uiState.update {
                it.copy(
                    assignmentName = assignmentDetails.assignment?.title.orEmpty(),
                    courseName = assignmentDetails.assignment?.course?.name.orEmpty(),
                    loading = false,
                    submissionIds = ids
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(loading = false)
            }
            speedGraderErrorHolder.postError(
                message = resources.getString(R.string.generalUnexpectedError),
                retryAction = ::fetchData
            )
        }
    }

    private fun onPageChange(position: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedItem = position)
        }
    }
}