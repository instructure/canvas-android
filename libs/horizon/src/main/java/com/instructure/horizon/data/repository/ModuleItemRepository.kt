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

import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.data.datasource.ModuleItemLocalDataSource
import com.instructure.horizon.data.datasource.ModuleItemNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class ModuleItemRepository @Inject constructor(
    private val networkDataSource: ModuleItemNetworkDataSource,
    private val localDataSource: ModuleItemLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getModuleItemsForCourse(courseId: Long): List<ModuleObject> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getModuleItemsForCourse(courseId)
                .also { if (shouldSync()) localDataSource.saveModuleItem(courseId, it) }
        } else {
            localDataSource.getModuleItemsForCourse(courseId)
        }
    }
}
