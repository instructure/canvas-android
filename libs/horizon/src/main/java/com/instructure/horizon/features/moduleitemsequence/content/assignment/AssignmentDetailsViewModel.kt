/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    private val assignmentDetailsRepository: AssignmentDetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assignmentId = savedStateHandle[ModuleItemContent.Assignment.ASSIGNMENT_ID] ?: -1L
    private val courseId = savedStateHandle[Const.COURSE_ID] ?: -1L

    private val _uiState =
        MutableStateFlow(AssignmentDetailsUiState(addSubmissionUiState = AddSubmissionUiState(onSubmissionTypeSelected = ::submissionTypeSelected)))

    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
        viewModelScope.tryLaunch {
            val assignment = assignmentDetailsRepository.getAssignment(assignmentId, courseId, forceNetwork = false)
            val lastActualSubmission = assignment.lastActualSubmission
            val submissions = if (lastActualSubmission != null) {
                mapSubmissions(assignment.submission?.submissionHistory?.filterNotNull() ?: emptyList())
            } else {
                emptyList()
            }
            val initialAttempt = lastActualSubmission?.attempt ?: -1L

            val submissionTypes = assignment.getSubmissionTypes().mapNotNull {
                when (it) {
                    Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> AddSubmissionTypeUiState.Text("")
                    Assignment.SubmissionType.ONLINE_UPLOAD -> AddSubmissionTypeUiState.File("")
                    else -> null
                }
            }

            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false),
                    instructions = assignment.description.orEmpty(),
                    ltiUrl = assignment.externalToolAttributes?.url.orEmpty(),
                    submissionDetailsUiState = SubmissionDetailsUiState(
                        submissions = submissions,
                        currentSubmissionAttempt = initialAttempt
                    ),
                    addSubmissionUiState = it.addSubmissionUiState.copy(submissionTypes = submissionTypes),
                    showSubmissionDetails = lastActualSubmission != null,
                    showAddSubmission = lastActualSubmission == null,
                )
            }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }
    }

    private fun mapSubmissions(submissions: List<Submission>): List<SubmissionUiState> {
        return submissions.mapNotNull {
            if (it.submissionType == Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString ||
                it.submissionType == Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            ) {
                SubmissionUiState(
                    submissionAttempt = it.attempt,
                    submissionContent = when (it.submissionType) {
                        Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString -> SubmissionContent.TextSubmission(it.body.orEmpty())
                        Assignment.SubmissionType.ONLINE_UPLOAD.apiString -> SubmissionContent.FileSubmission(
                            it.attachments.map { attachment ->
                                FileItem(
                                    fileName = attachment.displayName.orEmpty(),
                                    fileUrl = attachment.url.orEmpty(),
                                    fileType = attachment.contentType.orEmpty(),
                                    fileId = attachment.id,
                                    thumbnailUrl = attachment.thumbnailUrl.orEmpty()
                                )
                            }
                        )

                        else -> SubmissionContent.TextSubmission("")
                    },
                    date = it.submittedAt?.toString().orEmpty()
                )
            } else {
                null
            }
        }
    }

    private fun submissionTypeSelected(index: Int) {
        _uiState.update {
            it.copy(
                addSubmissionUiState = it.addSubmissionUiState.copy(
                    selectedSubmissionTypeIndex = index,
                )
            )
        }
    }
}