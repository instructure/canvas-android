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
package com.instructure.horizon.features.account.manageofflinecontent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.offline.sync.HorizonAggregateProgressObserver
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import com.instructure.horizon.offline.sync.HorizonProgressState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SyncingContentViewModel @Inject constructor(
    private val aggregateProgressObserver: HorizonAggregateProgressObserver,
    private val syncHelper: HorizonOfflineSyncHelper,
    courseSyncPlanDao: HorizonCourseSyncPlanDao,
    fileSyncPlanDao: HorizonFileSyncPlanDao,
) : ViewModel() {

    val uiState: StateFlow<SyncingContentUiState> = combine(
        aggregateProgressObserver.progressData,
        courseSyncPlanDao.findAllFlow(),
        fileSyncPlanDao.findAllFlow(),
    ) { progressData, coursePlans, filePlans ->
        val courses = coursePlans.map { plan ->
            val courseFiles = filePlans.filter { it.courseId == plan.courseId }
            OfflineCourseItemUiState(
                courseId = plan.courseId,
                courseName = plan.courseName,
                offlineState = when (plan.state) {
                    HorizonProgressState.COMPLETED -> CourseOfflineState.ALL
                    HorizonProgressState.ERROR -> CourseOfflineState.PARTIAL
                    else -> CourseOfflineState.ALL
                },
                files = courseFiles.map { file ->
                    OfflineFileItemUiState(
                        fileId = file.fileId,
                        fileName = file.fileName,
                        fileSizeLabel = "",
                        isSelected = true,
                        syncState = when (file.state) {
                            HorizonProgressState.COMPLETED -> FileSyncState.DONE
                            HorizonProgressState.IN_PROGRESS -> FileSyncState.SYNCING
                            else -> FileSyncState.PENDING
                        },
                    )
                },
            )
        }

        SyncingContentUiState(
            courses = courses,
            syncProgress = progressData.progress,
            syncProgressLabel = progressData.progressLabel,
            onCancelSyncClick = ::onCancelSync,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SyncingContentUiState())

    private fun onCancelSync() {
        syncHelper.cancelRunningWorkers()
    }
}
