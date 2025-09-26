/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.dashboard

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.DashboardContent
import com.instructure.canvasapi2.managers.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.JourneyApiManager
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val horizonGetCoursesManager: HorizonGetCoursesManager,
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val apiPrefs: ApiPrefs,
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val journeyApiManager: JourneyApiManager,
    private val unreadCountApi: UnreadCountAPI.UnreadCountsInterface,
) {
    suspend fun getDashboardContent(forceNetwork: Boolean): DataResult<DashboardContent> {
        return horizonGetCoursesManager.getDashboardContent(apiPrefs.user?.id ?: -1, forceNetwork)
    }

    suspend fun getFirstPageModulesWithItems(courseId: Long, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return moduleApi.getFirstPageModulesWithItems(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            params,
            includes = listOf("estimated_durations")
        )
    }

    suspend fun acceptInvite(courseId: Long, enrollmentId: Long) {
        return enrollmentApi.acceptInvite(courseId, enrollmentId, RestParams()).dataOrThrow
    }

    suspend fun getPrograms(forceNetwork: Boolean = false): List<Program> {
        return journeyApiManager.getPrograms(forceNetwork)
    }

    suspend fun getUnreadCounts(forceNetwork: Boolean = true): List<UnreadNotificationCount> {
        return unreadCountApi.getNotificationsCount(RestParams(isForceReadFromNetwork = forceNetwork)).dataOrNull.orEmpty()
    }
}