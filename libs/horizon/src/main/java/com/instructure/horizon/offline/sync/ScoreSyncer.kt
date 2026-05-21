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
package com.instructure.horizon.offline.sync

import com.instructure.horizon.data.datasource.CourseScoreLocalDataSource
import com.instructure.horizon.data.datasource.CourseScoreNetworkDataSource
import javax.inject.Inject

class ScoreSyncer @Inject constructor(
    private val networkDataSource: CourseScoreNetworkDataSource,
    private val localDataSource: CourseScoreLocalDataSource,
) {
    suspend fun sync(courseId: Long) {
        val assignmentGroups = networkDataSource.getAssignmentGroups(courseId, forceRefresh = true)
        val enrollments = networkDataSource.getEnrollments(courseId, forceRefresh = true)
        localDataSource.saveScoreData(courseId, assignmentGroups, enrollments)
    }
}
