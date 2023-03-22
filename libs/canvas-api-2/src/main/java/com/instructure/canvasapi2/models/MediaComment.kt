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

package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@JvmSuppressWildcards
@Parcelize
data class MediaComment(
        // Not provided by API, not used in our code
        //var id: Long = 0,
        @SerializedName("media_id")
        var mediaId: String? = null,
        @SerializedName("display_name")
        var displayName: String? = null,
        var url: String? = null,
        @SerializedName("media_type")
        var mediaType: MediaType? = null,
        @SerializedName("content-type")
        var contentType: String? = null
) : Parcelable {

    enum class MediaType {
        @SerializedName("audio", alternate = ["audio/*"])
        AUDIO,

        @SerializedName("video", alternate = ["video/*"])
        VIDEO
    }

    @Suppress("unused")
    val _fileName: String
        get() = if (mediaId.isNullOrBlank() || url.isNullOrBlank()) "" else "$mediaId.${url?.substringAfterLast('=')}"

}
