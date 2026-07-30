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
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetAssignmentDetailsUseCase
import com.instructure.horizon.domain.usecase.GetSubmissionHistoryUseCase
import com.instructure.horizon.domain.usecase.GetUnreadCommentsCountUseCase
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContextSource
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.HorizonSubmissionHelper
import com.instructure.horizon.horizonui.organisms.cards.AttemptCardState
import com.instructure.horizon.horizonui.organisms.cards.AttemptPillType
import com.instructure.horizon.util.formatScore
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.SubmissionState
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.localisedFormat
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAssignmentDetailsUseCase: GetAssignmentDetailsUseCase,
    private val getSubmissionHistoryUseCase: GetSubmissionHistoryUseCase,
    private val getUnreadCommentsCountUseCase: GetUnreadCommentsCountUseCase,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val oAuthApi: OAuthAPI.OAuthInterface,
    private val apiPrefs: ApiPrefs,
    private val aiAssistContextProvider: AiAssistContextProvider,
    private val networkStateProvider: NetworkStateProvider,
    private val createSubmissionDao: CreateSubmissionDao,
    private val createFileSubmissionDao: CreateFileSubmissionDao,
    private val submissionHelper: HorizonSubmissionHelper,
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

    private var loadedAssignment: Assignment? = null
    private var loadedAttempts: List<Submission> = emptyList()
    private var lastRealAttemptNumber: Long = 0L

    init {
        loadData()
        observePendingSubmission()
    }

    private fun observePendingSubmission() {
        val userId = apiPrefs.user?.id.orDefault()
        val textFlow = createSubmissionDao.findSubmissionByAssignmentIdAndTypeFlow(
            assignmentId, userId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString
        )
        val fileFlow = createSubmissionDao.findSubmissionByAssignmentIdAndTypeFlow(
            assignmentId, userId, Assignment.SubmissionType.ONLINE_UPLOAD.apiString
        )
        viewModelScope.launch {
            combine(textFlow, fileFlow) { text, file ->
                listOfNotNull(text, file).firstOrNull { it.isScheduled() || it.submissionState == SubmissionState.COMPLETED }
            }.collect { entity ->
                handlePendingSubmission(entity)
            }
        }
    }

    /**
     * Reacts to changes of the local (offline) submission queue for this assignment.
     * An offline-originated submission is surfaced as a synthetic "queued attempt" that is selectable
     * from the attempt picker and shown inline as the selected attempt. Online submissions keep the
     * legacy inline pending block. On success the queued attempt is silently folded into the real
     * submission history; on cancel it is reverted to an editable draft and disappears.
     */
    private suspend fun handlePendingSubmission(entity: CreateSubmissionEntity?) {
        when {
            entity == null -> onPendingSubmissionCleared()

            entity.submissionState == SubmissionState.COMPLETED -> {
                // The worker marks the row COMPLETED just before deleting it. If this was an offline
                // queued attempt, silently refresh so it becomes a real attempt (no success modal).
                val wasQueuedAttempt = uiState.value.submissionDetailsUiState.pendingAttemptNumber > 0L
                _uiState.update { it.copy(pendingSubmission = null) }
                if (wasQueuedAttempt) {
                    updateAssignment(showConfirmation = false)
                }
            }

            else -> {
                val pending = buildPendingSubmission(entity)
                val alreadyQueuedAttempt = uiState.value.submissionDetailsUiState.pendingAttemptNumber > 0L
                if (alreadyQueuedAttempt || !networkStateProvider.isOnline()) {
                    showQueuedAttempt(pending)
                } else {
                    // Online: keep the legacy inline pending block behavior unchanged.
                    _uiState.update { it.copy(pendingSubmission = pending) }
                }
            }
        }
    }

    private fun showQueuedAttempt(pending: PendingSubmissionUiState) {
        val assignment = loadedAssignment ?: return
        val pendingAttemptNumber = lastRealAttemptNumber + 1
        _uiState.update { state ->
            val firstEstablish = state.submissionDetailsUiState.pendingAttemptNumber <= 0L
            val selectedAttempt = if (firstEstablish) pendingAttemptNumber else state.submissionDetailsUiState.currentSubmissionAttempt
            val queuedCard = AttemptCardState(
                attemptNumber = pendingAttemptNumber,
                attemptTitle = context.getString(R.string.assignmentDetails_attemptNumber, pendingAttemptNumber),
                date = context.getString(R.string.notSubmittedStatusLabel),
                pillText = queuedPillText(pending.state),
                pillType = queuedPillType(pending.state),
                selected = selectedAttempt == pendingAttemptNumber,
                onClick = ::selectQueuedAttempt
            )
            val realCards = createAttemptCardsState(loadedAttempts, assignment, selectedAttempt)
            state.copy(
                pendingSubmission = pending,
                showSubmissionDetails = true,
                showAddSubmission = false,
                submissionDetailsUiState = state.submissionDetailsUiState.copy(
                    currentSubmissionAttempt = selectedAttempt,
                    pendingAttemptNumber = pendingAttemptNumber
                ),
                attemptSelectorUiState = state.attemptSelectorUiState.copy(attempts = listOf(queuedCard) + realCards)
            )
        }
    }

    private fun selectQueuedAttempt() {
        _uiState.update { state ->
            val pendingAttemptNumber = state.submissionDetailsUiState.pendingAttemptNumber
            state.copy(
                showSubmissionDetails = true,
                showAddSubmission = false,
                submissionDetailsUiState = state.submissionDetailsUiState.copy(currentSubmissionAttempt = pendingAttemptNumber),
                attemptSelectorUiState = state.attemptSelectorUiState.copy(
                    attempts = state.attemptSelectorUiState.attempts.map { it.copy(selected = it.attemptNumber == pendingAttemptNumber) }
                ),
                viewingAttemptText = null
            )
        }
        dismissAttemptSelector()
    }

    private fun onPendingSubmissionCleared() {
        _uiState.update { state ->
            if (state.pendingSubmission == null && state.submissionDetailsUiState.pendingAttemptNumber <= 0L) return@update state
            val assignment = loadedAssignment
            val hasRealSubmission = lastRealAttemptNumber > 0L
            val realCards = if (assignment != null) createAttemptCardsState(loadedAttempts, assignment, lastRealAttemptNumber) else emptyList()
            state.copy(
                pendingSubmission = null,
                showSubmissionDetails = hasRealSubmission,
                showAddSubmission = !hasRealSubmission,
                submissionDetailsUiState = state.submissionDetailsUiState.copy(
                    currentSubmissionAttempt = if (hasRealSubmission) lastRealAttemptNumber else -1L,
                    pendingAttemptNumber = -1L
                ),
                attemptSelectorUiState = state.attemptSelectorUiState.copy(attempts = realCards),
                viewingAttemptText = null
            )
        }
    }

    private suspend fun refreshPendingSubmission() {
        val userId = apiPrefs.user?.id.orDefault()
        val entity = createSubmissionDao.findSubmissionsByAssignmentId(assignmentId, userId)
            .firstOrNull { it.isScheduled() }
        if (entity != null) handlePendingSubmission(entity)
    }

    private fun queuedPillText(state: SubmissionState): String? = when (state) {
        SubmissionState.QUEUED -> context.getString(R.string.queuedSubmissions_statusQueued)
        SubmissionState.UPLOADING_FILES,
        SubmissionState.SUBMITTING,
        SubmissionState.VERIFYING,
        SubmissionState.RETRYING -> context.getString(R.string.queuedSubmissions_statusSubmitting)
        SubmissionState.FAILED -> context.getString(R.string.queuedSubmissions_statusFailed)
        else -> null
    }

    private fun queuedPillType(state: SubmissionState): AttemptPillType = when (state) {
        SubmissionState.FAILED -> AttemptPillType.DANGER
        SubmissionState.QUEUED -> AttemptPillType.DEFAULT
        else -> AttemptPillType.INSTITUTION
    }

    private fun CreateSubmissionEntity.isScheduled(): Boolean {
        return submissionState == SubmissionState.FAILED || (!isDraft && submissionState.isActive)
    }

    private suspend fun buildPendingSubmission(entity: CreateSubmissionEntity): PendingSubmissionUiState {
        val type = if (entity.submissionType == Assignment.SubmissionType.ONLINE_UPLOAD.apiString) {
            Assignment.SubmissionType.ONLINE_UPLOAD
        } else {
            Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        }
        val fileNames = if (type == Assignment.SubmissionType.ONLINE_UPLOAD) {
            createFileSubmissionDao.findFilesForSubmissionId(entity.id).mapNotNull { it.name }
        } else {
            emptyList()
        }
        // Force-submit is only offered while the upload hasn't started yet and the learner is back online.
        val showSubmit = entity.submissionState == SubmissionState.QUEUED && networkStateProvider.isOnline()
        return PendingSubmissionUiState(
            state = entity.submissionState,
            submissionType = type,
            text = entity.submissionEntry.takeIf { type == Assignment.SubmissionType.ONLINE_TEXT_ENTRY },
            fileNames = fileNames,
            showSubmitButton = showSubmit,
            onSubmit = { viewModelScope.launch { submissionHelper.submitScheduledSubmission(entity) } },
            onRetry = { viewModelScope.launch { submissionHelper.retrySubmission(entity) } },
            onCancel = { viewModelScope.launch { submissionHelper.cancelSubmission(entity) } },
        )
    }

    private fun loadData() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
        viewModelScope.tryLaunch {
            val assignment = getAssignmentDetailsUseCase(GetAssignmentDetailsUseCase.Params(courseId, assignmentId, forceRefresh = false))
            _assignmentFlow.value = assignment
            val attempts = getSubmissionHistoryUseCase(GetSubmissionHistoryUseCase.Params(courseId, assignmentId, apiPrefs.user?.id.orDefault()))
            storeAttempts(assignment, attempts)
            val lastActualSubmission = attempts.lastGradedOrSubmitted()
            val submissions = if (lastActualSubmission != null) {
                mapSubmissions(attempts)
            } else {
                emptyList()
            }
            val initialAttempt = lastActualSubmission?.attempt ?: 0L // We need to use 0 as the initial attempt if there are no submissions
            val description = htmlContentFormatter.formatHtmlWithIframes(assignment.description.orEmpty(), courseId)

            val attemptsUiState = createAttemptCardsState(attempts, assignment, initialAttempt)
            val showAttemptSelector = assignment.allowedAttempts != 1L

            val hasUnreadComments = getUnreadCommentsCountUseCase(
                GetUnreadCommentsCountUseCase.Params(assignmentId, apiPrefs.user?.id.orDefault())
            ) > 0

            aiAssistContextProvider.aiAssistContext = AiAssistContext(
                contextString = assignment.description.orEmpty(),
                contextSources = aiAssistContextProvider.aiAssistContext.contextSources +
                    AiAssistContextSource.Assignment(assignment.id.toString())
            )

            val isOffline = !networkStateProvider.isOnline()
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false),
                    instructions = description,
                    ltiUrl = assignment.externalToolAttributes?.url.orEmpty(),
                    submissionDetailsUiState = it.submissionDetailsUiState.copy(
                        submissions = submissions,
                        currentSubmissionAttempt = initialAttempt,
                        newAttemptEnabled = isNewAttemptAllowed(assignment, initialAttempt)
                    ),
                    showSubmissionDetails = lastActualSubmission != null,
                    showAddSubmission = lastActualSubmission == null,
                    onSubmissionSuccess = { updateAssignment() },
                    attemptSelectorUiState = it.attemptSelectorUiState.copy(attempts = attemptsUiState),
                    toolsBottomSheetUiState = it.toolsBottomSheetUiState.copy(showAttemptSelector = showAttemptSelector, hasUnreadComments = hasUnreadComments),
                    isOffline = isOffline,
                )
            }
            refreshPendingSubmission()
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }
    }

    private fun storeAttempts(assignment: Assignment, attempts: List<Submission>) {
        loadedAssignment = assignment
        loadedAttempts = attempts
        lastRealAttemptNumber = attempts.lastGradedOrSubmitted()?.attempt ?: 0L
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
                val authenticatedSessionURL = oAuthApi.getAuthenticatedSession(
                    ltiUrl,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrNull?.sessionUrl ?: ltiUrl
                _uiState.update { it.copy(urlToOpen = authenticatedSessionURL) }
            } catch (e: Exception) {
                _uiState.update { it.copy(urlToOpen = ltiUrl) }
            }
        }
    }

    private fun onUrlOpened() {
        _uiState.update { it.copy(urlToOpen = null) }
    }

    private fun isNewAttemptAllowed(assignment: Assignment, currentAttempt: Long): Boolean {
        return assignment.allowedAttempts <= 0L || currentAttempt < assignment.allowedAttempts
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

    private suspend fun updateAssignment(showConfirmation: Boolean = true) {
        val assignment = getAssignmentDetailsUseCase(GetAssignmentDetailsUseCase.Params(courseId, assignmentId, forceRefresh = true))
        _assignmentFlow.value = assignment
        val attempts = getSubmissionHistoryUseCase(GetSubmissionHistoryUseCase.Params(courseId, assignmentId, apiPrefs.user?.id.orDefault(), forceRefresh = true))
        storeAttempts(assignment, attempts)
        val lastActualSubmission = attempts.lastGradedOrSubmitted()
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
                pendingSubmission = null,
                submissionDetailsUiState = it.submissionDetailsUiState.copy(
                    submissions = submissions,
                    currentSubmissionAttempt = initialAttempt,
                    pendingAttemptNumber = -1L,
                    newAttemptEnabled = isNewAttemptAllowed(assignment, initialAttempt)
                ),
                showSubmissionDetails = lastActualSubmission != null,
                showAddSubmission = lastActualSubmission == null,
                submissionConfirmationUiState = if (showConfirmation) {
                    it.submissionConfirmationUiState.copy(
                        show = true,
                        attemptCardState = currentAttempt
                    )
                } else {
                    it.submissionConfirmationUiState
                },
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

    private fun List<Submission>.lastGradedOrSubmitted(): Submission? {
        return filter { it.workflowState == "graded" || it.workflowState == "submitted" }
            .maxByOrNull { it.attempt }
    }

    private fun onAssignmentUpdatedForAddSubmission() {
        _assignmentFlow.value = null
    }
}