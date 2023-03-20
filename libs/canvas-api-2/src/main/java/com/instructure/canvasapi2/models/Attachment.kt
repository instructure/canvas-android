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

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Attachment(
    override var id: Long = 0,
    @SerializedName("content-type")
    var contentType: String? = null,
    var filename: String? = null,
    @SerializedName("display_name")
    var displayName: String? = null,
    var url: String? = null,
    @SerializedName("thumbnail_url")
    var thumbnailUrl: String? = null,
    @SerializedName("preview_url")
    var previewUrl: String? = null,
    @SerializedName("created_at")
    var createdAt: Date? = null,
    var size: Long = 0
) : CanvasModel<Attachment>() {
    override val comparisonDate get() = createdAt
    override val comparisonString get() = displayName
}
