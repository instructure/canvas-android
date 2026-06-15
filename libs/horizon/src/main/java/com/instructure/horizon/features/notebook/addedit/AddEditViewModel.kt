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
package com.instructure.horizon.features.notebook.addedit

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.database.entity.SyncDataType
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.offline.HorizonOfflineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

abstract class AddEditViewModel(
    protected val offlineState: HorizonOfflineState,
    private val getLastSyncedAtUseCase: GetLastSyncedAtUseCase,
) : ViewModel() {
    protected val _uiState = MutableStateFlow(
        AddEditNoteUiState(
            onTypeChanged = ::onTypeChanged,
            onUserCommentChanged = ::onUserCommentChanged,
            onSnackbarDismiss = ::onSnackbarDismissed,
            updateDeleteConfirmationDialog = ::updateShowDeleteConfirmationDialog,
            updateExitConfirmationDialog = ::updateShowExitConfirmationDialog,
            isOffline = offlineState.isOffline.value,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        offlineState.isOffline
            .onEach { isOffline -> handleOfflineStateChange(isOffline) }
            .launchIn(viewModelScope)
    }

    private fun handleOfflineStateChange(isOffline: Boolean) {
        if (isOffline) {
            viewModelScope.tryLaunch {
                _uiState.update {
                    it.copy(
                        isOffline = true,
                        lastSyncedAtMs = getLastSyncedAtUseCase(GetLastSyncedAtUseCase.Params(SyncDataType.NOTES)),
                    )
                }
            } catch {
                _uiState.update { it.copy(isOffline = true, lastSyncedAtMs = null) }
            }
        } else {
            _uiState.update { it.copy(isOffline = false, lastSyncedAtMs = null) }
        }
    }

    private fun onTypeChanged(newType: NotebookType?) {
        _uiState.update { it.copy(type = newType,) }
        _uiState.update { it.copy(hasContentChange = hasContentChange()) }
    }

    private fun onUserCommentChanged(userComment: TextFieldValue) {
        _uiState.update { it.copy(userComment = userComment) }
        _uiState.update { it.copy(hasContentChange = hasContentChange()) }
    }

    private fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun updateShowExitConfirmationDialog(value: Boolean) {
        _uiState.update { it.copy(showExitConfirmationDialog = value) }
    }

    private fun updateShowDeleteConfirmationDialog(value: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmationDialog = value) }
    }

    protected abstract fun hasContentChange(): Boolean
}
