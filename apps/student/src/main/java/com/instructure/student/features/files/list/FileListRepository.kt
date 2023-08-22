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

package com.instructure.student.features.files.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider

class FileListRepository(
    fileListLocalDataSource: FileListLocalDataSource,
    fileListNetworkDataSource: FileListNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<FileListDataSource>(
    fileListLocalDataSource,
    fileListNetworkDataSource,
    networkStateProvider,
    featureFlagProvider
) {

    suspend fun getFirstPageItems(folderId: Long, forceNetwork: Boolean): DataResult<List<FileFolder>> {
        val foldersResult = getFirstPageFolders(folderId, forceNetwork)
        return when {
            foldersResult.isSuccess && (foldersResult as DataResult.Success).linkHeaders.nextUrl == null -> {
                val filesResult = getFirstPageFiles(folderId, forceNetwork)
                if (filesResult is DataResult.Success) {
                    DataResult.Success(foldersResult.data + filesResult.data, filesResult.linkHeaders)
                } else {
                    filesResult
                }
            }

            else -> foldersResult
        }
    }

    private suspend fun getFirstPageFolders(folderId: Long, forceNetwork: Boolean): DataResult<List<FileFolder>> {
        return dataSource().getFolders(folderId, forceNetwork)
    }

    suspend fun getNextPage(nextUrl: String, folderId: Long, forceNetwork: Boolean): DataResult<List<FileFolder>> {
        val nextResult = getNextPage(nextUrl, forceNetwork)
        return when {
            nextResult.isSuccess && (nextResult as DataResult.Success).linkHeaders.nextUrl == null && !nextUrl.contains("files") -> {
                val filesResult = getFirstPageFiles(folderId, forceNetwork)
                if (filesResult is DataResult.Success) {
                    DataResult.Success(nextResult.data + filesResult.data, filesResult.linkHeaders)
                } else {
                    filesResult
                }
            }
            else -> nextResult
        }
    }

    private suspend fun getNextPage(url: String, forceNetwork: Boolean): DataResult<List<FileFolder>> {
        return dataSource().getNextPage(url, forceNetwork)
    }

    private suspend fun getFirstPageFiles(folderId: Long, forceNetwork: Boolean): DataResult<List<FileFolder>> {
        return dataSource().getFiles(folderId, forceNetwork)
    }

    suspend fun getFolder(folderId: Long, forceNetwork: Boolean): FileFolder? {
        return dataSource().getFolder(folderId, forceNetwork)
    }

    suspend fun getRootFolderForContext(canvasContext: CanvasContext, forceNetwork: Boolean): FileFolder? {
        return dataSource().getRootFolderForContext(canvasContext, forceNetwork)
    }
}