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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.StreamAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class NotificationStreamNetworkDataSource @Inject constructor(
    private val streamApi: StreamAPI.StreamInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val getCoursesManager: HorizonGetCoursesManager,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getStream(
        forceRefresh: Boolean,
        shouldLoginOnTokenError: Boolean = true,
    ): List<StreamItem> {
        val params = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceRefresh,
            shouldLoginOnTokenError = shouldLoginOnTokenError,
        )
        return streamApi.getUserStream(params)
            .depaginate { streamApi.getNextPageStream(it, params) }
            .dataOrThrow
    }

    suspend fun getCoursesInProgressIds(forceRefresh: Boolean): List<Long> {
        return getCoursesManager
            .getCoursesWithProgress(apiPrefs.user?.id ?: -1L, forceRefresh)
            .dataOrThrow
            .map { it.courseId }
    }

    suspend fun getCourse(courseId: Long, shouldLoginOnTokenError: Boolean = true): Course {
        return courseApi.getCourse(
            courseId,
            RestParams(shouldLoginOnTokenError = shouldLoginOnTokenError),
        ).dataOrThrow
    }
}
