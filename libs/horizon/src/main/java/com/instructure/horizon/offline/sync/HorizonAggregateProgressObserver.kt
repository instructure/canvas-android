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

import android.text.format.Formatter
import android.content.Context
import com.instructure.horizon.R
import com.instructure.horizon.database.HorizonDatabaseProvider
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import com.instructure.horizon.database.entity.HorizonFileSyncPlanEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

data class HorizonSyncProgressData(
    val progressState: HorizonProgressState = HorizonProgressState.PENDING,
    val progress: Float = 0f,
    val progressLabel: String = "",
    val courseCount: Int = 0,
    val isActive: Boolean = false,
)

@Singleton
class HorizonAggregateProgressObserver @Inject constructor(
    // Provider keeps DAO refs fresh across user switch (DB is rebuilt per user).
    private val courseSyncPlanDaoProvider: Provider<HorizonCourseSyncPlanDao>,
    private val fileSyncPlanDaoProvider: Provider<HorizonFileSyncPlanDao>,
    @ApplicationContext private val context: Context,
    databaseProvider: HorizonDatabaseProvider,
    private val syncHelper: HorizonOfflineSyncHelper,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val sourceRevision = MutableStateFlow(0)

    init {
        databaseProvider.addOnDatabaseChangedListener {
            sourceRevision.value += 1
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val progressData: StateFlow<HorizonSyncProgressData> = sourceRevision
        .flatMapLatest {
            combine(
                courseSyncPlanDaoProvider.get().findAllFlow().distinctUntilChanged(),
                fileSyncPlanDaoProvider.get().findAllFlow().distinctUntilChanged(),
                syncHelper.isRunning().distinctUntilChanged(),
            ) { courses, files, workerRunning ->
                calculateProgress(courses, files, workerRunning)
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), HorizonSyncProgressData())

    private fun calculateProgress(
        courses: List<HorizonCourseSyncPlanEntity>,
        files: List<HorizonFileSyncPlanEntity>,
        workerRunning: Boolean,
    ): HorizonSyncProgressData {
        if (courses.isEmpty()) return HorizonSyncProgressData()

        val courseTotalSize = courses.sumOf { it.totalSize }
        val courseDownloadedSize = courses.sumOf { it.downloadedSize }

        val fileTotalSize = files.sumOf { it.fileSize }
        val fileDownloadedSize = files.sumOf { (it.fileSize * it.progress) / 100 }

        val totalSize = courseTotalSize + fileTotalSize
        val downloadedSize = courseDownloadedSize + fileDownloadedSize

        val progress = if (totalSize > 0) downloadedSize.toFloat() / totalSize else 0f

        val allStates = courses.map { it.state }
        val allFinished = allStates.all { it.isFinished() }
        val anyInProgress = allStates.any { it == HorizonProgressState.IN_PROGRESS }
        val anyRunning = allStates.any { it.isRunning() }
        val anyError = allStates.any { it == HorizonProgressState.ERROR }
        val anyPartial = allStates.any { it == HorizonProgressState.PARTIAL }

        val progressState = when {
            !allFinished && anyRunning -> HorizonProgressState.IN_PROGRESS
            !allFinished -> HorizonProgressState.PENDING
            anyError || anyPartial -> HorizonProgressState.ERROR
            else -> HorizonProgressState.COMPLETED
        }

        val progressLabel = when {
            progressState == HorizonProgressState.COMPLETED -> context.getString(
                R.string.offline_syncCompleted,
                Formatter.formatShortFileSize(context, downloadedSize),
            )
            progressState == HorizonProgressState.ERROR -> context.getString(R.string.offline_syncError)
            !anyInProgress -> context.getString(R.string.offline_syncWaiting)
            else -> context.getString(R.string.offline_syncInProgress)
        }

        return HorizonSyncProgressData(
            progressState = progressState,
            progress = progress.coerceIn(0f, 1f),
            progressLabel = progressLabel,
            courseCount = courses.size,
            isActive = workerRunning,
        )
    }
}
