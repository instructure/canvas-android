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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.TAB_PROGRESS_SIZE
import com.instructure.pandautils.room.offline.daos.CourseProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity

class AggregateProgressObserver(
    private val workManager: WorkManager,
    private val context: Context,
    syncProgressDao: SyncProgressDao,
    private val courseProgressDao: CourseProgressDao,
    private val fileSyncProgressDao: FileSyncProgressDao
) {

    val progressData: LiveData<AggregateProgressViewData>
        get() = _progressData
    private val _progressData = MutableLiveData<AggregateProgressViewData>()

    private var courseProgressLiveData: LiveData<List<CourseProgressEntity>>? = null
    private var fileProgressLiveData: LiveData<List<FileSyncProgressEntity>>? = null

    private val courseProgresses = mutableMapOf<String, CourseProgressEntity>()
    private val fileProgresses = mutableMapOf<String, FileSyncProgressEntity>()

    private val syncProgressObserver = Observer<List<SyncProgressEntity>> {
        courseProgressLiveData?.removeObserver(courseProgressObserver)
        val courseIds = it.map { it.courseId }
        if (courseIds.isEmpty()) {
            _progressData.postValue(
                AggregateProgressViewData(
                    title = "",
                    progressState = ProgressState.COMPLETED
                )
            )
        } else {
            setCourseWorkerIds(courseIds)
        }
    }

    private val courseProgressObserver = Observer<List<CourseProgressEntity>> {
        val startedCourses = it.filter { it.progressState != ProgressState.IN_PROGRESS }

        startedCourses.forEach {
            courseProgresses[it.workerId] = it
        }

        calculateProgress()
    }

    private val fileProgressObserver = Observer<List<FileSyncProgressEntity>> {
        it.forEach {
            fileProgresses[it.workerId] = it
        }

        calculateProgress()
    }

    init {
        syncProgressDao.findCourseProgressesLiveData().observeForever(syncProgressObserver)
    }

    private fun setCourseWorkerIds(courseIds: List<Long>) {
        if (courseIds.isEmpty()) return

        courseProgressLiveData = courseProgressDao.findByCourseIdsLiveData(courseIds)
        courseProgressLiveData?.observeForever(courseProgressObserver)

        fileProgressLiveData = fileSyncProgressDao.findByCourseIdsLiveData(courseIds)
        fileProgressLiveData?.observeForever(fileProgressObserver)
    }

    private fun calculateProgress() {
        val courseProgresses = courseProgresses.values.toList()
        val fileProgresses = fileProgresses.values.toList()

        when {
            courseProgresses.all { it.progressState == ProgressState.COMPLETED } && fileProgresses.all { it.progressState == ProgressState.COMPLETED } -> {
                val totalSize = _progressData.value?.totalSize.orEmpty()
                _progressData.value?.copy(
                    progressState = ProgressState.COMPLETED,
                    title = context.getString(R.string.syncProgress_downloadSuccess, totalSize, totalSize),
                    progress = 100
                )?.let {
                    _progressData.postValue(it)
                }
                return
            }

            fileProgresses.all { it.progressState.isFinished() } && courseProgresses.all { it.progressState.isFinished() }
                    && (courseProgresses.any { it.progressState == ProgressState.ERROR } || fileProgresses.any { it.progressState == ProgressState.ERROR }) -> {
                _progressData.value?.copy(
                    progressState = ProgressState.ERROR,
                    title = context.getString(R.string.syncProgress_syncErrorSubtitle)
                )?.let {
                    _progressData.postValue(it)
                }
                return
            }
        }

        var totalSize = 0L

        var downloadedTabSize = 0L
        var itemCount = fileProgresses.size

        courseProgresses.forEach { courseProgress ->

            val tabSize = courseProgress.tabs.count() * TAB_PROGRESS_SIZE
            totalSize += tabSize

            downloadedTabSize += courseProgress.tabs.count { it.value.state == ProgressState.COMPLETED } * TAB_PROGRESS_SIZE
            itemCount += courseProgress.tabs.count()
        }

        val filesSize = fileProgresses.sumOf { it.fileSize }
        val fileProgressSum = fileProgresses.sumOf { it.progress }

        val fileProgress = if (fileProgresses.isEmpty()) 100 else fileProgressSum / fileProgresses.size
        val downloadedFileSize = filesSize.toDouble() * (fileProgress.toDouble() / 100.0)
        val downloadedSize = downloadedTabSize + downloadedFileSize.toLong()
        val progress = (downloadedSize.toDouble() / totalSize.toDouble() * 100.0).toInt()

        _progressData.postValue(
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
        )

    }

    fun onCleared() {
        courseProgressLiveData?.removeObserver(courseProgressObserver)
    }
}

data class AggregateProgressViewData(
    val title: String,
    val totalSize: String = "",
    val progress: Int = 0,
    val itemCount: Int = 0,
    val progressState: ProgressState = ProgressState.STARTING
)