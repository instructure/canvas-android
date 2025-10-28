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
package com.instructure.pandautils.features.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToDoListViewModel @Inject constructor(
    private val repository: ToDoListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToDoListUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ToDoListViewModelAction>()
    val events = _events.receiveAsFlow()

    fun handleAction(action: ToDoListActionHandler) {
        when (action) {
            is ToDoListActionHandler.ItemClicked -> {
                viewModelScope.launch {
                    _events.send(ToDoListViewModelAction.OpenToDoItem(action.itemId))
                }
            }
            is ToDoListActionHandler.Refresh -> {
                // TODO: Implement refresh
            }
            is ToDoListActionHandler.ToggleItemChecked -> {
                // TODO: Implement toggle checked
            }
            is ToDoListActionHandler.FilterClicked -> {
                // TODO: Implement filter
            }
        }
    }
}
