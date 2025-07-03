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

package com.instructure.pandautils.features.speedgrader.details.submissiondetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.type.SubmissionType
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.features.speedgrader.SpeedGraderSelectedAttemptHolder
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentViewModel.Companion.ASSIGNMENT_ID_KEY
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentViewModel.Companion.STUDENT_ID_KEY
import com.instructure.pandautils.utils.ScreenState
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SubmissionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SubmissionDetailsRepository,
    private val speedGraderSelectedAttemptHolder: SpeedGraderSelectedAttemptHolder
) : ViewModel() {

    private val assignmentId: Long = savedStateHandle.get<Long>(ASSIGNMENT_ID_KEY) ?: -1L
    private val studentId: Long = savedStateHandle.get<Long>(STUDENT_ID_KEY) ?: -1L

    private val _uiState = MutableStateFlow(SubmissionDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            speedGraderSelectedAttemptHolder.selectedAttemptIdFlowFor(studentId).collectLatest {
                loadSubmissionDetails(it)
            }
        }
    }

    private fun loadSubmissionDetails(attemptId: Long?) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = ScreenState.Loading) }

            val submission = repository.getSubmission(assignmentId, studentId)
                .submission?.submissionHistoriesConnection?.edges
                ?.find { it?.node?.attempt == attemptId?.toInt() }
                ?.node

            _uiState.update {
                val wordCount = submission?.wordCount
                it.copy(
                    wordCount = wordCount.orDefault().toInt(),
                    state = if (
                        submission?.submissionType == SubmissionType.online_text_entry
                        && wordCount != null
                    ) {
                        ScreenState.Content
                    } else {
                        ScreenState.Empty
                    }
                )
            }
        } catch {
            _uiState.update {
                it.copy(state = ScreenState.Empty) // Not showing error for now, just hiding the section
            }
        }
    }
}
