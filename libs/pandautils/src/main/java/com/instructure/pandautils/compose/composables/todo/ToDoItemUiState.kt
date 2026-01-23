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
package com.instructure.pandautils.compose.composables.todo

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R
import java.util.Date

data class ToDoItemUiState(
    val id: String,
    val title: String,
    val date: Date,
    val dateLabel: String?,
    val contextLabel: String,
    val canvasContext: CanvasContext,
    val itemType: ToDoItemType,
    val isChecked: Boolean = false,
    val iconRes: Int = R.drawable.ic_calendar,
    val tag: String? = null,
    val htmlUrl: String? = null,
    val isClickable: Boolean = true,
    val onSwipeToDone: () -> Unit = {},
    val onCheckboxToggle: (Boolean) -> Unit = {}
)

enum class ToDoItemType {
    ASSIGNMENT,
    SUB_ASSIGNMENT,
    QUIZ,
    DISCUSSION,
    CALENDAR_EVENT,
    PLANNER_NOTE
}