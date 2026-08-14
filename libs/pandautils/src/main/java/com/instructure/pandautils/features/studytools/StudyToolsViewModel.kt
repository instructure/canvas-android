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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.domain.models.studytools.StudyToolsError
import com.instructure.pandautils.domain.models.studytools.StudyToolsOutcome
import com.instructure.pandautils.domain.usecase.studytools.GetStudyToolsUseCase
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
    private val networkStateProvider: NetworkStateProvider
) : ViewModel() {

    private val canvasContext: CanvasContext = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) ?: CanvasContext.emptyUserContext()

    private val _uiState = MutableStateFlow(StudyToolsUiState().withCallbacks())
    val uiState = _uiState.asStateFlow()

    /** Set once by [onPageResolved]; the page slug is not available at construction time. */
    private var pageUrl: String? = null

    /** Tools resolved by the availability check, reused when the panel opens. */
    private var availableTools: List<StudyToolOption>? = null

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

    private fun onStudyToolSelected(tool: StudyToolOption) = updateStudyTools { it.copy(selectedTool = tool) }

    private fun onStudyToolsBack() = updateStudyTools { it.copy(selectedTool = null) }

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
            onAiDisclosureClick = ::onAiDisclosureClick,
            onAiDisclosureDismiss = ::onAiDisclosureDismiss
        )
    )
}
