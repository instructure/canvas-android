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

package com.instructure.student.features.files.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult

interface FileListDataSource {

    suspend fun getFolders(folderId: Long, forceNetwork: Boolean): DataResult<List<FileFolder>>

    suspend fun getFiles(folderId: Long, forceNetwork: Boolean): DataResult<List<FileFolder>>

    suspend fun getFolder(folderId: Long, forceNetwork: Boolean): FileFolder?

    suspend fun getRootFolderForContext(canvasContext: CanvasContext, forceNetwork: Boolean): FileFolder?

    suspend fun getNextPage(url: String, forceNetwork: Boolean): DataResult<List<FileFolder>>
}