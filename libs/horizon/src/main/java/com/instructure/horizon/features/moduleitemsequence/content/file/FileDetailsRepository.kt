/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.file

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.FileFolder
import javax.inject.Inject

class FileDetailsRepository @Inject constructor(
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    private val oAuthApi: OAuthAPI.OAuthInterface
) {
    suspend fun getFileFolderFromURL(url: String): FileFolder? {
        return fileFolderApi.getFileFolderFromURL(url, RestParams()).dataOrNull
    }

    suspend fun getAuthenticatedFileUrl(fileUrl: String): String {
        return oAuthApi.getAuthenticatedSession("$fileUrl?display=borderless", RestParams(isForceReadFromNetwork = true)).dataOrThrow.sessionUrl
    }
}