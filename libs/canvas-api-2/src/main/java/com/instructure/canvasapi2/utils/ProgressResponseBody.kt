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
import okio.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileInputStream


class ProgressResponseBody(private val file: File, private val fileIndex: Int?, private val submissionId: Long?) :
    RequestBody() {

    override fun contentLength() = file.length()

    override fun contentType() = "application/octet-stream".toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded: Long = 0

        FileInputStream(file).use { stream ->
            var read: Int = stream.read(buffer)
            while (read != -1) {

                // Send out updates if we have a submission id and a file index
                if (fileIndex != null && submissionId != null) {
                    val event = ProgressEvent(fileIndex, submissionId, uploaded, contentLength())
                    EventBus.getDefault().postSticky(event)
                }

                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                read = stream.read(buffer)
            }
        }
    }
}

data class ProgressEvent(val fileIndex: Int, val submissionId: Long, val uploaded: Long, val contentLength: Long)