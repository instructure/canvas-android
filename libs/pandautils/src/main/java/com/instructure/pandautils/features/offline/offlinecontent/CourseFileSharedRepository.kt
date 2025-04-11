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

package com.instructure.pandautils.features.offline.offlinecontent

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.depaginate

class CourseFileSharedRepository(private val fileFolderApi: FileFolderAPI.FilesFoldersInterface) {

    suspend fun getCourseFoldersAndFiles(courseId: Long): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = true, shouldRefreshToken = false)
        val rootFolderResult =
            fileFolderApi.getRootFolderForContext(courseId, CanvasContext.Type.COURSE.apiString, params)

        if (rootFolderResult.isFail) return emptyList()

        val result = mutableListOf<FileFolder>()

        result.addAll(getAllFoldersAndFiles(rootFolderResult.dataOrThrow, params))

        return result
    }

    private suspend fun getAllFoldersAndFiles(folder: FileFolder, params: RestParams): List<FileFolder> {
        val result = mutableListOf<FileFolder>()
        result.add(folder)
        val subFolders = getFolders(folder, params)

        val currentFolderFiles = getFiles(folder, params)
        result.addAll(currentFolderFiles)

        for (subFolder in subFolders) {
            val subFolderFiles = getAllFoldersAndFiles(subFolder, params)
            result.addAll(subFolderFiles)
        }

        return result
    }

    suspend fun getCourseFiles(courseId: Long): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = true)
        val rootFolderResult =
            fileFolderApi.getRootFolderForContext(courseId, CanvasContext.Type.COURSE.apiString, params)

        if (rootFolderResult.isFail) return emptyList()

        return getAllFiles(rootFolderResult.dataOrThrow, params)
    }

    private suspend fun getAllFiles(folder: FileFolder, params: RestParams): List<FileFolder> {
        val result = mutableListOf<FileFolder>()
        val subFolders = getFolders(folder, params)

        val currentFolderFiles = getFiles(folder, params)
        result.addAll(currentFolderFiles)

        for (subFolder in subFolders) {
            val subFolderFiles = getAllFiles(subFolder, params)
            result.addAll(subFolderFiles)
        }

        return result
    }

    private suspend fun getFolders(folder: FileFolder, params: RestParams): List<FileFolder> {
        val foldersResult = fileFolderApi.getFirstPageFolders(folder.id, params.copy(usePerPageQueryParam = true)).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        return foldersResult.dataOrNull.orEmpty().filterValidFileFolders()
    }

    private suspend fun getFiles(folder: FileFolder, params: RestParams): List<FileFolder> {
        val filesResult = fileFolderApi.getFirstPageFiles(folder.id, params.copy(usePerPageQueryParam = true)).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        return filesResult.dataOrNull.orEmpty().filterValidFileFolders()
    }

    private fun List<FileFolder>.filterValidFileFolders() = this.filter {
        !it.isHidden && !it.isLocked && !it.isHiddenForUser && !it.isLockedForUser
    }
}