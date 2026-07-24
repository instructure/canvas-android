/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.offline

import android.net.Uri
import com.instructure.horizon.database.dao.PendingActionDao
import com.instructure.horizon.database.entity.PendingActionEntity
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import javax.inject.Inject
import javax.inject.Singleton

data class PendingActionAttachment(val uri: Uri, val fileName: String)

@Singleton
class HorizonOfflineActionQueue @Inject constructor(
    private val pendingActionDao: PendingActionDao,
    private val fileStaging: HorizonPendingFileStaging,
    private val syncHelper: HorizonOfflineSyncHelper,
) {

    suspend fun enqueue(
        actionType: String,
        payload: String,
        courseId: Long? = null,
        attachments: List<PendingActionAttachment> = emptyList(),
    ): Long {
        val id = pendingActionDao.insert(
            PendingActionEntity(
                actionType = actionType,
                courseId = courseId,
                payload = payload,
                createdAt = System.currentTimeMillis(),
            )
        )

        try {
            attachments.forEach { fileStaging.stage(id, it.uri, it.fileName) }
        } catch (e: Exception) {
            pendingActionDao.delete(id)
            fileStaging.delete(id)
            throw e
        }

        syncHelper.enqueueActionDrain()
        return id
    }
}
