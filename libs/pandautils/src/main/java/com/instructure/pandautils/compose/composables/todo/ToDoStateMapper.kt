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

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.getContextNameForPlannerItem
import com.instructure.pandautils.utils.getDateTextForPlannerItem
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.utils.getTagForPlannerItem
import com.instructure.pandautils.utils.getUrl
import com.instructure.pandautils.utils.isComplete
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ToDoStateMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs
) {

    fun mapToUiState(
        plannerItem: PlannerItem,
        courseMap: Map<Long, Course>,
        onSwipeToDone: () -> Unit,
        onCheckboxToggle: (Boolean) -> Unit
    ): ToDoItemUiState {
        val itemType = when (plannerItem.plannableType) {
            PlannableType.ASSIGNMENT -> ToDoItemType.ASSIGNMENT
            PlannableType.SUB_ASSIGNMENT -> ToDoItemType.SUB_ASSIGNMENT
            PlannableType.QUIZ -> ToDoItemType.QUIZ
            PlannableType.DISCUSSION_TOPIC -> ToDoItemType.DISCUSSION
            PlannableType.CALENDAR_EVENT -> ToDoItemType.CALENDAR_EVENT
            PlannableType.PLANNER_NOTE -> ToDoItemType.PLANNER_NOTE
            else -> ToDoItemType.CALENDAR_EVENT
        }

        val itemId = plannerItem.plannable.id.toString()

        // Account-level calendar events should not be clickable
        val isAccountLevelEvent = plannerItem.contextType?.equals("Account", ignoreCase = true) == true
        val isClickable = !(isAccountLevelEvent && itemType == ToDoItemType.CALENDAR_EVENT)

        return ToDoItemUiState(
            id = itemId,
            title = plannerItem.plannable.title,
            date = plannerItem.plannableDate,
            dateLabel = plannerItem.getDateTextForPlannerItem(context),
            contextLabel = plannerItem.getContextNameForPlannerItem(context, courseMap.values),
            canvasContext = plannerItem.canvasContext,
            itemType = itemType,
            isChecked = plannerItem.isComplete(),
            iconRes = plannerItem.getIconForPlannerItem(),
            tag = plannerItem.getTagForPlannerItem(context),
            htmlUrl = plannerItem.getUrl(apiPrefs),
            isClickable = isClickable,
            onSwipeToDone = onSwipeToDone,
            onCheckboxToggle = onCheckboxToggle
        )
    }
}