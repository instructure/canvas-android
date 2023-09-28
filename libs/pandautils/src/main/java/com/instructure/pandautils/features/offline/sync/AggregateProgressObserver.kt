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
import androidx.work.WorkQuery
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.TAB_PROGRESS_SIZE
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity
import com.instructure.pandautils.utils.fromJson
import java.util.UUID

class AggregateProgressObserver(
    private val workManager: WorkManager,
    private val context: Context,
    syncProgressDao: SyncProgressDao
) {

    val progressData: LiveData<AggregateProgressViewData>
        get() = _progressData
    private val _progressData = MutableLiveData<AggregateProgressViewData>()

    private var aggregateProgressLiveData: LiveData<List<WorkInfo>>? = null
    private var courseProgressLiveData: LiveData<List<WorkInfo>>? = null

    private val syncProgressObserver = Observer<List<SyncProgressEntity>> {
        courseProgressLiveData?.removeObserver(courseProgressObserver)
        aggregateProgressLiveData?.removeObserver(aggregateProgressObserver)
        val workerIds = it.map { UUID.fromString(it.uuid) }
        if (workerIds.isEmpty()) {
            _progressData.postValue(
                AggregateProgressViewData(
                    title = "",
                    progressState = ProgressState.COMPLETED
                )
            )
        } else {
            setCourseWorkerIds(workerIds)
        }
    }

    private val aggregateProgressObserver = object : Observer<List<WorkInfo>> {
        override fun onChanged(value: List<WorkInfo>) {

            when {
                value.all { it.state == WorkInfo.State.SUCCEEDED } -> {
                    val totalSize = _progressData.value?.totalSize.orEmpty()
                    aggregateProgressLiveData?.removeObserver(this)
                    _progressData.value?.copy(
                        progressState = ProgressState.COMPLETED,
                        title = context.getString(R.string.syncProgress_downloadSuccess, totalSize, totalSize),
                        progress = 100
                    )?.let {
                        _progressData.postValue(it)
                    }
                    return
                }

                value.all { it.state.isFinished } && value.any { it.state == WorkInfo.State.FAILED } -> {
                    aggregateProgressLiveData?.removeObserver(this)
                    _progressData.value?.copy(
                        progressState = ProgressState.ERROR,
                        title = context.getString(R.string.syncProgress_syncErrorSubtitle)
                    )?.let {
                        _progressData.postValue(it)
                    }
                    return
                }
            }

            val courseWorkInfos = value.filter { it.tags.contains(CourseSyncWorker.TAG) }
            val fileWorkInfos = value.filter { it.tags.contains(FileSyncWorker.TAG) }

            var totalSize = 0L
            var filesSize = 0L
            var downloadedTabSize = 0L
            var fileProgressSum = 0
            var itemCount = fileWorkInfos.size

            courseWorkInfos.forEach {
                val courseProgress = if (it.state.isFinished) {
                    it.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson<CourseProgress>() ?: return@forEach
                } else {
                    it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>()
                        ?: return@forEach
                }

                val tabSize = courseProgress.tabs.count() * TAB_PROGRESS_SIZE
                val courseFileSizes = courseProgress.fileSyncData?.sumOf { it.fileSize } ?: 0
                val courseSize = tabSize + courseFileSizes

                totalSize += courseSize
                filesSize += courseFileSizes
                downloadedTabSize += courseProgress.tabs.count { it.value.state == ProgressState.COMPLETED } * TAB_PROGRESS_SIZE
                itemCount += courseProgress.tabs.count()
            }

            fileWorkInfos.forEach {
                val fileProgress = if (it.state.isFinished) {
                    it.outputData.getString(FileSyncWorker.OUTPUT)?.fromJson<FileSyncProgress>() ?: return@forEach
                } else {
                    it.progress.getString(FileSyncWorker.PROGRESS)?.fromJson<FileSyncProgress>() ?: return@forEach
                }

                fileProgressSum += fileProgress.progress
            }

            val fileProgress = if (fileWorkInfos.isEmpty()) 100 else fileProgressSum / fileWorkInfos.size
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
    }

    private val courseProgressObserver = Observer<List<WorkInfo>> {
        val startedCourses = it.filter {
            it.state.isFinished || it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)
                ?.fromJson<CourseProgress>()?.fileSyncData != null
        }.toSet()

        val workerIds = mutableSetOf<UUID>()

        startedCourses.forEach {
            workerIds.add(it.id)

            val progress = if (it.state.isFinished) {
                it.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson<CourseProgress>()
            } else {
                it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>()
            }

            progress?.fileSyncData?.map { UUID.fromString(it.workerId) }?.let {
                workerIds.addAll(it)
            }
        }

        if (workerIds.isNotEmpty()) {
            aggregateProgressLiveData?.removeObserver(aggregateProgressObserver)
            aggregateProgressLiveData = workManager.getWorkInfosLiveData(WorkQuery.fromIds(workerIds.toList()))
            aggregateProgressLiveData?.observeForever(aggregateProgressObserver)
        } else {
            _progressData.postValue(
                AggregateProgressViewData(
                    title = context.getString(R.string.syncProgress_downloadStarting),
                    progressState = ProgressState.STARTING
                )
            )
        }
    }

    init {
        syncProgressDao.findCourseProgressesLiveData().observeForever(syncProgressObserver)
    }

    private fun setCourseWorkerIds(workerIds: List<UUID>) {
        if (workerIds.isEmpty()) return

        courseProgressLiveData = workManager.getWorkInfosLiveData(WorkQuery.fromIds(workerIds))
        courseProgressLiveData?.observeForever(courseProgressObserver)
    }

    fun onCleared() {
        courseProgressLiveData?.removeObserver(courseProgressObserver)
        aggregateProgressLiveData?.removeObserver(aggregateProgressObserver)
    }
}

data class AggregateProgressViewData(
    val title: String,
    val totalSize: String = "",
    val progress: Int = 0,
    val itemCount: Int = 0,
    val progressState: ProgressState = ProgressState.STARTING
)