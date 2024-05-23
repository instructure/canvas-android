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
import com.instructure.canvasapi2.utils.NaturalOrderComparator
import com.instructure.canvasapi2.utils.isValid
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.Locale

@Parcelize
data class FileFolder(
    // Common Attributes
    override val id: Long = 0,
    @SerializedName("created_at")
    val createdDate: Date? = null,
    @SerializedName("updated_at")
    val updatedDate: Date? = null,
    @SerializedName("unlock_at")
    var unlockDate: Date? = null,
    @SerializedName("lock_at")
    var lockDate: Date? = null,
    @SerializedName("locked")
    var isLocked: Boolean = false,
    @SerializedName("hidden")
    var isHidden: Boolean = false,
    @SerializedName("locked_for_user")
    val isLockedForUser: Boolean = false,
    @SerializedName("hidden_for_user")
    val isHiddenForUser: Boolean = false,

    // File Attributes
    @SerializedName("folder_id")
    val folderId: Long = 0,
    val size: Long = 0,
    @SerializedName("content-type")
    val contentType: String? = null,
    val url: String? = null,
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerializedName("lock_info")
    val lockInfo: LockInfo? = null,

    // Folder Attributes
    @SerializedName("parent_folder_id")
    val parentFolderId: Long = 0,
    @SerializedName("context_id")
    val contextId: Long = 0,
    @SerializedName("files_count")
    val filesCount: Int = 0,
    val position: Int = 0,
    @SerializedName("folders_count")
    val foldersCount: Int = 0,
    @SerializedName("context_type")
    val contextType: String? = null,
    val name: String? = null,
    @SerializedName("folders_url")
    val foldersUrl: String? = null,
    @SerializedName("files_url")
    val filesUrl: String? = null,
    @SerializedName("full_name")
    val fullName: String? = null,
    @SerializedName("usage_rights")
    var usageRights: UsageRights? = null,
    @SerializedName("for_submissions")
    var forSubmissions: Boolean = false, // Only for folders
    @SerializedName("can_upload")
    val canUpload: Boolean = false,
    var avatar: Avatar? = null, // Used to get a file token to update avatars with vanity URLs
    @SerializedName("visibility_level")
    val visibilityLevel: String? = null
) : CanvasModel<FileFolder>() {
    val isRoot: Boolean get() = parentFolderId == 0L
    val isFile: Boolean get() = !displayName.isNullOrBlank()

    val isHtmlFile: Boolean
        get() = contentType?.contains("html") == true
                || name?.endsWith(".htm") == true
                || name?.endsWith(".html") == true

    fun getFilePreviewUrl(fullDomain: String, canvasContext: CanvasContext): String {
        return "$fullDomain${canvasContext.toAPIString()}/files/$id/preview"
    }

    /* We override compareTo instead of using Canvas Comparable methods */
    override fun compareTo(other: FileFolder) = compareFiles(this, other)

    private fun compareFiles(file1: FileFolder, file2: FileFolder): Int {
        return when {
            (file1.fullName == null && file2.fullName != null) -> 1
            (file1.fullName != null && file2.fullName == null) -> -1
            (file1.fullName != null && file2.fullName != null) -> NaturalOrderComparator.compare(
                file1.fullName.lowercase(
                    Locale.getDefault()
                ), file2.fullName.lowercase(Locale.getDefault())
            )

            else -> NaturalOrderComparator.compare(
                file1.displayName?.lowercase(Locale.getDefault()),
                file2.displayName?.lowercase(Locale.getDefault())
            )
        }
    }

    val isLocalFile = url.isValid() && !URLUtil.isNetworkUrl(url)

}
