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
package com.instructure.pandautils.features.studytools

import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.domain.models.studytools.StudyToolsError
import com.instructure.pandautils.domain.models.studytools.StudyToolsOutcome
import com.instructure.pandautils.domain.usecase.studytools.GetStudyToolsUseCase
import com.instructure.pandautils.domain.usecase.studytools.RunStudyToolUseCase
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns both the toolbar icon's availability and the panel's own state, so [PageDetailsViewModel]
 * stays entirely Study-Tools-free. Scoped to the hosting Fragment (injected there via
 * `by viewModels()`, alongside [PageDetailsViewModel], and passed down to `PageDetailsScreen` as
 * plain state - not resolved with `hiltViewModel()` inside the composable, so the screen stays
 * testable without a Hilt test harness), so it outlives the panel being opened and closed - see
 * [resetPanel].
 */
@HiltViewModel
class StudyToolsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStudyToolsUseCase: GetStudyToolsUseCase,
    private val runStudyToolUseCase: RunStudyToolUseCase,
    private val networkStateProvider: NetworkStateProvider,
    private val resources: Resources,
    private val analytics: Analytics
) : ViewModel() {

    private val canvasContext: CanvasContext = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) ?: CanvasContext.emptyUserContext()

    private val _uiState = MutableStateFlow(StudyToolsUiState().withCallbacks())
    val uiState = _uiState.asStateFlow()

    /** Set once by [onPageResolved]; the page slug is not available at construction time. */
    private var pageUrl: String? = null

    /** Tools resolved by the availability check, reused when the panel opens. */
    private var availableTools: List<StudyToolOption>? = null

    /** The in-flight generation, cancelled whenever the user leaves the tool it belongs to. */
    private var studyToolJob: Job? = null

    /** The in-flight menu list load, cancelled on dismiss and replaced by any newer retry. */
    private var loadStudyToolsJob: Job? = null

    /** True only while an availability check is blocked on connectivity, so it can be retried once online. */
    private var pendingAvailabilityCheck = false

    init {
        // Retries the availability check once connectivity returns, so a page opened offline is not
        // stuck without the entry point for the rest of the fragment's lifetime.
        viewModelScope.launch {
            networkStateProvider.isOnlineLiveData.asFlow()
                .filter { isOnline -> isOnline && pendingAvailabilityCheck }
                .collect { pageUrl?.let(::checkStudyToolsAvailability) }
        }
    }

    /**
     * Called once the hosting page has resolved its slug (front pages and pages routed with a
     * parcelable [com.instructure.canvasapi2.models.Page] do not carry it in the nav args). Kicks
     * off the availability check; a no-op on any call after the first, since the slug for a given
     * panel instance never changes.
     */
    fun onPageResolved(url: String) {
        if (pageUrl != null) return
        pageUrl = url
        checkStudyToolsAvailability(url)
    }

    /**
     * Asks the server whether this page has any study tools, and keeps the list so opening the
     * panel does not repeat the request. A failure simply leaves the icon hidden — Study Tools is
     * additive, so it must never surface an error on the page itself.
     */
    private fun checkStudyToolsAvailability(url: String) {
        val courseId = (canvasContext as? Course)?.id ?: return
        if (!networkStateProvider.isOnline()) {
            pendingAvailabilityCheck = true
            return
        }
        pendingAvailabilityCheck = false

        viewModelScope.launch {
            val outcome = getStudyToolsUseCase(GetStudyToolsUseCase.Params(courseId, url))
            val tools = (outcome as? StudyToolsOutcome.Success)?.data?.takeIf { it.isNotEmpty() }
            availableTools = tools
            if (tools != null) {
                _uiState.update { it.copy(available = true) }
            }
        }
    }

    private fun onOpen() {
        // Offline is checked before the cache: a tool list we cannot actually run is worse than
        // saying plainly that there is no connection.
        val cached = availableTools?.takeIf { networkStateProvider.isOnline() }

        // When the availability check already fetched the list, the panel opens straight into its
        // content instead of repeating an identical request.
        val initialState = cached?.let { StudyToolsScreenState.Content(it) }
            ?: StudyToolsScreenState.Loading

        _uiState.update { it.copy(panelVisible = true, screenState = initialState) }

        if (cached == null) loadStudyTools()
    }

    private fun onDismiss() {
        studyToolJob?.cancel()
        loadStudyToolsJob?.cancel()
        resetPanel()
    }

    /**
     * The VM outlives a panel close, so closing it must explicitly clear what a fresh open should
     * not inherit, while keeping the [availableTools] cache and [StudyToolsUiState.available] flag.
     */
    private fun resetPanel() {
        _uiState.update {
            it.copy(
                panelVisible = false,
                selectedTool = null,
                toolState = null,
                aiDisclosureVisible = false
            )
        }
    }

    private fun loadStudyTools() {
        val courseId = (canvasContext as? Course)?.id
        val url = pageUrl
        if (courseId == null || url == null) {
            updateStudyTools { it.copy(screenState = StudyToolsScreenState.NoToolsAvailable) }
            return
        }

        // Checked up front so an offline device shows a clear state instead of a failed request.
        if (!networkStateProvider.isOnline()) {
            updateStudyTools { it.copy(screenState = StudyToolsScreenState.Offline) }
            return
        }

        updateStudyTools { it.copy(screenState = StudyToolsScreenState.Loading) }

        // Cancelled and replaced rather than left to race: retry spam on a slow request would
        // otherwise let an older response overwrite a newer one, in whichever order they land.
        loadStudyToolsJob?.cancel()
        loadStudyToolsJob = viewModelScope.launch {
            val outcome = getStudyToolsUseCase(GetStudyToolsUseCase.Params(courseId, url))
            availableTools = (outcome as? StudyToolsOutcome.Success)?.data?.takeIf { it.isNotEmpty() }
            updateStudyTools { it.copy(screenState = outcome.toStudyToolsScreenState()) }
        }
    }

    private fun StudyToolsOutcome<List<StudyToolOption>>.toStudyToolsScreenState() = when (this) {
        is StudyToolsOutcome.Success -> if (data.isEmpty()) {
            StudyToolsScreenState.NoToolsAvailable
        } else {
            StudyToolsScreenState.Content(data)
        }

        is StudyToolsOutcome.Failure -> when (val e = error) {
            is StudyToolsError.Unavailable -> StudyToolsScreenState.NoToolsAvailable
            else -> StudyToolsScreenState.Error(e.message, e.header, retryable = e is StudyToolsError.TemporarilyUnavailable)
        }
    }

    private fun onStudyToolSelected(tool: StudyToolOption) {
        // A generation for the tool being left behind is of no use to anyone.
        studyToolJob?.cancel()
        updateStudyTools { it.copy(selectedTool = tool, toolState = null) }
        runStudyTool(tool, regenerate = false)
    }

    private fun onStudyToolsBack() {
        studyToolJob?.cancel()
        updateStudyTools { it.copy(selectedTool = null, toolState = null) }
    }

    /** Always regenerates: a plain repeat of the request returns the result the server cached for 24h. */
    private fun onStudyToolRegenerate() = rerunSelectedTool(regenerate = true)

    /** Re-attempts a generation that produced nothing, leaving the server's cache in place. */
    private fun onStudyToolRetry() = rerunSelectedTool(regenerate = false)

    private fun rerunSelectedTool(regenerate: Boolean) {
        val tool = _uiState.value.selectedTool ?: return
        runStudyTool(tool, regenerate)
    }

    private fun runStudyTool(tool: StudyToolOption, regenerate: Boolean) {
        // Whatever the user is currently reading, kept so a failed regenerate reports itself instead of
        // taking their content away.
        val previous = (_uiState.value.toolState as? StudyToolState.Ready)
            ?.copy(regenerateError = null)

        fun fail(message: String?, header: String? = null, retryable: Boolean = false) {
            // retryable is dropped here on purpose: the Regenerate link is already the retry
            // affordance for this path. header is folded into the message instead of being dropped
            // too, so the same server response reads the same way as it does on a first generation.
            val regenerateError = listOfNotNull(header, message)
                .joinToString(" ")
                .ifEmpty { null } ?: genericStudyToolsError()
            val state = previous?.copy(regenerateError = regenerateError)
                ?: StudyToolState.Error(message, header, retryable)
            updateStudyTools { it.copy(toolState = state) }
        }

        val courseId = (canvasContext as? Course)?.id
        val url = pageUrl
        if (courseId == null || url == null) {
            fail(message = null)
            return
        }

        if (!networkStateProvider.isOnline()) {
            if (previous != null) {
                fail(message = resources.getString(R.string.studyTools_offline))
            } else {
                updateStudyTools { it.copy(toolState = StudyToolState.Offline) }
            }
            return
        }

        updateStudyTools { it.copy(toolState = StudyToolState.Generating) }

        studyToolJob?.cancel()
        studyToolJob = viewModelScope.launch {
            val outcome = runStudyToolUseCase(
                RunStudyToolUseCase.Params(
                    courseId = courseId,
                    pageUrl = url,
                    tool = tool.tool,
                    prompt = tool.prompt,
                    regenerate = regenerate
                )
            )

            // A result for a tool the user has already moved on from must not replace what they are
            // looking at now.
            if (_uiState.value.selectedTool != tool) return@launch

            when (outcome) {
                is StudyToolsOutcome.Success ->
                    updateStudyTools { it.copy(toolState = StudyToolState.Ready(outcome.data)) }

                // The repository has already exhausted its automatic 502 retries, so a manual one is the
                // last resort rather than a duplicate.
                is StudyToolsOutcome.Failure -> fail(
                    message = outcome.error.message,
                    header = outcome.error.header,
                    retryable = outcome.error is StudyToolsError.TemporarilyUnavailable
                )
            }
        }
    }

    private fun genericStudyToolsError(): String = resources.getString(R.string.studyTools_genericError)

    private fun onStudyToolFeedback(feedback: StudyToolsFeedback) {
        val ready = _uiState.value.toolState as? StudyToolState.Ready ?: return
        // Re-tapping the vote already cast would double-count it in Pendo.
        if (ready.feedback == feedback) return

        // Pendo-only by design: there is no feedback endpoint, so this cannot fail and has no retry.
        analytics.logEvent(
            AnalyticsEventConstants.STUDY_ASSIST_FEEDBACK,
            Bundle().apply { putString(AnalyticsParamConstants.VOTE, feedback.vote) }
        )

        // Re-read inside the update so the vote is applied to the current content, never to the copy
        // captured above.
        updateStudyTools { state ->
            val current = state.toolState as? StudyToolState.Ready ?: return@updateStudyTools state
            state.copy(toolState = current.copy(feedback = feedback))
        }
    }

    private fun onAiDisclosureClick() = updateStudyTools { it.copy(aiDisclosureVisible = true) }

    private fun onAiDisclosureDismiss() = updateStudyTools { it.copy(aiDisclosureVisible = false) }

    /** No-ops when the panel is closed, so a late callback can never resurrect it. */
    private fun updateStudyTools(block: (StudyToolsUiState) -> StudyToolsUiState) {
        _uiState.update { state -> if (state.panelVisible) block(state) else state }
    }

    private fun StudyToolsUiState.withCallbacks(): StudyToolsUiState = copy(
        actions = StudyToolsActions(
            onOpen = ::onOpen,
            onDismiss = ::onDismiss,
            onToolSelected = ::onStudyToolSelected,
            onBack = ::onStudyToolsBack,
            onRetry = ::loadStudyTools,
            onRegenerate = ::onStudyToolRegenerate,
            onToolRetry = ::onStudyToolRetry,
            onFeedback = ::onStudyToolFeedback,
            onAiDisclosureClick = ::onAiDisclosureClick,
            onAiDisclosureDismiss = ::onAiDisclosureDismiss
        )
    )
}
