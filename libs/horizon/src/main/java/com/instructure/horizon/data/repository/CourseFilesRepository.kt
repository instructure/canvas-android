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

import com.instructure.canvasapi2.models.FileFolder
import com.instructure.horizon.data.datasource.CourseFilesLocalDataSource
import com.instructure.horizon.data.datasource.CourseFilesNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class CourseFilesRepository @Inject constructor(
    private val networkDataSource: CourseFilesNetworkDataSource,
    private val localDataSource: CourseFilesLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getCourseFiles(courseId: Long): List<FileFolder> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getCourseFiles(courseId).also { files ->
                if (shouldSync()) localDataSource.saveCourseFiles(courseId, files)
            }
        } else {
            localDataSource.getCourseFiles(courseId)
        }
    }

    suspend fun getFileInfo(courseId: Long, fileId: Long): FileFolder? {
        return if (shouldFetchFromNetwork()) networkDataSource.getFileInfo(courseId, fileId) else null
    }

    suspend fun getSyncedFileIds(courseId: Long): Set<Long> {
        return localDataSource.getSyncedFileIds(courseId)
    }

    override suspend fun sync() = Unit
}
