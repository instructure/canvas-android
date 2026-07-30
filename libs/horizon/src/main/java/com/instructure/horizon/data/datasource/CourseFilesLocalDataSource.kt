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

import com.instructure.canvasapi2.models.FileFolder
import com.instructure.horizon.database.dao.HorizonFileFolderDao
import com.instructure.horizon.database.dao.HorizonLocalFileDao
import com.instructure.horizon.database.entity.HorizonFileFolderEntity
import javax.inject.Inject

class CourseFilesLocalDataSource @Inject constructor(
    private val fileFolderDao: HorizonFileFolderDao,
    private val localFileDao: HorizonLocalFileDao,
) {

    suspend fun getCourseFiles(courseId: Long): List<FileFolder> {
        return fileFolderDao.findByCourseId(courseId).map { it.toFileFolder() }
    }

    suspend fun saveCourseFiles(courseId: Long, files: List<FileFolder>) {
        files.forEach { file ->
            fileFolderDao.insert(
                HorizonFileFolderEntity(
                    id = file.id,
                    courseId = courseId,
                    url = file.url.orEmpty(),
                    displayName = file.displayName.orEmpty(),
                    size = file.size,
                    contentType = file.contentType,
                    thumbnailUrl = file.thumbnailUrl,
                )
            )
        }
    }

    suspend fun getSyncedFileIds(courseId: Long): Set<Long> {
        return localFileDao.findByCourseId(courseId).map { it.id }.toSet()
    }

    private fun HorizonFileFolderEntity.toFileFolder() = FileFolder(
        id = id,
        url = url,
        displayName = displayName,
        size = size,
        contentType = contentType,
        thumbnailUrl = thumbnailUrl,
    )
}
