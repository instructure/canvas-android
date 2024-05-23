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
 */

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.RemoteFile

@Entity
data class RemoteFileEntity(
    @PrimaryKey
    val id: Long,
    val folderId: Long,
    val displayName: String?,
    val fileName: String?,
    val contentType: String?,
    val url: String?,
    val size: Long,
    val createdAt: String?,
    val updatedAt: String?,
    val unlockAt: String?,
    val locked: Boolean,
    val hidden: Boolean,
    val lockAt: String?,
    val hiddenForUser: Boolean,
    val thumbnailUrl: String?,
    val modifiedAt: String?,
    val lockedForUser: Boolean,
    val previewUrl: String?,
    val lockExplanation: String?
) {
    constructor(remoteFile: RemoteFile) : this(
        id = remoteFile.id,
        folderId = remoteFile.folderId,
        displayName = remoteFile.displayName,
        fileName = remoteFile.fileName,
        contentType = remoteFile.contentType,
        url = remoteFile.url,
        size = remoteFile.size,
        createdAt = remoteFile.createdAt,
        updatedAt = remoteFile.updatedAt,
        unlockAt = remoteFile.unlockAt,
        locked = remoteFile.locked,
        hidden = remoteFile.hidden,
        lockAt = remoteFile.lockAt,
        hiddenForUser = remoteFile.hiddenForUser,
        thumbnailUrl = remoteFile.thumbnailUrl,
        modifiedAt = remoteFile.modifiedAt,
        lockedForUser = remoteFile.lockedForUser,
        previewUrl = remoteFile.previewUrl,
        lockExplanation = remoteFile.lockExplanation
    )

    fun toApiModel() = RemoteFile(
        id = id,
        folderId = folderId,
        displayName = displayName,
        fileName = fileName,
        contentType = contentType,
        url = url,
        size = size,
        createdAt = createdAt,
        updatedAt = updatedAt,
        unlockAt = unlockAt,
        locked = locked,
        hidden = hidden,
        lockAt = lockAt,
        hiddenForUser = hiddenForUser,
        thumbnailUrl = thumbnailUrl,
        modifiedAt = modifiedAt,
        lockedForUser = lockedForUser,
        previewUrl = previewUrl,
        lockExplanation = lockExplanation
    )
}