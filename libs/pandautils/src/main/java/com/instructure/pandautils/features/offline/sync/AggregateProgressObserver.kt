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
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity

class AggregateProgressObserver(
    private val context: Context,
    courseSyncProgressDao: CourseSyncProgressDao,
    fileSyncProgressDao: FileSyncProgressDao
) {

    val progressData: LiveData<AggregateProgressViewData?>
        get() = _progressData
    private val _progressData = MutableLiveData<AggregateProgressViewData?>()

    private var courseProgressLiveData: LiveData<List<CourseSyncProgressEntity>>? = null
    private var fileProgressLiveData: LiveData<List<FileSyncProgressEntity>>? = null

    private var courseProgresses = mutableMapOf<Long, CourseSyncProgressEntity>()
    private var fileProgresses = mutableMapOf<Long, FileSyncProgressEntity>()

    private val courseProgressObserver = Observer<List<CourseSyncProgressEntity>> {
        courseProgresses = it.associateBy { it.courseId }.toMutableMap()

        calculateProgress()
    }

    private val fileProgressObserver = Observer<List<FileSyncProgressEntity>> {
        fileProgresses = it.associateBy { it.fileId }.toMutableMap()

        calculateProgress()
    }

    init {
        courseProgressLiveData = courseSyncProgressDao.findAllLiveData()
        courseProgressLiveData?.observeForever(courseProgressObserver)

        fileProgressLiveData = fileSyncProgressDao.findAllLiveData()
        fileProgressLiveData?.observeForever(fileProgressObserver)
    }

    private fun calculateProgress() {
        val courseProgresses = courseProgresses.values.toList()
        val fileProgresses = fileProgresses.values.toList()

        if (courseProgresses.isEmpty() && fileProgresses.isEmpty()) {
            _progressData.postValue(null)
            return
        }

        val totalSize = courseProgresses.sumOf { it.totalSize() } + fileProgresses.sumOf { it.fileSize }
        val downloadedTabSize = courseProgresses.sumOf { it.downloadedSize() }
        val downloadedFileSize = fileProgresses.sumOf { it.fileSize * (it.progress.toDouble() / 100.0)  }
        val downloadedSize = downloadedTabSize + downloadedFileSize.toLong()
        val progress = (downloadedSize.toDouble() / totalSize.toDouble() * 100.0).toInt()

        val itemCount = courseProgresses.size

        val viewData = when {
            courseProgresses.all { it.progressState == ProgressState.STARTING } -> {
                AggregateProgressViewData(
                    title = context.getString(R.string.syncProgress_downloadStarting),
                    progressState = ProgressState.STARTING
                )

            }

            courseProgresses.all { it.progressState == ProgressState.COMPLETED } && fileProgresses.all { it.progressState == ProgressState.COMPLETED } -> {
                val totalSizeString = NumberHelper.readableFileSize(context, totalSize)
                AggregateProgressViewData(
                    progressState = ProgressState.COMPLETED,
                    title = context.getString(R.string.syncProgress_downloadSuccess, totalSizeString, totalSizeString),
                    progress = 100
                )

            }

            fileProgresses.all { it.progressState.isFinished() } && courseProgresses.all { it.progressState.isFinished() }
                    && (courseProgresses.any { it.progressState == ProgressState.ERROR } || fileProgresses.any { it.progressState == ProgressState.ERROR }) -> {
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
                        NumberHelper.readableFileSize(context, totalSize)
                    ),
                    totalSize = NumberHelper.readableFileSize(context, totalSize),
                    progress = progress,
                    itemCount = itemCount,
                    progressState = ProgressState.IN_PROGRESS
                )
            }
        }

        _progressData.postValue(viewData)
    }

    fun onCleared() {
        courseProgressLiveData?.removeObserver(courseProgressObserver)
        fileProgressLiveData?.removeObserver(fileProgressObserver)
    }
}

data class AggregateProgressViewData(
    val title: String,
    val totalSize: String = "",
    val progress: Int = 0,
    val itemCount: Int = 0,
    val progressState: ProgressState = ProgressState.STARTING
)