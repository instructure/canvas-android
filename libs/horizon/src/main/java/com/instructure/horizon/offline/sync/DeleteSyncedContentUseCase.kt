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

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.HorizonDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteSyncedContentUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: HorizonDatabase,
    private val apiPrefs: ApiPrefs,
    private val syncHelper: HorizonOfflineSyncHelper,
) {
    suspend operator fun invoke() {
        syncHelper.cancelRunningWorkers()
        syncHelper.cancelPeriodicSync()

        withContext(Dispatchers.IO) {
            database.clearAllTables()

            val userId = apiPrefs.user?.id ?: return@withContext
            val userDir = File(context.filesDir, userId.toString())
            if (userDir.exists()) {
                userDir.deleteRecursively()
            }
        }
    }
}
