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
package com.instructure.horizon.features.learn.course.details

import com.instructure.canvasapi2.apis.ExternalToolAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class CourseDetailsRepository @Inject constructor(
    private val getCoursesManager: HorizonGetCoursesManager,
    private val externalToolApi: ExternalToolAPI.ExternalToolInterface,
    private val apiPrefs: ApiPrefs
) {
    suspend fun getCourse(courseId: Long, forceNetwork: Boolean): CourseWithProgress {
        return getCoursesManager.getCourseWithProgressById(courseId, apiPrefs.user?.id ?: -1L, forceNetwork).dataOrThrow
    }

    suspend fun hasExternalTools(courseId: Long, forceNetwork: Boolean): Boolean {
        return externalToolApi.getExternalToolsForCourses(
            listOf(CanvasContext.emptyCourseContext(courseId).contextId),
            RestParams(isForceReadFromNetwork = forceNetwork)
        ).dataOrNull.orEmpty().isNotEmpty()
    }
}