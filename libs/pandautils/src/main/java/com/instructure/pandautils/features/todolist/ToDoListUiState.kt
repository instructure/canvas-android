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

import com.instructure.pandautils.compose.composables.todo.ToDoItemUiState
import java.util.Date

data class ToDoListUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val itemsByDate: Map<Date, List<ToDoItemUiState>> = emptyMap(),
    val snackbarMessage: String? = null,
    val onSnackbarDismissed: () -> Unit = {},
    val confirmationSnackbarData: ConfirmationSnackbarData? = null,
    val onUndoMarkAsDoneUndoneAction: () -> Unit = {},
    val onMarkedAsDoneSnackbarDismissed: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val toDoCount: Int? = null,
    val onToDoCountChanged: () -> Unit = {},
    val onFiltersChanged: (Boolean) -> Unit = {},
    val isFilterApplied: Boolean = false,
    val removingItemIds: Set<String> = emptySet()
)

data class ConfirmationSnackbarData(
    val itemId: String,
    val title: String,
    val markedAsDone: Boolean
)
