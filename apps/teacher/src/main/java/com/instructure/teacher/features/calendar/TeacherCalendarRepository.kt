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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.toPlannerItems
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.pandautils.utils.orDefault
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class TeacherCalendarRepository(
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val coursesApi: CourseAPI.CoursesInterface,
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface,
    private val apiPrefs: ApiPrefs,
    private val featuresApi: FeaturesAPI.FeaturesInterface,
    private val calendarFilterDao: CalendarFilterDao
) : CalendarRepository() {

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
                }.dataOrThrow
                    .filterNot { it.isHidden }
                    .toPlannerItems(PlannableType.CALENDAR_EVENT)
                    .mapContextName()
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
                }.dataOrThrow
                    .filterNot { it.isHidden }
                    .toPlannerItems(PlannableType.ASSIGNMENT)
                    .mapContextName()
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

    override suspend fun getCalendarFilters(): CalendarFilterEntity? {
        return calendarFilterDao.findByUserIdAndDomain(apiPrefs.user?.id.orDefault(), apiPrefs.fullDomain)
    }

    override suspend fun updateCalendarFilters(calendarFilterEntity: CalendarFilterEntity) {
        calendarFilterDao.insertOrUpdate(calendarFilterEntity)
    }
}
