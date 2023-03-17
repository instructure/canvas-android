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
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class DiscussionAttachment(
        override val id: Long = 0,
        val locked: Boolean = false,
        val hidden: Boolean = false,
        @SerializedName("locked_for_user")
        val lockedForUser: Boolean = false,
        @SerializedName("hidden_for_user")
        val hiddenForUser: Boolean = false,
        val size: Int = 0,
        @SerializedName("lock_at")
        val lockAt: String? = null,
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("display_name")
        val displayName: String? = null,
        val filename: String? = null,
        val url: String? = null,
        @SerializedName("content-type")
        val contentType: String? = null,
        @SerializedName("folder_id")
        val folderId: Long = 0,
        @SerializedName("thumbnail_url")
        val thumbnailUrl: String? = null
) : CanvasModel<DiscussionAttachment>() {
    override val comparisonString get() = filename

    fun shouldShowToUser(): Boolean {
        return if (hidden || hiddenForUser) {
            false
        } else if (locked || lockedForUser) {
            val unlockAtDate = unlockAt.toDate()
            if (unlockAt == null) {
                false
            } else {
                Date().after(unlockAtDate)
            }
        } else {
            true
        }
    }
}
