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
 */
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Parcelize
data class FileUploadParams(
        var message: String? = null,
        @SerializedName("upload_url")
        var uploadUrl: String? = null,
        @SerializedName("upload_params")
        var uploadParams: Map<String, String> = emptyMap(),
        @SerializedName("attachments")
        var list: List<FileUploadParams>? = null
) : Parcelable {
    fun getPlainTextUploadParams() = uploadParams.mapValues { it.value.toRequestBody("text/plain".toMediaTypeOrNull()) }
}
