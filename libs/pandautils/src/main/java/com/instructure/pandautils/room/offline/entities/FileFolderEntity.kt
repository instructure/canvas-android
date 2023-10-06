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

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.LockInfo
import java.util.Date

@Entity
data class FileFolderEntity(
    @PrimaryKey
    val id: Long,
    val createdDate: Date?,
    val updatedDate: Date?,
    var unlockDate: Date?,
    var lockDate: Date?,
    var isLocked: Boolean,
    var isHidden: Boolean,
    val isLockedForUser: Boolean,
    val isHiddenForUser: Boolean,

    // File Attributes
    val folderId: Long,
    val size: Long,
    val contentType: String?,
    val url: String?,
    val displayName: String?,
    val thumbnailUrl: String?,
    // Folder Attributes
    val parentFolderId: Long,
    val contextId: Long,
    val filesCount: Int,
    val position: Int,
    val foldersCount: Int,
    val contextType: String?,
    val name: String?,
    val foldersUrl: String?,
    val filesUrl: String?,
    val fullName: String?,
    val forSubmissions: Boolean,
    val canUpload: Boolean
) {

    constructor(fileFolder: FileFolder) : this(
        fileFolder.id,
        fileFolder.createdDate,
        fileFolder.updatedDate,
        fileFolder.unlockDate,
        fileFolder.lockDate,
        fileFolder.isLocked,
        fileFolder.isHidden,
        fileFolder.isLockedForUser,
        fileFolder.isHiddenForUser,
        fileFolder.folderId,
        fileFolder.size,
        fileFolder.contentType,
        fileFolder.url,
        fileFolder.displayName,
        fileFolder.thumbnailUrl,
        fileFolder.parentFolderId,
        fileFolder.contextId,
        fileFolder.filesCount,
        fileFolder.position,
        fileFolder.foldersCount,
        fileFolder.contextType,
        fileFolder.name,
        fileFolder.foldersUrl,
        fileFolder.filesUrl,
        fileFolder.fullName,
        fileFolder.forSubmissions,
        fileFolder.canUpload
    )

    fun toApiModel(): FileFolder {
        return FileFolder(
            id,
            createdDate,
            updatedDate,
            unlockDate,
            lockDate,
            isLocked,
            isHidden,
            isLockedForUser,
            isHiddenForUser,
            folderId,
            size,
            contentType,
            url,
            displayName,
            thumbnailUrl,
            null,
            parentFolderId,
            contextId,
            filesCount,
            position,
            foldersCount,
            contextType,
            name,
            foldersUrl,
            filesUrl,
            fullName,
            null,
            forSubmissions,
            canUpload
        )
    }
}