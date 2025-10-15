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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.PlannerItemDao
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.ScheduleItemFacade

class SyllabusLocalDataSource(
    private val courseSettingsDao: CourseSettingsDao,
    private val courseFacade: CourseFacade,
    private val scheduleItemFacade: ScheduleItemFacade,
    private val plannerItemDao: PlannerItemDao
) : SyllabusDataSource {

    override suspend fun getCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return courseSettingsDao.findByCourseId(courseId)?.toApiModel()
    }

    override suspend fun getCourseWithSyllabus(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        return courseFacade.getCourseById(courseId)?.let {
            DataResult.Success(it)
        } ?: DataResult.Fail()
    }

    override suspend fun getCalendarEvents(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        canvasContexts: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<ScheduleItem>> {
        return try {
            DataResult.Success(scheduleItemFacade.findByItemType(canvasContexts, type.apiName))
        } catch (e: Exception) {
            DataResult.Fail()
        }

    }

    override suspend fun getPlannerItems(
        startDate: String?,
        endDate: String?,
        contextCodes: List<String>,
        filter: String?,
        forceNetwork: Boolean
    ): DataResult<List<PlannerItem>> {
        return try {
            val courseIds = contextCodes.mapNotNull { contextCode ->
                val parts = contextCode.split("_")
                if (parts.size == 2 && parts[0] == "course") {
                    parts[1].toLongOrNull()
                } else null
            }

            val plannerItems = if (courseIds.isNotEmpty()) {
                plannerItemDao.findByCourseIds(courseIds).map { it.toApiModel() }
            } else {
                emptyList()
            }

            DataResult.Success(plannerItems)
        } catch (e: Exception) {
            DataResult.Fail()
        }
    }
}