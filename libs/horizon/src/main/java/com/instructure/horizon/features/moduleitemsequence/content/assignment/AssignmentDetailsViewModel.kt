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
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContextSource
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.horizonui.organisms.cards.AttemptCardState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.localisedFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val assignmentDetailsRepository: AssignmentDetailsRepository,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val aiAssistContextProvider: AiAssistContextProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assignmentId = savedStateHandle[ModuleItemContent.Assignment.ASSIGNMENT_ID] ?: -1L
    private val courseId = savedStateHandle[Const.COURSE_ID] ?: -1L

    private val _uiState =
        MutableStateFlow(
            AssignmentDetailsUiState(
                assignmentId = assignmentId,
                submissionDetailsUiState = SubmissionDetailsUiState(onNewAttemptClick = ::onNewAttemptClick),
                toolsBottomSheetUiState = ToolsBottomSheetUiState(
                    onDismiss = ::dismissToolsBottomSheet,
                    onAttemptsClick = ::openAttemptSelector,
                    onCommentsClick = ::openComments
                ),
                ltiButtonPressed = ::ltiButtonPressed,
                onUrlOpened = ::onUrlOpened,
                submissionConfirmationUiState = SubmissionConfirmationUiState(onDismiss = ::onSubmissionDialogDismissed),
                onCommentsBottomSheetDismissed = ::dismissComments,
                onAssignmentUpdatedForAddSubmission = ::onAssignmentUpdatedForAddSubmission,
            )
        )

    val uiState = _uiState.asStateFlow()

    private val _assignmentFlow = MutableStateFlow<Assignment?>(null)
    val assignmentFlow = _assignmentFlow.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
        viewModelScope.tryLaunch {
            val assignment = assignmentDetailsRepository.getAssignment(assignmentId, courseId, forceNetwork = false)
            _assignmentFlow.value = assignment
            val lastActualSubmission = assignment.lastGradedOrSubmittedSubmission
            val attempts = assignment.submission?.submissionHistory?.filterNotNull() ?: emptyList()
            val submissions = if (lastActualSubmission != null) {
                mapSubmissions(attempts)
            } else {
                emptyList()
            }
            val initialAttempt = lastActualSubmission?.attempt ?: 0L // We need to use 0 as the initial attempt if there are no submissions
            val description = htmlContentFormatter.formatHtmlWithIframes(assignment.description.orEmpty(), courseId)

            val attemptsUiState = createAttemptCardsState(attempts, assignment, initialAttempt)
            val showAttemptSelector = assignment.allowedAttempts != 1L

            val hasUnreadComments = assignmentDetailsRepository.hasUnreadComments(assignmentId)

            aiAssistContextProvider.aiAssistContext = AiAssistContext(
                contextString = assignment.description.orEmpty(),
                contextSources = aiAssistContextProvider.aiAssistContext.contextSources +
                    AiAssistContextSource.Assignment(assignment.id.toString())
            )

            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false),
                    instructions = description,
                    ltiUrl = assignment.externalToolAttributes?.url.orEmpty(),
                    submissionDetailsUiState = it.submissionDetailsUiState.copy(
                        submissions = submissions,
                        currentSubmissionAttempt = initialAttempt
                    ),
                    showSubmissionDetails = lastActualSubmission != null,
                    showAddSubmission = lastActualSubmission == null,
                    onSubmissionSuccess = ::updateAssignment,
                    attemptSelectorUiState = it.attemptSelectorUiState.copy(attempts = attemptsUiState),
                    toolsBottomSheetUiState = it.toolsBottomSheetUiState.copy(showAttemptSelector = showAttemptSelector, hasUnreadComments = hasUnreadComments)
                )
            }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }
    }

    private fun createAttemptCardsState(
        attempts: List<Submission>,
        assignment: Assignment,
        initialAttempt: Long
    ) = attempts
        .filter { it.workflowState != "unsubmitted" }
        .sortedByDescending { it.attempt }
        .map { attempt ->
            createAttemptCard(
                attempt,
                assignment.pointsPossible,
                selected = attempt.attempt == initialAttempt,
                showScore = true,
                onClick = {
                    val viewingAttemptText = if (attempt.attempt != initialAttempt) {
                        context.getString(R.string.assignmentDetails_viewingAttempt, attempt.attempt)
                    } else {
                        null
                    }
                    _uiState.update {
                        it.copy(
                            showSubmissionDetails = true,
                            showAddSubmission = false,
                            submissionDetailsUiState = it.submissionDetailsUiState.copy(
                                currentSubmissionAttempt = attempt.attempt
                            ),
                            attemptSelectorUiState = it.attemptSelectorUiState.copy(
                                attempts = it.attemptSelectorUiState.attempts.map { card ->
                                    card.copy(selected = card.attemptNumber == attempt.attempt)
                                }
                            ),
                            viewingAttemptText = viewingAttemptText
                        )
                    }
                    dismissAttemptSelector()
                })
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
                showAddSubmission = true,
                attemptSelectorUiState = it.attemptSelectorUiState.copy(attempts = it.attemptSelectorUiState.attempts.map { attempt ->
                    attempt.copy(selected = false)
                })
            )
        }
    }

    private suspend fun updateAssignment() {
        val assignment = assignmentDetailsRepository.getAssignment(assignmentId, courseId, forceNetwork = true)
        _assignmentFlow.value = assignment
        val lastActualSubmission = assignment.lastGradedOrSubmittedSubmission
        val attempts = assignment.submission?.submissionHistory?.filterNotNull() ?: emptyList()
        val submissions = if (lastActualSubmission != null) {
            mapSubmissions(attempts)
        } else {
            emptyList()
        }
        val initialAttempt = lastActualSubmission?.attempt ?: -1L

        val currentAttempt = lastActualSubmission?.let {
            createAttemptCard(it, assignment.pointsPossible)
        }

        val attemptsUiState = createAttemptCardsState(attempts, assignment, initialAttempt)

        _uiState.update {
            it.copy(
                submissionDetailsUiState = it.submissionDetailsUiState.copy(
                    submissions = submissions,
                    currentSubmissionAttempt = initialAttempt
                ),
                showSubmissionDetails = lastActualSubmission != null,
                showAddSubmission = lastActualSubmission == null,
                submissionConfirmationUiState = it.submissionConfirmationUiState.copy(
                    show = true,
                    attemptCardState = currentAttempt
                ),
                attemptSelectorUiState = it.attemptSelectorUiState.copy(attempts = attemptsUiState),
                viewingAttemptText = null
            )
        }
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

    private fun createAttemptCard(
        submission: Submission,
        possibleScore: Double,
        selected: Boolean = false,
        showScore: Boolean = false,
        onClick: (() -> Unit)? = null
    ): AttemptCardState {
        val score = if (showScore && submission.isGraded) {
            val formattedScore = formatScore(submission.score)
            val formattedPossibleScore = formatScore(possibleScore)
            context.getString(R.string.attemptCard_score, formattedScore, formattedPossibleScore)
        } else {
            null
        }
        return AttemptCardState(
            attemptNumber = submission.attempt,
            attemptTitle = context.getString(R.string.assignmentDetails_attemptNumber, submission.attempt),
            date = submission.submittedAt?.localisedFormat("MM/dd, h:mm a", context).orEmpty(),
            score = score,
            selected = selected,
            onClick = onClick
        )
    }

    private fun openAttemptSelector() {
        _uiState.update {
            it.copy(
                attemptSelectorUiState = it.attemptSelectorUiState.copy(
                    show = true,
                    onDismiss = ::dismissAttemptSelector
                ),
                toolsBottomSheetUiState = it.toolsBottomSheetUiState.copy(show = false)
            )
        }
    }

    private fun dismissAttemptSelector() {
        _uiState.update {
            it.copy(
                attemptSelectorUiState = it.attemptSelectorUiState.copy(
                    show = false
                )
            )
        }
    }

    private fun openComments() {
        _uiState.update {
            it.copy(
                openCommentsBottomSheetParams = OpenCommentsBottomSheetParams(assignmentId, courseId),
                toolsBottomSheetUiState = it.toolsBottomSheetUiState.copy(show = false)
            )
        }
    }

    private fun dismissComments() {
        _uiState.update {
            it.copy(openCommentsBottomSheetParams = null)
        }
    }

    private fun formatScore(value: Double): String {
        val formatter = NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
            isGroupingUsed = false
        }
        return formatter.format(value)
    }

    private fun onAssignmentUpdatedForAddSubmission() {
        _assignmentFlow.value = null
    }
}