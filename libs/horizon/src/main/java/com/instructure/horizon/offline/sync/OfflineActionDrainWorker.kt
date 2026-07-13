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
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.FeatureFlagProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

@HiltWorker
class OfflineActionDrainWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val featureFlagProvider: FeatureFlagProvider,
    private val apiPrefs: ApiPrefs,
    private val actionSyncers: Set<@JvmSuppressWildcards OfflineActionSyncer>,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        if (!featureFlagProvider.offlineEnabled()) return Result.success()

        val enqueuedUserId = inputData.getLong(USER_ID_KEY, -1L)
        val currentUserId = apiPrefs.user?.id ?: -1L
        if (enqueuedUserId != -1L && currentUserId != -1L && enqueuedUserId != currentUserId) {
            return Result.success()
        }

        var anyFailed = false
        for (syncer in actionSyncers) {
            coroutineContext.ensureActive()
            try {
                syncer.drain()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                anyFailed = true
            }
        }

        return if (anyFailed) Result.retry() else Result.success()
    }

    companion object {
        const val TAG = "HorizonOfflineActionDrain"
        const val USER_ID_KEY = "userId"
    }
}
