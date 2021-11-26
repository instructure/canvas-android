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
 */
package com.instructure.canvasapi2.utils

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import org.threeten.bp.Duration
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    updateInterval: Duration = Duration.ofMillis(250),
    private val onProgress: ProgressRequestUpdateListener? = null
) : RequestBody() {

    private val interval = updateInterval.toMillis()

    init {
        if (interval < 100) throw IllegalArgumentException("Update interval cannot be less than 100 milliseconds")
    }

    override fun contentLength() = file.length()

    override fun contentType() = contentType.toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded: Long = 0

        var lastUpdate = System.currentTimeMillis()

        FileInputStream(file).use { stream ->
            var read: Int = stream.read(buffer)
            while (read != -1) {

                // Send out updates
                if (onProgress != null && System.currentTimeMillis() > lastUpdate + interval) {
                    val progress = uploaded.toDouble() / contentLength().toDouble()
                    if (!onProgress.onProgressUpdated(progress.toFloat(), contentLength())) {
                        return@use // If false is returned, stop uploading
                    }
                    lastUpdate = System.currentTimeMillis()
                }

                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                read = stream.read(buffer)
            }
        }
    }
}

interface ProgressRequestUpdateListener {
    // Given a percentage progress and the total content length, return false if uploading should stop
    fun onProgressUpdated(progressPercent: Float, length: Long): Boolean
}