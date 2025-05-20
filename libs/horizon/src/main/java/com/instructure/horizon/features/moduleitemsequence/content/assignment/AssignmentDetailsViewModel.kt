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

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.HorizonSubmissionHelper
import com.instructure.horizon.horizonui.organisms.cards.AttemptCardState
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.format
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val assignmentDetailsRepository: AssignmentDetailsRepository,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val submissionHelper: HorizonSubmissionHelper,
    private val apiPrefs: ApiPrefs,
    private val createSubmissionDao: CreateSubmissionDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assignmentId = savedStateHandle[ModuleItemContent.Assignment.ASSIGNMENT_ID] ?: -1L
    private val courseId = savedStateHandle[Const.COURSE_ID] ?: -1L
    private var assignmentName: String = ""

    private val _uiState =
        MutableStateFlow(
            AssignmentDetailsUiState(
                submissionDetailsUiState = SubmissionDetailsUiState(onNewAttemptClick = ::onNewAttemptClick),
                addSubmissionUiState = AddSubmissionUiState(
                    onSubmissionTypeSelected = ::submissionTypeSelected,
                    onSubmissionButtonClicked = ::showSubmissionConfirmation,
                    onDeleteDraftClicked = ::deleteDraftClicked,
                    onDismissDeleteDraftConfirmation = ::deleteDraftDismissed,
                    onDraftDeleted = ::deleteDraftSubmission,
                    onDismissSubmissionConfirmation = ::submissionConfirmationDismissed,
                    onSubmitAssignment = ::sendSubmission
                ),
                toolsBottomSheetUiState = ToolsBottomSheetUiState(onDismiss = ::dismissToolsBottomSheet),
                ltiButtonPressed = ::ltiButtonPressed,
                onUrlOpened = ::onUrlOpened,
                submissionConfirmationUiState = SubmissionConfirmationUiState(onDismiss = ::onSubmissionDialogDismissed)
            )
        )

    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
        viewModelScope.tryLaunch {
            val assignment = assignmentDetailsRepository.getAssignment(assignmentId, courseId, forceNetwork = false)
            assignmentName = assignment.name.orEmpty()
            val lastActualSubmission = assignment.lastActualSubmission
            val submissions = if (lastActualSubmission != null) {
                mapSubmissions(assignment.submission?.submissionHistory?.filterNotNull() ?: emptyList())
            } else {
                emptyList()
            }
            val initialAttempt = lastActualSubmission?.attempt ?: -1L

            val text = withContext(Dispatchers.IO) {
                val draft = createSubmissionDao.findDraftSubmissionByAssignmentId(assignmentId, apiPrefs.user?.id.orDefault())
                draft?.lastActivityDate?.let {
                    updateDraftText(it)
                }
                draft?.submissionEntry.orEmpty()
            }

            val submissionTypes = assignment.getSubmissionTypes().mapNotNull {
                when (it) {
                    Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> AddSubmissionTypeUiState.Text(text, ::onTextSubmissionChanged)
                    Assignment.SubmissionType.ONLINE_UPLOAD -> AddSubmissionTypeUiState.File("")
                    else -> null
                }
            }

            val description = htmlContentFormatter.formatHtmlWithIframes(assignment.description.orEmpty())

            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false),
                    instructions = description,
                    ltiUrl = assignment.externalToolAttributes?.url.orEmpty(),
                    submissionDetailsUiState = it.submissionDetailsUiState.copy(
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

    fun openAssignmentTools() {
        _uiState.update {
            it.copy(
                toolsBottomSheetUiState = it.toolsBottomSheetUiState.copy(show = true)
            )
        }
    }

    private fun dismissToolsBottomSheet() {
        _uiState.update {
            it.copy(
                toolsBottomSheetUiState = it.toolsBottomSheetUiState.copy(show = false)
            )
        }
    }

    private fun ltiButtonPressed(ltiUrl: String) {
        viewModelScope.launch {
            try {
                val authenticatedSessionURL =
                    assignmentDetailsRepository.authenticateUrl(ltiUrl)

                _uiState.update { it.copy(urlToOpen = authenticatedSessionURL) }
            } catch (e: Exception) {
                _uiState.update { it.copy(urlToOpen = ltiUrl) }
            }
        }
    }

    private fun onUrlOpened() {
        _uiState.update { it.copy(urlToOpen = null) }
    }

    private fun onNewAttemptClick() {
        _uiState.update {
            it.copy(
                showSubmissionDetails = false,
                showAddSubmission = true
            )
        }
    }

    private fun onTextSubmissionChanged(text: String) {
        val textSubmission =
            uiState.value.addSubmissionUiState.submissionTypes[uiState.value.addSubmissionUiState.selectedSubmissionTypeIndex]
        if (textSubmission is AddSubmissionTypeUiState.Text) {
            _uiState.update {
                it.copy(
                    addSubmissionUiState = it.addSubmissionUiState.copy(
                        submissionTypes = it.addSubmissionUiState.submissionTypes.mapIndexed { index, submissionType ->
                            if (index == uiState.value.addSubmissionUiState.selectedSubmissionTypeIndex) {
                                textSubmission.copy(text = text)
                            } else {
                                submissionType
                            }
                        }
                    )
                )
            }
            viewModelScope.launch {
                // We need to replace the line breaks from the RCE because if we delete any text it will still leave a <br> tag in the RCE
                if (text.replace("<br>", "").isNotBlank()) {
                    submissionHelper.saveDraft(CanvasContext.emptyCourseContext(id = courseId), assignmentId, assignmentName, text)
                    updateDraftText(Date())
                } else {
                    createSubmissionDao.deleteDraftByAssignmentId(assignmentId, apiPrefs.user?.id.orDefault())
                    updateDraftText()
                }
            }
        }
    }

    private fun updateDraftText(date: Date? = null) {
        val draftText = if (date == null) {
            ""
        } else {
            context.getString(R.string.assignmentDetails_draftSaved, date.format("dd/MM, h:mm a"))
        }
        _uiState.update {
            it.copy(
                addSubmissionUiState = it.addSubmissionUiState.copy(
                    draftDateString = draftText
                )
            )
        }
    }

    private fun sendSubmission() {
        _uiState.update {
            it.copy(
                addSubmissionUiState = it.addSubmissionUiState.copy(
                    showSubmissionConfirmation = false
                )
            )
        }
        val selectedSubmissionType =
            uiState.value.addSubmissionUiState.submissionTypes[uiState.value.addSubmissionUiState.selectedSubmissionTypeIndex]
        if (selectedSubmissionType is AddSubmissionTypeUiState.Text) {
            submissionHelper.startTextSubmission(
                canvasContext = CanvasContext.emptyCourseContext(id = courseId),
                assignmentId = assignmentId,
                text = selectedSubmissionType.text,
                assignmentName = assignmentName
            )
            viewModelScope.launch {
                createSubmissionDao.findSubmissionByAssignmentIdFlow(assignmentId, apiPrefs.user?.id.orDefault()).collect { entity ->
                    if (entity != null) {
                        updateSubmissionProgress(entity)
                    }
                }
            }
        } else if (selectedSubmissionType is AddSubmissionTypeUiState.File) {

        }
    }

    private suspend fun updateSubmissionProgress(entity: CreateSubmissionEntity) {
        val progress = entity.progress ?: 0f
        val showProgress = progress > 0f && progress < 100.0
        if (progress == 100.0f) {
            updateAssignment()
            onTextSubmissionChanged("")
            _uiState.update {
                it.copy(
                    showSubmissionDetails = true,
                    showAddSubmission = false,
                    addSubmissionUiState = it.addSubmissionUiState.copy(
                        submissionInProgress = showProgress
                    ),
                    submissionConfirmationUiState = it.submissionConfirmationUiState.copy(
                        show = true
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    addSubmissionUiState = it.addSubmissionUiState.copy(
                        submissionInProgress = showProgress,
                    ),
                    showSubmissionDetails = false,
                    showAddSubmission = true,
                )
            }
        }
    }

    private suspend fun updateAssignment() {
        val assignment = assignmentDetailsRepository.getAssignment(assignmentId, courseId, forceNetwork = true)
        val lastActualSubmission = assignment.lastActualSubmission
        val submissions = if (lastActualSubmission != null) {
            mapSubmissions(assignment.submission?.submissionHistory?.filterNotNull() ?: emptyList())
        } else {
            emptyList()
        }
        val initialAttempt = lastActualSubmission?.attempt ?: -1L

        val currentAttempt = lastActualSubmission?.let {
            createAttemptCard(it)
        }

        _uiState.update {
            it.copy(
                submissionDetailsUiState = it.submissionDetailsUiState.copy(
                    submissions = submissions,
                    currentSubmissionAttempt = initialAttempt
                ),
                showSubmissionDetails = lastActualSubmission != null,
                showAddSubmission = lastActualSubmission == null,
                submissionConfirmationUiState = it.submissionConfirmationUiState.copy(attemptCardState = currentAttempt)
            )
        }
    }

    private fun createAttemptCard(submission: Submission): AttemptCardState {
        return AttemptCardState(
            attemptTitle = context.getString(R.string.assignmentDetails_attemptNumber, submission.attempt),
            date = submission.submittedAt?.format("dd/MM, h:mm a").orEmpty()
        )
    }

    private fun onSubmissionDialogDismissed() {
        _uiState.update {
            it.copy(
                submissionConfirmationUiState = it.submissionConfirmationUiState.copy(
                    show = false
                )
            )
        }
    }

    private fun deleteDraftClicked() {
        _uiState.update {
            it.copy(
                addSubmissionUiState = it.addSubmissionUiState.copy(
                    showDeleteDraftConfirmation = true
                )
            )
        }
    }

    private fun deleteDraftDismissed() {
        _uiState.update {
            it.copy(
                addSubmissionUiState = it.addSubmissionUiState.copy(
                    showDeleteDraftConfirmation = false
                )
            )
        }
    }

    private fun deleteDraftSubmission() {
        viewModelScope.launch {
            createSubmissionDao.deleteDraftByAssignmentId(assignmentId, apiPrefs.user?.id.orDefault())
            _uiState.update {
                it.copy(
                    addSubmissionUiState = it.addSubmissionUiState.copy(
                        showDeleteDraftConfirmation = false
                    )
                )
            }
            onTextSubmissionChanged("")
        }
    }

    private fun showSubmissionConfirmation() {
        _uiState.update {
            it.copy(
                addSubmissionUiState = it.addSubmissionUiState.copy(
                    showSubmissionConfirmation = true
                )
            )
        }
    }

    private fun submissionConfirmationDismissed() {
        _uiState.update {
            it.copy(
                addSubmissionUiState = it.addSubmissionUiState.copy(
                    showSubmissionConfirmation = false
                )
            )
        }
    }
}