/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.offline.sync

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao

interface HtmlParserFileSource {
    suspend fun findLocalFilePath(fileId: Long): String?
    suspend fun findDisplayName(fileId: Long, courseId: Long): String?
    suspend fun isRegisteredForSync(fileId: Long): Boolean
}

class OfflineHtmlParserFileSource(
    private val localFileDao: LocalFileDao,
    private val fileFolderDao: FileFolderDao,
    private val fileSyncSettingsDao: FileSyncSettingsDao,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
) : HtmlParserFileSource {

    override suspend fun findLocalFilePath(fileId: Long): String? {
        return localFileDao.findById(fileId)?.path
    }

    override suspend fun findDisplayName(fileId: Long, courseId: Long): String? {
        fileFolderDao.findById(fileId)?.displayName?.takeIf { it.isNotEmpty() }?.let { return it }
        return fileFolderApi.getCourseFile(
            courseId, fileId,
            RestParams(isForceReadFromNetwork = false, shouldLoginOnTokenError = false)
        ).dataOrNull?.displayName
    }

    override suspend fun isRegisteredForSync(fileId: Long): Boolean {
        return fileSyncSettingsDao.findById(fileId) != null
    }
}
