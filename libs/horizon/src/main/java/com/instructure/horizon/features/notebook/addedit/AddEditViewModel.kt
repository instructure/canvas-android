/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.notebook.addedit

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.database.entity.SyncDataType
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.offline.HorizonOfflineViewModel
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class AddEditViewModel(
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    getLastSyncedAtUseCase: GetLastSyncedAtUseCase,
) : HorizonOfflineViewModel(networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase) {
    protected val _uiState = MutableStateFlow(
        AddEditNoteUiState(
            onTypeChanged = ::onTypeChanged,
            onUserCommentChanged = ::onUserCommentChanged,
            onSnackbarDismiss = ::onSnackbarDismissed,
            updateDeleteConfirmationDialog = ::updateShowDeleteConfirmationDialog,
            updateExitConfirmationDialog = ::updateShowExitConfirmationDialog,
            isOffline = isOffline(),
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (isOffline()) {
            viewModelScope.tryLaunch {
                _uiState.update {
                    it.copy(
                        isOffline = true,
                        lastSyncedAtMs = getLastSyncTime(SyncDataType.NOTES),
                    )
                }
            } catch { }
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

    override fun onNetworkRestored() {
        _uiState.update { it.copy(isOffline = false, lastSyncedAtMs = null) }
    }

    override fun onNetworkLost() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    isOffline = true,
                    lastSyncedAtMs = getLastSyncTime(SyncDataType.NOTES),
                )
            }
        } catch {
            _uiState.update { it.copy(isOffline = true, lastSyncedAtMs = null) }
        }
    }
}
