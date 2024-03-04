/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.pandautils.utils.poll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val progressApi: ProgressAPI.ProgressInterface,
    private val progressPreferences: ProgressPreferences
) : ViewModel() {

    private val progressId = stateHandle.get<Long>(ProgressDialogFragment.PROGRESS_ID) ?: -1
    private val title = stateHandle.get<String>(ProgressDialogFragment.TITLE) ?: ""
    private val progressTitle = stateHandle.get<String>(ProgressDialogFragment.PROGRESS_TITLE) ?: ""
    private val note = stateHandle.get<String>(ProgressDialogFragment.NOTE)

    private val _uiState = MutableStateFlow(
        ProgressUiState(
            title = title,
            progressTitle = progressTitle,
            progress = 0f,
            note = note,
            state = ProgressState.QUEUED,
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ProgressViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        val params = RestParams(isForceReadFromNetwork = true)
        try {
            poll(500, -1, block = {
                val progress = progressApi.getProgress(progressId.toString(), params).dataOrThrow
                val newState = when {
                    progress.isQueued -> ProgressState.QUEUED
                    progress.isRunning -> ProgressState.RUNNING
                    progress.isCompleted -> ProgressState.COMPLETED
                    progress.isFailed -> ProgressState.FAILED
                    else -> ProgressState.QUEUED
                }
                _uiState.emit(_uiState.value.copy(progress = progress.completion, state = newState))
                progress
            },
                validate = {
                    it.hasRun
                })
        } catch (e: Exception) {
            _uiState.emit(_uiState.value.copy(state = ProgressState.FAILED))
        }
    }

    fun handleAction(action: ProgressAction) {
        viewModelScope.launch {
            when (action) {
                is ProgressAction.Cancel -> {
                    cancel()
                }

                is ProgressAction.Close -> {
                    _events.send(ProgressViewModelAction.Close)
                }
            }
        }
    }

    private suspend fun cancel() {
        progressPreferences.cancelledProgressIds = progressPreferences.cancelledProgressIds + progressId
        val params = RestParams(isForceReadFromNetwork = true)
        progressApi.cancelProgress(progressId.toString(), params).dataOrNull
        _events.send(ProgressViewModelAction.Close)
    }

}