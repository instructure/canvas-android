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

class CourseFileRepository(private val fileFolderApi: FileFolderAPI.FilesFoldersInterface) {

    suspend fun getCourseFiles(courseId: Long): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = true)
        val rootFolderResult =
            fileFolderApi.getRootFolderForContext(courseId, CanvasContext.Type.COURSE.apiString, params)

        if (rootFolderResult.isFail) return emptyList()

        return getAllFiles(rootFolderResult.dataOrThrow)
    }

    private suspend fun getAllFiles(folder: FileFolder): List<FileFolder> {
        val result = mutableListOf<FileFolder>()
        val subFolders = getFolders(folder)

        val currentFolderFiles = getFiles(folder)
        result.addAll(currentFolderFiles)

        for (subFolder in subFolders) {
            val subFolderFiles = getAllFiles(subFolder)
            result.addAll(subFolderFiles)
        }

        return result
    }

    private suspend fun getFolders(folder: FileFolder): List<FileFolder> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val foldersResult = fileFolderApi.getFirstPageFolders(folder.id, params).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        return foldersResult.dataOrNull.orEmpty().filterValidFileFolders()
    }

    private suspend fun getFiles(folder: FileFolder): List<FileFolder> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val filesResult = fileFolderApi.getFirstPageFiles(folder.id, params).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        return filesResult.dataOrNull.orEmpty().filterValidFileFolders()
    }

    private fun List<FileFolder>.filterValidFileFolders() = this.filter {
        !it.isHidden && !it.isLocked && !it.isHiddenForUser && !it.isLockedForUser
    }
}