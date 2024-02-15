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
 */
package com.instructure.pandautils.features.todo.details

import org.threeten.bp.LocalDate

data class ToDoUiState(
    val title: String = "",
    val contextName: String? = null,
    val contextColor: Int? = null,
    val date: String = "",
    val description: String = "",
    val deleting: Boolean = false,
    val errorSnack: String? = null
)

sealed class ToDoAction {
    data object DeleteToDo : ToDoAction()
    data object SnackbarDismissed : ToDoAction()
}

sealed class ToDoViewModelAction {
    data class RefreshCalendarDay(val date: LocalDate) : ToDoViewModelAction()
}