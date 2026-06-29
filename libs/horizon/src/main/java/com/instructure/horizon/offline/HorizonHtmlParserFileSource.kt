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
package com.instructure.horizon.offline

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.horizon.database.dao.HorizonFileFolderDao
import com.instructure.horizon.database.dao.HorizonLocalFileDao
import com.instructure.pandautils.features.offline.sync.HtmlParserFileSource
import javax.inject.Inject

class HorizonHtmlParserFileSource @Inject constructor(
    private val localFileDao: HorizonLocalFileDao,
    private val fileFolderDao: HorizonFileFolderDao,
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

    // HorizonFileSyncRepository.syncHtmlFiles already skips files that are in alreadyDownloadedIds,
    // so all files should be passed through for sync consideration.
    override suspend fun isRegisteredForSync(fileId: Long): Boolean = false
}
