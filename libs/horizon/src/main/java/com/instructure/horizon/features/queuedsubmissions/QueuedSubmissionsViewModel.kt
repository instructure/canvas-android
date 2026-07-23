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
package com.instructure.horizon.features.queuedsubmissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.HorizonSubmissionHelper
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Deep-link path used by the dashboard widget to open the full scheduled-submissions list. */
const val QUEUED_SUBMISSIONS_PATH = "/queuedSubmissions"

@HiltViewModel
class QueuedSubmissionsViewModel @Inject constructor(
    private val createSubmissionDao: CreateSubmissionDao,
    private val submissionHelper: HorizonSubmissionHelper,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueuedSubmissionsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            createSubmissionDao.findScheduledSubmissionsFlow(apiPrefs.user?.id.orDefault())
                .collect { entities ->
                    _uiState.update { state ->
                        state.copy(
                            loading = false,
                            items = entities.map { it.toItemUiState() }
                        )
                    }
                }
        }
    }

    private fun CreateSubmissionEntity.toItemUiState(): QueuedSubmissionItemUiState {
        val submission = this
        return QueuedSubmissionItemUiState(
            submissionId = id,
            assignmentName = assignmentName.orEmpty(),
            state = submissionState,
            courseId = canvasContext.id,
            assignmentId = assignmentId,
            onRetry = { viewModelScope.launch { submissionHelper.retrySubmission(submission) } },
            onCancel = { viewModelScope.launch { submissionHelper.cancelSubmission(submission) } }
        )
    }
}
