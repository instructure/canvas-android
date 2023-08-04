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

package com.instructure.canvasapi2.apis

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File

interface FileDownloadAPI {

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody
}

sealed class DownloadState {
    data class InProgress(val progress: Int) : DownloadState()
    object Success : DownloadState()
    data class Failure(val throwable: Throwable) : DownloadState()
}

fun ResponseBody.saveFile(file: File): Flow<DownloadState> {
    val debounce = 500L

    return flow {
        emit(DownloadState.InProgress(0))
        var lastUpdate = System.currentTimeMillis()
        try {
            byteStream().use { inputStream ->
                file.outputStream().use { outputStream ->
                    val totalBytes = contentLength()
                    val buffer = ByteArray(8 * 1024)
                    var progressBytes = 0L
                    var bytes = inputStream.read(buffer)

                    while (bytes >= 0) {
                        outputStream.write(buffer, 0, bytes)
                        progressBytes += bytes
                        bytes = inputStream.read(buffer)

                        if (System.currentTimeMillis() - lastUpdate > debounce) {
                            emit(DownloadState.InProgress((progressBytes * 100 / totalBytes).toInt()))
                            lastUpdate = System.currentTimeMillis()
                        }
                    }
                }
            }
            emit(DownloadState.Success)
        } catch (e: Exception) {
            emit(DownloadState.Failure(e))
        }
    }
}