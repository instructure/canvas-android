/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.offline

import androidx.lifecycle.asFlow
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.instructure.pandautils.domain.usecase.BaseFlowUseCase
import com.instructure.pandautils.features.offline.sync.OfflineSyncWorker
import com.instructure.pandautils.utils.FeatureFlagProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveOfflineSyncUpdatesUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val featureFlagProvider: FeatureFlagProvider
) : BaseFlowUseCase<Unit, Unit>() {

    private val runningWorkers = mutableSetOf<String>()

    override fun execute(params: Unit): Flow<Unit> = flow {
        if (!featureFlagProvider.offlineEnabled()) {
            return@flow
        }

        val workQuery = WorkQuery.Builder
            .fromTags(listOf(OfflineSyncWorker.PERIODIC_TAG, OfflineSyncWorker.ONE_TIME_TAG))
            .build()

        workManager.getWorkInfosLiveData(workQuery).asFlow()
            .collect { workInfos ->
                workInfos.forEach { workInfo ->
                    if (workInfo.state == State.RUNNING) {
                        runningWorkers.add(workInfo.id.toString())
                    }
                }

                if (workInfos.any {
                    (it.state == State.SUCCEEDED || it.state == State.FAILED) &&
                    runningWorkers.contains(it.id.toString())
                }) {
                    emit(Unit)
                    runningWorkers.clear()
                }
            }
    }
}