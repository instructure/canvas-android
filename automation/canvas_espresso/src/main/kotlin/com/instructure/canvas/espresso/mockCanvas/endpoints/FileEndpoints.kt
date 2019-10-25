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
package com.instructure.canvas.espresso.mockCanvas.endpoints

import android.util.Log
import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.AuthModel
import com.instructure.canvas.espresso.mockCanvas.utils.DontCareAuthModel
import com.instructure.canvas.espresso.mockCanvas.utils.LongId
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse

/**
 * Endpoint for file list operations
 *
 * ROUTES:
 * - `{fileId}` -> `download` -> [FileDownloadEndpoint]
 * - `{fileId}` -> `preview` -> [FileDownloadEndpoint]
 */
object FileListEndpoint : Endpoint(
        LongId(PathVars::fileId) to endpoint (
            Segment("download") to FileDownloadEndpoint,
            Segment("preview") to FileDownloadEndpoint
        )
)

/** Endpoint to retrieve contents for [pathVars.fileId] */
object FileDownloadEndpoint : Endpoint (
        response = {
            GET {
                val fileId = pathVars.fileId
                val content = data.fileContents[fileId]
                //Log.d("<--", "FileDownloadEndpoint fileId=$fileId, content=\"$content\"")
                if (content != null) {
                    request.successResponse(content)
                } else {
                    //Log.d("<--", "FileDownloadEndpoint REJECTED")
                    request.unauthorizedResponse()
                }
            }

        }

) {
    // Disable auth-check for file-download endpoint
    override val authModel: AuthModel
        get() = DontCareAuthModel
}
