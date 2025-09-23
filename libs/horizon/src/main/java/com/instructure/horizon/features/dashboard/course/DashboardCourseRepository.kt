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
package com.instructure.horizon.features.dashboard.course

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.JourneyApiManager
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import javax.inject.Inject

class DashboardCourseRepository @Inject constructor(
    private val horizonGetCoursesManager: HorizonGetCoursesManager,
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val apiPrefs: ApiPrefs,
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val journeyApiManager: JourneyApiManager,
) {
    suspend fun getEnrollments(forceNetwork: Boolean): List<GetCoursesQuery.Enrollment> {
        return horizonGetCoursesManager.getEnrollments(apiPrefs.user?.id ?: -1, forceNetwork).dataOrThrow
    }

    suspend fun acceptInvite(courseId: Long, enrollmentId: Long) {
        return enrollmentApi.acceptInvite(courseId, enrollmentId, RestParams()).dataOrThrow
    }

    suspend fun getPrograms(forceNetwork: Boolean = false): List<Program> {
        return journeyApiManager.getPrograms(forceNetwork)
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
}