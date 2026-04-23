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
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.horizon.data.datasource.CourseScoreLocalDataSource
import com.instructure.horizon.data.datasource.CourseScoreNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class CourseScoreRepository @Inject constructor(
    private val networkDataSource: CourseScoreNetworkDataSource,
    private val localDataSource: CourseScoreLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getAssignmentGroups(courseId: Long, forceRefresh: Boolean = false): List<AssignmentGroup> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getAssignmentGroups(courseId, forceRefresh)
        } else {
            localDataSource.getAssignmentGroups(courseId)
        }
    }

    suspend fun getEnrollments(courseId: Long, forceRefresh: Boolean): List<Enrollment> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getEnrollments(courseId, forceRefresh)
        } else {
            localDataSource.getEnrollments(courseId)
        }
    }

    suspend fun saveScoreData(courseId: Long, assignmentGroups: List<AssignmentGroup>, enrollments: List<Enrollment>) {
        if (shouldSync()) {
            localDataSource.saveScoreData(courseId, assignmentGroups, enrollments)
        }
    }

    override suspend fun sync() {
        TODO("Not yet implemented")
    }
}
