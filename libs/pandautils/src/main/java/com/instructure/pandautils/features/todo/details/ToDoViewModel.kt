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

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.R
import com.instructure.pandautils.features.todo.details.ToDoFragment.Companion.PLANNER_ITEM
import com.instructure.pandautils.utils.backgroundColor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class ToDoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToDoUiState())
    val uiState = _uiState.asStateFlow()

    private val plannerItem: PlannerItem? = savedStateHandle.get<PlannerItem>(PLANNER_ITEM)

    init {
        loadData()
    }

    private fun loadData() {
        plannerItem?.let { plannerItem ->
            val dateText = plannerItem.plannable.todoDate.toDate()?.let {
                val dateText = DateHelper.dayMonthYearFormat.format(it)
                val timeText = DateHelper.getFormattedTime(context, it)
                context.getString(R.string.calendarDate, dateText, timeText)
            }

            _uiState.update {
                it.copy(
                    title = plannerItem.plannable.title,
                    contextName = plannerItem.contextName,
                    contextColor = plannerItem.canvasContext.backgroundColor,
                    date = dateText.orEmpty(),
                    description = plannerItem.plannable.details.orEmpty()
                )
            }
        }
    }
}
