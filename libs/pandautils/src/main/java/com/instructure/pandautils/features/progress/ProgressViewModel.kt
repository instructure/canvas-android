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
import com.instructure.canvasapi2.models.Progress
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.poll
import com.instructure.pandautils.utils.retry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToLong

@HiltViewModel
class ProgressViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val progressApi: ProgressAPI.ProgressInterface
) : ViewModel() {

    private val progressId = stateHandle.get<Long>("progressId") ?: -1
    private val title = stateHandle.get<String>("title") ?: ""
    private val progressTitle = stateHandle.get<String>("progressTitle") ?: ""
    private val note = stateHandle.get<String>("note")

    private val _uiState = MutableStateFlow(
        ProgressUiState(
            title = title,
            progressTitle = progressTitle,
            progress = 0L,
            note = note,
            buttonTitle = "Cancel",
            state = ProgressState.QUEUED,
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ProgressViewModelAction>(1)
    val events = _events.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val params = RestParams(isForceReadFromNetwork = true)
        viewModelScope.launch {
            val progress = poll(500, -1, block = {
                var newProgress: Progress? = null
                retry(initialDelay = 500) {
                    newProgress = progressApi.getProgress(progressId.toString(), params).dataOrThrow
                }
                newProgress?.let {
                    val newState = when {
                        it.isQueued -> ProgressState.QUEUED
                        it.isRunning -> ProgressState.RUNNING
                        it.isCompleted -> ProgressState.COMPLETED
                        it.isFailed -> ProgressState.FAILED
                        else -> ProgressState.QUEUED
                    }
                    _uiState.emit(_uiState.value.copy(progress = it.completion.roundToLong(), state = newState))
                }
                newProgress
            },
                validate = {
                    it.hasRun
                })
        }
    }

    fun handleAction(action: ProgressAction) {
        viewModelScope.launch {
            when (action) {
                is ProgressAction.Cancel -> {

                }
                is ProgressAction.Close -> {
                    _events.send(ProgressViewModelAction.Close)
                }
            }
        }
    }

}