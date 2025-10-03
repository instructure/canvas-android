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

package com.instructure.student.mobius.syllabus

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.syllabus.datasource.SyllabusDataSource
import com.instructure.student.mobius.syllabus.datasource.SyllabusLocalDataSource
import com.instructure.student.mobius.syllabus.datasource.SyllabusNetworkDataSource

class SyllabusRepository(
    syllabusLocalDataSource: SyllabusLocalDataSource,
    syllabusNetworkDataSource: SyllabusNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<SyllabusDataSource>(syllabusLocalDataSource, syllabusNetworkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return dataSource().getCourseSettings(courseId, forceNetwork)
    }

    suspend fun getCourseWithSyllabus(courseId: Long, forceNetwork: Boolean): DataResult<Course> {
        return dataSource().getCourseWithSyllabus(courseId, forceNetwork)
    }

    suspend fun getCalendarEvents(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        canvasContexts: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<ScheduleItem>> {
        return dataSource().getCalendarEvents(allEvents, type, startDate, endDate, canvasContexts, forceNetwork)
    }

    suspend fun getPlannerItems(
        startDate: String?,
        endDate: String?,
        contextCodes: List<String>,
        filter: String?,
        forceNetwork: Boolean
    ): DataResult<List<PlannerItem>> {
        return dataSource().getPlannerItems(startDate, endDate, contextCodes, filter, forceNetwork)
    }
}