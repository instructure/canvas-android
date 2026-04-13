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

class FileContentLocalDataSource @Inject constructor(
    private val localFileDao: HorizonLocalFileDao,
    private val fileFolderDao: HorizonFileFolderDao,
) {

    suspend fun getLocalFilePath(fileId: Long): String? {
        return localFileDao.findById(fileId)?.path
    }

    suspend fun getFileFolder(fileId: Long): FileFolder? {
        return fileFolderDao.findById(fileId)?.toFileFolder()
    }

    suspend fun saveFileFolder(fileFolder: FileFolder) {
        fileFolderDao.insert(
            HorizonFileFolderEntity(
                id = fileFolder.id,
                url = fileFolder.url.orEmpty(),
                displayName = fileFolder.displayName.orEmpty(),
                contentType = fileFolder.contentType,
                thumbnailUrl = fileFolder.thumbnailUrl,
            )
        )
    }

    private fun HorizonFileFolderEntity.toFileFolder(): FileFolder {
        return FileFolder(
            id = id,
            url = url,
            displayName = displayName,
            contentType = contentType,
            thumbnailUrl = thumbnailUrl,
        )
    }
}
