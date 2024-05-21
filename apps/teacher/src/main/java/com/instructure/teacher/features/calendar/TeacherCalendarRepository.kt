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
package com.instructure.teacher.features.calendar

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.features.calendar.CalendarRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class TeacherCalendarRepository(
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val coursesApi: CourseAPI.CoursesInterface,
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface,
    private val apiPrefs: ApiPrefs,
    private val featuresApi: FeaturesAPI.FeaturesInterface
) : CalendarRepository {

    private var canvasContexts: List<CanvasContext> = emptyList()

    override suspend fun getPlannerItems(
        startDate: String,
        endDate: String,
        contextCodes: List<String>,
        forceNetwork: Boolean
    ): List<PlannerItem> {
        if (contextCodes.isEmpty()) return emptyList()

        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val allItems = coroutineScope {
            val calendarEvents = async {
                calendarEventApi.getCalendarEvents(
                    false,
                    CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
                    startDate,
                    endDate,
                    contextCodes,
                    restParams
                ).depaginate {
                    calendarEventApi.next(it, restParams)
                }.dataOrThrow.toPlannerItems(PlannableType.CALENDAR_EVENT)
            }

            val calendarAssignments = async {
                calendarEventApi.getCalendarEvents(
                    false,
                    CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
                    startDate,
                    endDate,
                    contextCodes,
                    restParams
                ).depaginate {
                    calendarEventApi.next(it, restParams)
                }.dataOrThrow.toPlannerItems(PlannableType.ASSIGNMENT)
            }

            val plannerNotes = async {
                plannerApi.getPlannerNotes(startDate, endDate, contextCodes, restParams).depaginate {
                    plannerApi.nextPagePlannerNotes(it, restParams)
                }.dataOrThrow.toPlannerItems()
            }

            return@coroutineScope listOf(calendarEvents, calendarAssignments, plannerNotes).awaitAll()
        }

        return allItems.flatten().sortedBy { it.plannableDate }
    }

    override suspend fun getCanvasContexts(): DataResult<Map<CanvasContext.Type, List<CanvasContext>>> {
        val params = RestParams(usePerPageQueryParam = true)

        val coursesResult = coursesApi.getFirstPageCoursesCalendar(params)
            .depaginate { nextUrl -> coursesApi.next(nextUrl, params) }

        return if (coursesResult is DataResult.Success) {
            val users = apiPrefs.user?.let { listOf(it) } ?: emptyList()
            val contexts = (users + coursesResult.data).groupBy { it.type }.filter { it.value.isNotEmpty() }
            canvasContexts = contexts.values.flatten()
            DataResult.Success(contexts)
        } else {
            DataResult.Fail()
        }
    }

    override suspend fun getCalendarFilterLimit(): Int {
        val result = featuresApi.getAccountSettingsFeatures(RestParams())
        return if (result.isSuccess) {
            val features = result.dataOrThrow
            val increasedContextLimit = features["calendar_contexts_limit"]
            if (increasedContextLimit == true) 20 else 10
        } else {
            10
        }
    }

    private fun List<Plannable>.toPlannerItems(): List<PlannerItem> {
        return mapNotNull { plannable ->
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
}

private fun List<ScheduleItem>.toPlannerItems(type: PlannableType): List<PlannerItem> {
    return mapNotNull {
        val plannableType = if (type == PlannableType.ASSIGNMENT) {
            when {
                it.assignment?.getSubmissionTypes()
                    ?.contains(Assignment.SubmissionType.DISCUSSION_TOPIC) == true -> PlannableType.DISCUSSION_TOPIC

                it.assignment?.getSubmissionTypes()?.contains(Assignment.SubmissionType.ONLINE_QUIZ) == true -> PlannableType.QUIZ
                else -> PlannableType.ASSIGNMENT
            }
        } else {
            type
        }
        val plannableDate = if (type == PlannableType.ASSIGNMENT) it.assignment?.dueDate else it.startDate
        val plannableId = if (plannableType == PlannableType.DISCUSSION_TOPIC && it.assignment?.discussionTopicHeader?.id != null) {
            it.assignment?.discussionTopicHeader?.id!!
        } else {
            it.id
        } // Plannable id is the discussion id for the students so we make this the same for the teachers as well.

        if (plannableDate == null) {
            null
        } else {
            PlannerItem(
                if (it.courseId != -1L) it.courseId else null,
                if (it.groupId != -1L) it.groupId else null,
                if (it.userId != -1L) it.userId else null,
                it.contextType?.apiString,
                it.contextName,
                plannableType,
                Plannable(
                    plannableId,
                    it.title.orEmpty(),
                    it.courseId,
                    it.groupId,
                    it.userId,
                    null,
                    if (type == PlannableType.ASSIGNMENT) it.assignment?.dueDate else null,
                    it.assignment?.id,
                    null,
                    it.startDate,
                    it.endDate,
                    it.description,
                    it.isAllDay
                ),
                plannableDate,
                it.htmlUrl,
                null,
                null,
                null,
            )
        }
    }
}