/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.student.mobius.syllabus.datasource

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class SyllabusNetworkDataSource(
    private val courseApi: CourseAPI.CoursesInterface,
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface,
    private val plannerApi: PlannerAPI.PlannerInterface
) : SyllabusDataSource {

    override suspend fun getCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourseSettings(courseId, restParams).dataOrNull
    }

    override suspend fun getCourseWithSyllabus(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourseWithSyllabus(courseId, restParams)
    }

    override suspend fun getCalendarEvents(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        canvasContexts: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<ScheduleItem>> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return calendarEventApi.getCalendarEvents(
            allEvents,
            type.apiName,
            startDate,
            endDate,
            canvasContexts,
            restParams
        ).depaginate { calendarEventApi.next(it, restParams) }
    }

    override suspend fun getPlannerItems(
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
}