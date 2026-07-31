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

package com.instructure.ngc.features.assignmentdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.ngc.features.submission.domain.GetFileDraftUseCase
import com.instructure.ngc.features.submission.domain.GetMediaDraftUseCase
import com.instructure.ngc.features.submission.domain.GetTextDraftUseCase
import com.instructure.ngc.features.submission.domain.GetUrlDraftUseCase
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NGCSubmitDraftHintsUiState(
    val hasTextDraft: Boolean = false,
    val hasFileDraft: Boolean = false,
    val hasUrlDraft: Boolean = false,
    val hasMediaDraft: Boolean = false,
)

/**
 * Lightweight ViewModel that checks whether a saved draft exists for each NGC-supported
 * submission type (Text Entry and File Upload) so the submit-chooser bottom sheet can
 * show a "Continue draft" hint under the correct row.
 *
 * This is intentionally NGC-local and does not touch the shared [AssignmentDetailsViewModel]
 * or its [AssignmentDetailsUiState].
 */
@HiltViewModel
class NGCSubmitDraftHintsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTextDraftUseCase: GetTextDraftUseCase,
    private val getFileDraftUseCase: GetFileDraftUseCase,
    private val getUrlDraftUseCase: GetUrlDraftUseCase,
    private val getMediaDraftUseCase: GetMediaDraftUseCase,
) : ViewModel() {

    private val assignmentId: Long = savedStateHandle.get<Long>(Const.ASSIGNMENT_ID) ?: 0L

    private val _uiState = MutableStateFlow(NGCSubmitDraftHintsUiState())
    val uiState: StateFlow<NGCSubmitDraftHintsUiState> = _uiState.asStateFlow()

    init {
        loadDraftHints()
    }

    fun refresh() {
        loadDraftHints()
    }

    private fun loadDraftHints() {
        viewModelScope.launch {
            val textDraft = runCatching {
                getTextDraftUseCase(GetTextDraftUseCase.Params(assignmentId))
            }.getOrNull()
            val hasTextDraft = textDraft != null

            val fileDraft = runCatching {
                getFileDraftUseCase(GetFileDraftUseCase.Params(assignmentId))
            }.getOrNull()
            val hasFileDraft = fileDraft != null && fileDraft.files.isNotEmpty()

            val urlDraft = runCatching {
                getUrlDraftUseCase(GetUrlDraftUseCase.Params(assignmentId))
            }.getOrNull()
            val hasUrlDraft = urlDraft != null

            val mediaDraft = runCatching {
                getMediaDraftUseCase(GetMediaDraftUseCase.Params(assignmentId))
            }.getOrNull()
            val hasMediaDraft = mediaDraft != null

            _uiState.value = NGCSubmitDraftHintsUiState(
                hasTextDraft = hasTextDraft,
                hasFileDraft = hasFileDraft,
                hasUrlDraft = hasUrlDraft,
                hasMediaDraft = hasMediaDraft,
            )
        }
    }
}
