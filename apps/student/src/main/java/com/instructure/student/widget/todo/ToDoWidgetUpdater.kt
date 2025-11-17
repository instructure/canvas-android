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
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.courseOrUserColor
import com.instructure.pandautils.utils.getContextNameForPlannerItem
import com.instructure.pandautils.utils.getDateTextForPlannerItem
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.utils.getTagForPlannerItem
import com.instructure.pandautils.utils.getUrl
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.student.widget.glance.WidgetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.threeten.bp.LocalDate


private const val PLANNER_DATE_RANGE_DAYS = 28L

class ToDoWidgetUpdater(
    private val repository: ToDoWidgetRepository,
    private val apiPrefs: ApiPrefs
) {
    suspend fun updateData(context: Context): Flow<ToDoWidgetUiState> {
        return flow {
            emit(ToDoWidgetUiState(WidgetState.Loading))

            val user = apiPrefs.user
            if (user == null) {
                emit(ToDoWidgetUiState(WidgetState.NotLoggedIn))
                return@flow
            }

            try {
                val courses = repository.getCourses(true)
                val calendarFilters = repository.getCalendarFilters(apiPrefs.user?.id.orDefault(), apiPrefs.fullDomain)

                val now = LocalDate.now().atStartOfDay()
                val plannerItemsDataResult = repository.getPlannerItems(
                    now.minusDays(PLANNER_DATE_RANGE_DAYS).toApiString().orEmpty(),
                    now.plusDays(PLANNER_DATE_RANGE_DAYS).toApiString().orEmpty(),
                    calendarFilters?.filters.orEmpty().toList(),
                    true
                )

                if (plannerItemsDataResult is DataResult.Fail && plannerItemsDataResult.failure is Failure.Authorization) {
                    emit(ToDoWidgetUiState(WidgetState.NotLoggedIn))
                    return@flow
                }
                // Other errors are handled in catch
                val plannerItems = plannerItemsDataResult.dataOrThrow
                    .filter { it.plannerOverride?.markedComplete != true }
                    .filter { !isComplete(it) }

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

    private fun isComplete(plannerItem: PlannerItem): Boolean {
        return if (plannerItem.plannableType == PlannableType.ASSIGNMENT
            || plannerItem.plannableType == PlannableType.DISCUSSION_TOPIC
            || plannerItem.plannableType == PlannableType.SUB_ASSIGNMENT
        ) {
            plannerItem.submissionState?.submitted == true
        } else {
            false
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
