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

package com.instructure.pandautils.features.offline.sync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.StudioMediaProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AggregateProgressObserver(
    private val context: Context,
    courseSyncProgressDao: CourseSyncProgressDao,
    fileSyncProgressDao: FileSyncProgressDao,
    studioMediaProgressDao: StudioMediaProgressDao,
    firebaseCrashlytics: FirebaseCrashlytics
) {

    val progressData: LiveData<AggregateProgressViewData?>
        get() = _progressData
    private val _progressData = MutableLiveData<AggregateProgressViewData?>()

    private var courseProgresses = mutableMapOf<Long, CourseSyncProgressEntity>()
    private var fileProgresses = mutableMapOf<Long, FileSyncProgressEntity>()
    private var studioMediaProgresses = mutableListOf<StudioMediaProgressEntity>()

    init {
        GlobalScope.launch(Dispatchers.Main) {
            courseSyncProgressDao.findAllFlow()
                .catch { e ->
                    firebaseCrashlytics.recordException(e)
                    emit(emptyList())
                }
                .collect { entities ->
                    courseProgresses = entities.associateBy { it.courseId }.toMutableMap()
                    calculateProgress()
                }
        }

        GlobalScope.launch(Dispatchers.Main) {
            fileSyncProgressDao.findAllFlow()
                .catch { e ->
                    firebaseCrashlytics.recordException(e)
                    emit(emptyList())
                }
                .collect { entities ->
                    fileProgresses = entities.associateBy { it.fileId }.toMutableMap()
                    calculateProgress()
                }
        }

        GlobalScope.launch(Dispatchers.Main) {
            studioMediaProgressDao.findAllFlow()
                .catch { e ->
                    firebaseCrashlytics.recordException(e)
                    emit(emptyList())
                }
                .collect { entities ->
                    studioMediaProgresses = entities.toMutableList()
                    calculateProgress()
                }
        }
    }

    private fun calculateProgress() {
        val courseProgresses = courseProgresses.values.toList()
        val fileProgresses = fileProgresses.values.toList()

        if (courseProgresses.isEmpty() && fileProgresses.isEmpty()) {
            _progressData.postValue(null)
            return
        }

        val totalSize = courseProgresses.sumOf { it.totalSize() } + fileProgresses.sumOf { it.fileSize } + studioMediaProgresses.sumOf { it.fileSize }
        val downloadedTabSize = courseProgresses.sumOf { it.downloadedSize() }
        val downloadedFileSize = fileProgresses.sumOf { it.fileSize * (it.progress.toDouble() / 100.0)  }
        val downloadedStudioMediaSize = studioMediaProgresses.sumOf { it.fileSize * (it.progress.toDouble() / 100.0)  }
        val downloadedSize = downloadedTabSize + downloadedFileSize.toLong() + downloadedStudioMediaSize.toLong()
        val progress = (downloadedSize.toDouble() / totalSize.toDouble() * 100.0).toInt()

        val itemCount = courseProgresses.size

        val totalSizeString = NumberHelper.readableFileSize(context, totalSize)

        val allProgressStates =
            courseProgresses.map { it.progressState } + fileProgresses.map { it.progressState } + studioMediaProgresses.map { it.progressState }
        val viewData = when {
            courseProgresses.all { it.progressState == ProgressState.STARTING } -> {
                AggregateProgressViewData(
                    title = context.getString(R.string.syncProgress_downloadStarting),
                    progressState = ProgressState.STARTING
                )
            }

            allProgressStates.all { it == ProgressState.COMPLETED } -> {
                AggregateProgressViewData(
                    progressState = ProgressState.COMPLETED,
                    title = context.getString(R.string.syncProgress_downloadSuccess, totalSizeString, totalSizeString),
                    progress = 100
                )
            }

            allProgressStates.all { it.isFinished() } && allProgressStates.any { it == ProgressState.ERROR } -> {
                AggregateProgressViewData(
                    progressState = ProgressState.ERROR,
                    title = context.getString(R.string.syncProgress_syncErrorSubtitle)
                )

            }

            else -> {
                AggregateProgressViewData(
                    title = context.getString(
                        R.string.syncProgress_downloadProgress,
                        NumberHelper.readableFileSize(context, downloadedSize),
                        totalSizeString
                    ),
                    totalSize = totalSizeString,
                    progress = progress,
                    itemCount = itemCount,
                    progressState = ProgressState.IN_PROGRESS
                )
            }
        }

        _progressData.postValue(viewData)
    }
}

data class AggregateProgressViewData(
    val title: String,
    val totalSize: String = "",
    val progress: Int = 0,
    val itemCount: Int = 0,
    val progressState: ProgressState = ProgressState.STARTING
)