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

import android.webkit.URLUtil
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteFile(
    override val id: Long = 0,
    @SerializedName("folder_id")
    val folderId: Long = 0,
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("filename")
    val fileName: String? = null,
    @SerializedName("content-type")
    val contentType: String? = null,
    val url: String? = null,
    val size: Long = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("unlock_at")
    val unlockAt: String? = null,
    val locked: Boolean = false,
    val hidden: Boolean = false,
    @SerializedName("lock_at")
    val lockAt: String? = null,
    @SerializedName("hidden_for_user")
    val hiddenForUser: Boolean = false,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerializedName("modified_at")
    val modifiedAt: String? = null,
    @SerializedName("locked_for_user")
    val lockedForUser: Boolean = false,
    @SerializedName("preview_url")
    val previewUrl: String? = null,
    @SerializedName("lock_explanation")
    val lockExplanation: String? = null
) : CanvasModel<RemoteFile>() {
    override val comparisonDate get() = createdAt.toDate()
    override val comparisonString get() = displayName

    @IgnoredOnParcel
    val isLocalFile = url.isValid() && !URLUtil.isNetworkUrl(url)

    fun toAttachment(): Attachment = Attachment(
        id = id,
        contentType = contentType,
        filename = fileName,
        displayName = displayName,
        url = url,
        thumbnailUrl = thumbnailUrl,
        previewUrl = previewUrl,
        createdAt = createdAt.toDate(),
        size = size
    )
}
