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

import android.content.Context
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import com.instructure.horizon.database.entity.HorizonFileSyncPlanEntity
import com.instructure.horizon.offline.sync.HorizonAggregateProgressObserver
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import com.instructure.horizon.offline.sync.HorizonProgressState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SyncingContentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
            mapCourseToUiState(plan, filePlans.filter { it.courseId == plan.courseId })
        }

        SyncingContentUiState(
            courses = courses,
            syncProgress = progressData.progress,
            syncProgressLabel = progressData.progressLabel,
            onCancelSyncClick = ::onCancelSync,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SyncingContentUiState())

    private fun mapCourseToUiState(
        plan: HorizonCourseSyncPlanEntity,
        courseFiles: List<HorizonFileSyncPlanEntity>,
    ): OfflineCourseItemUiState {
        val totalCourseFileSize = courseFiles.sumOf { it.fileSize }
        val courseSizeLabel = if (totalCourseFileSize > 0) {
            "~${Formatter.formatShortFileSize(context, totalCourseFileSize)}"
        } else {
            ""
        }

        return OfflineCourseItemUiState(
            courseId = plan.courseId,
            courseName = plan.courseName,
            courseSizeLabel = courseSizeLabel,
            syncState = when (plan.state) {
                HorizonProgressState.COMPLETED -> CourseSyncState.DONE
                HorizonProgressState.IN_PROGRESS -> CourseSyncState.SYNCING
                HorizonProgressState.ERROR -> CourseSyncState.ERROR
                HorizonProgressState.PENDING -> CourseSyncState.PENDING
            },
            files = courseFiles.map { file ->
                OfflineFileItemUiState(
                    fileId = file.fileId,
                    fileName = file.fileName,
                    fileSizeLabel = if (file.fileSize > 0) {
                        "~${Formatter.formatShortFileSize(context, file.fileSize)}"
                    } else {
                        ""
                    },
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

    private fun onCancelSync() {
        viewModelScope.launch {
            syncHelper.cancelRunningWorkers()
        }
    }
}
