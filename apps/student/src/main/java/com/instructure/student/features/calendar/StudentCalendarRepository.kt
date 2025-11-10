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
package com.instructure.student.features.calendar

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.pandautils.utils.orDefault

class StudentCalendarRepository(
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val coursesApi: CourseAPI.CoursesInterface,
    private val groupsApi: GroupAPI.GroupInterface,
    private val apiPrefs: ApiPrefs,
    private val calendarFilterDao: CalendarFilterDao
) : CalendarRepository() {

    override suspend fun getPlannerItems(
        startDate: String,
        endDate: String,
        contextCodes: List<String>,
        forceNetwork: Boolean
    ): List<PlannerItem> {
        val restParams =
            RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)

        // The calendar on web does not include announcements, so we filter them out here to match that behavior
        return plannerApi.getPlannerItems(
            startDate,
            endDate,
            emptyList(), // We always request all the events for students and filter locally
            null,
            restParams
        ).depaginate {
            plannerApi.nextPagePlannerItems(it, restParams)
        }.dataOrThrow
            .filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST }
    }

    override suspend fun getCanvasContexts(): DataResult<Map<CanvasContext.Type, List<CanvasContext>>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false)

        val coursesResult = coursesApi.getFirstPageCoursesCalendar(params)
            .depaginate { nextUrl -> coursesApi.next(nextUrl, params) }

        if (coursesResult.isFail) return DataResult.Fail()

        val courses = (coursesResult as DataResult.Success).data
        val validCourses = courses.filter { !it.accessRestrictedByDate && it.hasActiveEnrollment() }

        val groupsResult = groupsApi.getFirstPageGroups(params)
            .depaginate { nextUrl -> groupsApi.getNextPageGroups(nextUrl, params) }

        val groups = groupsResult.dataOrNull ?: emptyList()

        val courseMap = validCourses.associateBy { it.id }
        val validGroups = groups.filter { it.courseId == 0L || courseMap[it.courseId] != null }

        val users = apiPrefs.user?.let { listOf(it) } ?: emptyList()

        val contexts = (users + validCourses + validGroups).groupBy { it.type }.filter { it.value.isNotEmpty() }

        return DataResult.Success(contexts)
    }

    override suspend fun getCalendarFilterLimit(): Int {
        return -1
    }

    override suspend fun getCalendarFilters(): CalendarFilterEntity? {
        return calendarFilterDao.findByUserIdAndDomain(apiPrefs.user?.id.orDefault(), apiPrefs.fullDomain)
    }

    override suspend fun updateCalendarFilters(calendarFilterEntity: CalendarFilterEntity) {
        calendarFilterDao.insertOrUpdate(calendarFilterEntity)
    }
}