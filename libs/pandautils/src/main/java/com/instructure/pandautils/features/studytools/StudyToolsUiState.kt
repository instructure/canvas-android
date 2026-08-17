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
 *
 */
package com.instructure.pandautils.features.studytools

import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.domain.models.studytools.StudyToolResult

data class StudyToolsUiState(
    // Study Tools is only offered once the server confirms the course has tools for this page, so
    // the icon stays hidden entirely when the feature is off rather than opening an empty panel.
    val available: Boolean = false,
    // Whether the panel (sheet on phones, drawer on tablets) is currently open.
    val panelVisible: Boolean = false,
    val screenState: StudyToolsScreenState = StudyToolsScreenState.Loading,
    /** The tool whose content is open, or null while the menu is showing. */
    val selectedTool: StudyToolOption? = null,
    /** The generation state of [selectedTool]; null whenever no tool is open. */
    val toolState: StudyToolState? = null,
    val aiDisclosureVisible: Boolean = false,
    val actions: StudyToolsActions = StudyToolsActions()
)

sealed class StudyToolsScreenState {
    data object Loading : StudyToolsScreenState()
    data class Content(val tools: List<StudyToolOption>) : StudyToolsScreenState()

    /**
     * The feature is reachable but has nothing to offer — the course has every per-tool flag off.
     * A persistent state rather than a toast, matching web.
     */
    data object NoToolsAvailable : StudyToolsScreenState()
    data object Offline : StudyToolsScreenState()

    /**
     * [retryable] is false for failures that repeating cannot fix (the daily rate limit, an
     * unprocessable page) - those offer no retry affordance at all, matching [StudyToolState.Error].
     */
    data class Error(val message: String?, val header: String? = null, val retryable: Boolean = false) : StudyToolsScreenState()
}

/**
 * The lifecycle of a single tool run.
 */
sealed class StudyToolState {
    data object Generating : StudyToolState()
    data class Ready(
        val result: StudyToolResult,
        /** The vote already cast for this particular generation; reset whenever content is regenerated. */
        val feedback: StudyToolsFeedback? = null,
        /**
         * Why the last regenerate failed. Set instead of replacing the content with an error state, so a
         * failed regenerate never takes away the result the user was already reading. Cleared by the next
         * regenerate attempt.
         */
        val regenerateError: String? = null
    ) : StudyToolState()

    data object Offline : StudyToolState()

    /**
     * [message] and [header] come from the server and are already localized, so they are preferred
     * over local copy. [retryable] is false for failures that repeating cannot fix, such as the daily
     * rate limit — those offer no retry affordance at all.
     */
    data class Error(
        val message: String?,
        val header: String? = null,
        val retryable: Boolean = false
    ) : StudyToolState()
}

/**
 * The [vote] values are Canvas web's, so mobile and web feedback land in the same Pendo funnel.
 */
enum class StudyToolsFeedback(val vote: String) {
    LIKED("liked"),
    DISLIKED("disliked")
}

/**
 * UI → ViewModel callbacks, wired to private ViewModel functions in the state the ViewModel emits.
 */
data class StudyToolsActions(
    val onOpen: () -> Unit = {},
    val onDismiss: () -> Unit = {},
    val onToolSelected: (StudyToolOption) -> Unit = {},
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    /** Always requests fresh content — the server serves a 24h cached result otherwise. */
    val onRegenerate: () -> Unit = {},
    /**
     * Re-attempts a generation that never produced anything. Unlike [onRegenerate] it leaves the
     * server's cache alone, so recovering from an offline blip does not spend one of the day's
     * generations on content the server may already hold.
     */
    val onToolRetry: () -> Unit = {},
    val onFeedback: (StudyToolsFeedback) -> Unit = {},
    val onAiDisclosureClick: () -> Unit = {},
    val onAiDisclosureDismiss: () -> Unit = {}
)
