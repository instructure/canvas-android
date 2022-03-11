/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.pandautils.utils

import com.instructure.canvasapi2.managers.NotoriousManager
import com.instructure.canvasapi2.models.NotoriousConfig
import com.instructure.canvasapi2.models.NotoriousSession
import com.instructure.canvasapi2.models.notorious.NotoriousResult
import com.instructure.canvasapi2.models.notorious.NotoriousResultWrapper
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.awaitApi
import java.io.File

object NotoriousUploader {

    suspend fun performUpload(mediaPath: String, onProgress: ProgressRequestUpdateListener? = null) : DataResult<NotoriousResult> {
        try{
            // Set initial progress
            onProgress?.onProgressUpdated(0f, 0)

            // Get NotoriousConfig
            val config = awaitApi<NotoriousConfig> { NotoriousManager.getConfiguration(it) }
            val notoriousDomain = if (config.isEnabled) {
                config.domain.orEmpty()
            } else {
                return DataResult.Fail(Failure.Network("Notorious Config/Response Error: $config"))
            }

            // Start session
            val session = awaitApi<NotoriousSession> { NotoriousManager.startSession(it) }
            ApiPrefs.notoriousDomain = notoriousDomain
            ApiPrefs.notoriousToken = session.token.orEmpty()

            // Get upload token
            val resultWrapper = awaitApi<NotoriousResultWrapper> { NotoriousManager.getUploadToken(it) }
            resultWrapper.result?.error?.let {
                return DataResult.Fail(Failure.Network("Notorious XML/Response Error: $resultWrapper"))
            }
            val uploadToken = resultWrapper.result?.id.orEmpty()

            // Perform upload
            val contentType = FileUtils.getMimeType(mediaPath)
            val file = File(mediaPath)
            val response = NotoriousManager.uploadFileSynchronous(uploadToken, file, contentType, onProgress)

            if (response == null || response.code() != 201) {
                val errorMessage = "Notorious response error: ${response?.message()}, code: ${response?.code()}"
                return DataResult.Fail(Failure.Network(errorMessage))
            }

            // Return uploaded media info
            return DataResult.Success(
                NotoriousManager.getMediaIdSynchronous(uploadToken, file.name, contentType)!!.result!!
            )

        } catch (e: Throwable) {
            return if (!APIHelper.hasNetworkConnection()) {
                DataResult.Fail(Failure.Network("No network!"))
            } else {
                DataResult.Fail(Failure.Exception(e))
            }
        }
    }

}
