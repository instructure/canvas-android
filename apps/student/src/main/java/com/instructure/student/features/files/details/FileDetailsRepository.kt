/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.student.features.files.details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import okhttp3.ResponseBody

class FileDetailsRepository(
        localDataSource: FileDetailsLocalDataSource,
        networkDataSource: FileDetailsNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
) : Repository<FileDetailsDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun markAsRead(canvasContext: CanvasContext, moduleId: Long, itemId: Long, forceNetwork: Boolean): DataResult<ResponseBody> {
        return dataSource().markAsRead(canvasContext, moduleId, itemId, forceNetwork)
    }

    suspend fun getFileFolderFromURL(url: String, forceNetwork: Boolean): DataResult<FileFolder?> {
        return dataSource().getFileFolderFromURL(url, forceNetwork)
    }
}