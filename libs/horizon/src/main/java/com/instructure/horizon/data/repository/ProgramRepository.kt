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

import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.horizon.data.datasource.ProgramLocalDataSource
import com.instructure.horizon.data.datasource.ProgramNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class ProgramRepository @Inject constructor(
    private val networkDataSource: ProgramNetworkDataSource,
    private val localDataSource: ProgramLocalDataSource,
    private val enrollmentRepository: CourseEnrollmentRepository,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getPrograms(): List<Program> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getPrograms()
                .also { programs ->
                    if (shouldSync()) {
                        val enrolledCourseIds = enrollmentRepository.getEnrolledCourseIds().toSet()
                        localDataSource.savePrograms(programs, enrolledCourseIds)
                    }
                }
        } else {
            localDataSource.getPrograms()
        }
    }

    override suspend fun sync() {
        TODO("Not yet implemented")
    }
}
