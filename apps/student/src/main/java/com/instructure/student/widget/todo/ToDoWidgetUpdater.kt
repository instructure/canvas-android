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

package com.instructure.student.widget.todo

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.courseOrUserColor
import com.instructure.pandautils.utils.filterByToDoFilters
import com.instructure.pandautils.utils.getContextNameForPlannerItem
import com.instructure.pandautils.utils.getDateTextForPlannerItem
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.utils.getTagForPlannerItem
import com.instructure.pandautils.utils.getUrl
import com.instructure.pandautils.utils.isComplete
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.student.widget.glance.WidgetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


private const val PLANNER_DATE_RANGE_DAYS = 28L

class ToDoWidgetUpdater(
    private val repository: ToDoWidgetRepository,
    private val apiPrefs: ApiPrefs
) {
    fun updateData(context: Context, forceNetwork: Boolean = true): Flow<ToDoWidgetUiState> {
        return flow {
            emit(ToDoWidgetUiState(WidgetState.Loading))

            val user = apiPrefs.user
            if (user == null) {
                emit(ToDoWidgetUiState(WidgetState.NotLoggedIn))
                return@flow
            }

            try {
                val courses = repository.getCourses(forceNetwork)
                val todoFilters = repository.getToDoFilters()

                val startDate = todoFilters.pastDateRange.calculatePastDateRange().toApiString()
                val endDate = todoFilters.futureDateRange.calculateFutureDateRange().toApiString()

                val plannerItemsDataResult = repository.getPlannerItems(startDate, endDate, forceNetwork)

                if (plannerItemsDataResult is DataResult.Fail && plannerItemsDataResult.failure is Failure.Authorization) {
                    emit(ToDoWidgetUiState(WidgetState.NotLoggedIn))
                    return@flow
                }
                // Other errors are handled in catch
                val plannerItems = plannerItemsDataResult.dataOrThrow
                    .distinctBy { it.id }
                    .filterByToDoFilters(todoFilters, courses)
                    .filter { !it.isComplete() }
                    .sortedBy { it.comparisonDate }

                val toDoWidgetUiState = ToDoWidgetUiState(
                    if (plannerItems.isEmpty()) {
                        WidgetState.Empty
                    } else {
                        WidgetState.Content
                    },
                    plannerItems.map {
                        it.toWidgetPlannerItem(context, courses)
                    }
                )
                emit(toDoWidgetUiState)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(ToDoWidgetUiState(WidgetState.Error))
            }
        }
    }

    private fun PlannerItem.toWidgetPlannerItem(
        context: Context,
        courses: List<Course>
    ) = WidgetPlannerItem(
        date = plannableDate.toLocalDate(),
        iconRes = getIconForPlannerItem(),
        canvasContextColor = canvasContext.courseOrUserColor,
        canvasContextText = getContextNameForPlannerItem(context, courses),
        title = plannable.title,
        dateText = getDateTextForPlannerItem(context).orEmpty(),
        url = getUrl(apiPrefs),
        tag = getTagForPlannerItem(context)
    )
}
