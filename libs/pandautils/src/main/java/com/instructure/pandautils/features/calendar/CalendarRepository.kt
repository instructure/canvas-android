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
package com.instructure.pandautils.features.calendar

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity

abstract class CalendarRepository {

    protected var canvasContexts: List<CanvasContext> = emptyList()

    abstract suspend fun getPlannerItems(
        startDate: String,
        endDate: String,
        contextCodes: List<String>,
        forceNetwork: Boolean
    ): List<PlannerItem>

    abstract suspend fun getCanvasContexts(): DataResult<Map<CanvasContext.Type, List<CanvasContext>>>

    abstract suspend fun getCalendarFilterLimit(): Int

    abstract suspend fun getCalendarFilters(): CalendarFilterEntity?

    abstract suspend fun updateCalendarFilters(calendarFilterEntity: CalendarFilterEntity)

    // This is only used in the Teacher/Parent app to map Planner Items to Plannables because we use a different API.
    // canvasContexts should already be populated before calling this function.
    protected fun List<Plannable>.toPlannerItems(): List<PlannerItem> {
        return mapNotNull { plannable ->
            // We don't need to handle group context, because we don't have groups in the Teacher/Parent app.
            val contextType = if (plannable.courseId != null) CanvasContext.Type.COURSE.apiString else CanvasContext.Type.USER.apiString
            val contextName = if (plannable.courseId != null) canvasContexts.find { it.id == plannable.courseId }?.name else null
            val plannableDate = plannable.todoDate.toDate()
            if (plannableDate == null) {
                null
            } else {
                PlannerItem(
                    courseId = plannable.courseId,
                    groupId = plannable.groupId,
                    userId = plannable.userId,
                    contextType = contextType,
                    contextName = contextName,
                    plannableType = PlannableType.PLANNER_NOTE,
                    plannable = plannable,
                    plannableDate = plannableDate,
                    htmlUrl = null,
                    submissionState = null,
                    newActivity = false,
                    plannerOverride = null
                )
            }
        }
    }

    // This is only used in the Teacher/Parent app to map the context name for planner items, because for some events we don't get it from the API.
    // canvasContexts should already be populated before calling this function.
    protected fun List<PlannerItem>.mapContextName(): List<PlannerItem> {
        return map { plannerItem ->
            if (!plannerItem.contextName.isNullOrEmpty()) {
                plannerItem
            } else {
                // We don't need to handle group context, because we don't have groups in the Teacher/Parent app.
                val contextName = if (plannerItem.courseId != null) canvasContexts.find { it.id == plannerItem.courseId }?.name else null
                plannerItem.copy(contextName = contextName)
            }
        }
    }
}