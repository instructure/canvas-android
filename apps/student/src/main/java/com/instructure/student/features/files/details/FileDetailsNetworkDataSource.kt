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

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import okhttp3.ResponseBody

class FileDetailsNetworkDataSource(
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
) : FileDetailsDataSource {
    suspend fun markAsRead(canvasContext: CanvasContext, moduleId: Long, itemId: Long, forceNetwork: Boolean): ResponseBody? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return moduleApi.markModuleItemRead(canvasContext.apiContext(), canvasContext.id, moduleId, itemId, restParams).dataOrNull
    }

    override suspend fun getFileFolderFromURL(url: String, fileId: Long, forceNetwork: Boolean): FileFolder? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return fileFolderApi.getFileFolderFromURL(url, restParams).dataOrNull
    }
}