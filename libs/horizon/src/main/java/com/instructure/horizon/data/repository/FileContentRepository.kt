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

import com.instructure.horizon.data.datasource.FileContentLocalDataSource
import com.instructure.horizon.data.datasource.FileContentNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

data class FileDetails(
    val id: Long,
    val url: String,
    val displayName: String,
    val contentType: String?,
    val thumbnailUrl: String?,
    val localPath: String?,
)

class FileContentRepository @Inject constructor(
    private val networkDataSource: FileContentNetworkDataSource,
    private val localDataSource: FileContentLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getFileDetails(url: String, courseId: Long): FileDetails {
        return if (shouldFetchFromNetwork()) {
            val fileFolder = networkDataSource.getFileDetails(url)
                ?: throw IllegalStateException("File not found: $url")
            if (shouldSync()) {
                localDataSource.saveFileFolder(fileFolder)
                networkDataSource.downloadFile(fileFolder.id, courseId)
            }
            val localPath = localDataSource.getLocalFilePath(fileFolder.id)
            FileDetails(
                id = fileFolder.id,
                url = fileFolder.url.orEmpty(),
                displayName = fileFolder.displayName.orEmpty(),
                contentType = fileFolder.contentType,
                thumbnailUrl = fileFolder.thumbnailUrl,
                localPath = localPath,
            )
        } else {
            val fileId = extractFileId(url)
                ?: throw IllegalStateException("Cannot determine file ID from URL: $url")
            val fileFolder = localDataSource.getFileFolder(fileId)
                ?: throw IllegalStateException("File $fileId not available offline")
            val localPath = localDataSource.getLocalFilePath(fileId)
            FileDetails(
                id = fileFolder.id,
                url = fileFolder.url.orEmpty(),
                displayName = fileFolder.displayName.orEmpty(),
                contentType = fileFolder.contentType,
                thumbnailUrl = fileFolder.thumbnailUrl,
                localPath = localPath,
            )
        }
    }

    private fun extractFileId(url: String): Long? {
        return Regex("files/(\\d+)").find(url)?.groupValues?.get(1)?.toLongOrNull()
    }
}
