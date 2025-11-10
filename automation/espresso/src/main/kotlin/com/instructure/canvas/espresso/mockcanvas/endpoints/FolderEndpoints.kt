/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */
package com.instructure.canvas.espresso.mockcanvas.endpoints

import com.instructure.canvas.espresso.mockcanvas.Endpoint
import com.instructure.canvas.espresso.mockcanvas.utils.AuthModel
import com.instructure.canvas.espresso.mockcanvas.utils.DontCareAuthModel
import com.instructure.canvas.espresso.mockcanvas.utils.LongId
import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import com.instructure.canvas.espresso.mockcanvas.utils.Segment
import com.instructure.canvas.espresso.mockcanvas.utils.successPaginatedResponse
import com.instructure.canvas.espresso.mockcanvas.utils.successResponse
import com.instructure.canvas.espresso.mockcanvas.utils.unauthorizedResponse
import com.instructure.canvasapi2.models.FileFolder

/**
 * Endpoint for folder list operations
 *
 * ROUTES:
 * - `{folderId}` -> [FolderEndpoint]
 */
object FolderListEndpoint : Endpoint(
        LongId(PathVars::folderId) to FolderEndpoint,
        response = {
            GET {
                val folders = data.fileFolders.values.toList()
                request.successPaginatedResponse(folders)
            }
        }
)

/**
 * Endpoint for specific folder operations
 *
 * ROUTES:
 * - `files` -> [FolderFileListEndpoint]
 * - `folders` -> [FolderSubfoldersEndpoint]
 */
object FolderEndpoint : Endpoint(
        Segment("files") to FolderFileListEndpoint,
        Segment("folders") to FolderSubfoldersEndpoint,
        response = {
            GET {
                val folderId = pathVars.folderId
                val folder = data.fileFolders[folderId]
                if (folder != null) {
                    request.successResponse(folder)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
) {
    // Disable auth-check for folder endpoint
    override val authModel: AuthModel
        get() = DontCareAuthModel
}

/**
 * Endpoint to return all files associated with a folder
 */
object FolderFileListEndpoint : Endpoint(
        response = {
            GET {
                val folderId = pathVars.folderId
                val fileList = data.folderFiles[folderId] ?: mutableListOf<FileFolder>()
                request.successPaginatedResponse(fileList.toList())
            }
        }

)

/**
 * Endpoint to return all subfolders associated with a folder
 */
object FolderSubfoldersEndpoint : Endpoint(
        response = {
            GET {
                val folderId = pathVars.folderId
                val folderList = data.folderSubFolders[folderId] ?: mutableListOf<FileFolder>()
                request.successPaginatedResponse(folderList.toList())
            }
        }
)


