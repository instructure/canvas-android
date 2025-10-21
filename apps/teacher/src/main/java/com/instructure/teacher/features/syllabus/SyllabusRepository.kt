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

package com.instructure.teacher.features.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class SyllabusRepository @Inject constructor(
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface
) {

    suspend fun getPlannerItems(
        startDate: String?,
        endDate: String?,
        contextCodes: List<String>,
        filter: String?,
        forceNetwork: Boolean
    ): DataResult<List<PlannerItem>> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return plannerApi.getPlannerItems(
            startDate,
            endDate,
            contextCodes,
            filter,
            restParams
        ).depaginate { plannerApi.nextPagePlannerItems(it, restParams) }
    }

    suspend fun getCalendarEvents(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        contextCodes: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<ScheduleItem>> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return calendarEventApi.getCalendarEvents(
            allEvents,
            type.apiName,
            startDate,
            endDate,
            contextCodes,
            restParams
        ).depaginate { calendarEventApi.next(it, restParams) }
    }
}