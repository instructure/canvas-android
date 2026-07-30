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
package com.instructure.horizon.offline.sync

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.data.datasource.ModuleItemNetworkDataSource
import com.instructure.horizon.database.dao.PendingActionDao
import com.instructure.horizon.database.entity.PendingActionEntity
import com.instructure.horizon.offline.offlineReplayRestParams
import javax.inject.Inject

class MarkAsDoneActionSyncer @Inject constructor(
    pendingActionDao: PendingActionDao,
    private val networkDataSource: ModuleItemNetworkDataSource,
) : PendingActionSyncer(pendingActionDao) {

    override val actionType = MARK_AS_DONE_ACTION

    override suspend fun replay(action: PendingActionEntity): DataResult<*> {
        val payload = gson.fromJson(action.payload, MarkAsDonePayload::class.java)
        val params = offlineReplayRestParams()
        return if (payload.done) {
            networkDataSource.markAsDone(payload.courseId, payload.moduleId, payload.itemId, params)
        } else {
            networkDataSource.markAsNotDone(payload.courseId, payload.moduleId, payload.itemId, params)
        }
    }
}
