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

import com.google.gson.annotations.SerializedName

data class CreateFolder(
        var name: String = "",
        var locked: Boolean = true
)

data class UpdateFileFolder(
        val name: String? = null,
        @SerializedName("lock_at")
        val lockAt: String? = null,
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        val locked: Boolean? = null,
        val hidden: Boolean? = null,
        @SerializedName("parent_folder_id")
        var parentFolderId: Long? = null,   // Used for Files
        @SerializedName("on_duplicate")
        var onDuplicate: String? = null,     // Used for files - "overwrite" or "rename"
        @SerializedName("visibility_level")
        val visibilityLevel: String? = null
)


sealed class FileAccessStatus(var lockAt: String = "", var unlockAt: String = "")
class PublishStatus : FileAccessStatus()
class UnpublishStatus : FileAccessStatus()
class RestrictedStatus : FileAccessStatus()
class RestrictedScheduleStatus : FileAccessStatus()

