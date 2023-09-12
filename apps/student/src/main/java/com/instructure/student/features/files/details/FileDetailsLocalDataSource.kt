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

import com.instructure.canvasapi2.models.FileFolder
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao

class FileDetailsLocalDataSource(
    private val fileFolderDao: FileFolderDao,
    private val localFileFolderDao: LocalFileDao,
) : FileDetailsDataSource {
    override suspend fun getFileFolderFromURL(url: String, fileId: Long, forceNetwork: Boolean): FileFolder? {
        val file = fileFolderDao.findById(fileId) ?: return null
        val localFile = localFileFolderDao.findById(fileId) ?: return null
        return file.copy(url = localFile.path).toApiModel()
    }
}