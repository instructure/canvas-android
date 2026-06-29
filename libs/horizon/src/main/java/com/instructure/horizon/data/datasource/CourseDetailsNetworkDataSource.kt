/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.apis.ExternalToolAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class CourseDetailsNetworkDataSource @Inject constructor(
    private val getCoursesManager: HorizonGetCoursesManager,
    private val getProgramsManager: GetProgramsManager,
    private val externalToolApi: ExternalToolAPI.ExternalToolInterface,
    private val apiPrefs: ApiPrefs,
) {

    suspend fun getCourse(courseId: Long, forceRefresh: Boolean): CourseWithProgress {
        return getCoursesManager.getCourseWithProgressById(courseId, apiPrefs.user?.id ?: -1L, forceRefresh).dataOrThrow
    }

    suspend fun getProgramsForCourse(courseId: Long, forceRefresh: Boolean): List<Program> {
        return getProgramsManager.getPrograms(forceRefresh).filter {
            it.sortedRequirements.firstOrNull()?.courseId == courseId
        }
    }

    suspend fun hasExternalTools(courseId: Long, forceRefresh: Boolean): Boolean {
        return externalToolApi.getExternalToolsForCourses(
            listOf(CanvasContext.emptyCourseContext(courseId).contextId),
            RestParams(isForceReadFromNetwork = forceRefresh)
        ).dataOrNull.orEmpty().isNotEmpty()
    }
}
