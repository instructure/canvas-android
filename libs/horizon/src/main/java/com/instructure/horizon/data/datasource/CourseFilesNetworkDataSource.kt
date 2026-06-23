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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class CourseFilesNetworkDataSource @Inject constructor(
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
) {

    suspend fun getCourseFiles(courseId: Long): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        val rootFolder = fileFolderApi.getRootFolderForContext(
            courseId,
            CanvasContext.Type.COURSE.apiString,
            params,
        ).dataOrNull ?: return emptyList()
        return getAllFiles(rootFolder, params)
    }

    private suspend fun getAllFiles(folder: FileFolder, params: RestParams): List<FileFolder> {
        val subFolders = fileFolderApi.getFirstPageFolders(folder.id, params.copy(usePerPageQueryParam = true))
            .depaginate { fileFolderApi.getNextPageFileFoldersList(it, params) }
            .dataOrNull.orEmpty()
            .filterValidFileFolders()

        val files = fileFolderApi.getFirstPageFiles(folder.id, params.copy(usePerPageQueryParam = true))
            .depaginate { fileFolderApi.getNextPageFileFoldersList(it, params) }
            .dataOrNull.orEmpty()
            .filterValidFileFolders()

        return files + subFolders.flatMap { getAllFiles(it, params) }
    }

    suspend fun getFileInfo(courseId: Long, fileId: Long): FileFolder? {
        val params = RestParams(isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        return fileFolderApi.getCourseFile(courseId, fileId, params).dataOrNull
    }

    private fun List<FileFolder>.filterValidFileFolders() = filter {
        !it.isHidden && !it.isLocked && !it.isHiddenForUser && !it.isLockedForUser
    }
}
