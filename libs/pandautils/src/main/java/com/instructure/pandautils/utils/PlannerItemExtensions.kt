/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R

fun PlannerItem.todoHtmlUrl(apiPrefs: ApiPrefs): String {
    return "${apiPrefs.fullDomain}/todos/${this.plannable.id}"
}

@DrawableRes
fun PlannerItem.getIconForPlannerItem(): Int {
    return when (this.plannableType) {
        PlannableType.ASSIGNMENT, PlannableType.SUB_ASSIGNMENT -> R.drawable.ic_assignment
        PlannableType.QUIZ -> R.drawable.ic_quiz
        PlannableType.CALENDAR_EVENT -> R.drawable.ic_calendar
        PlannableType.DISCUSSION_TOPIC -> R.drawable.ic_discussion
        PlannableType.PLANNER_NOTE -> R.drawable.ic_todo
        else -> R.drawable.ic_calendar
    }
}
