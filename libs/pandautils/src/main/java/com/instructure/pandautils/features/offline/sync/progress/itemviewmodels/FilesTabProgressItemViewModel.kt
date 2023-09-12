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

package com.instructure.pandautils.features.offline.sync.progress.itemviewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.FileSyncProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.FileTabProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.utils.fromJson
import java.util.UUID
import com.instructure.pandautils.BR
import com.instructure.pandautils.features.offline.sync.FileSyncData
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker

data class FilesTabProgressItemViewModel(
    val data: FileTabProgressViewData,
    private val workManager: WorkManager,
    private val context: Context
) : GroupItemViewModel(collapsable = true, items = data.items, collapsed = true) {
    override val layoutId = R.layout.item_file_tab_progress

    override val viewType = ViewType.COURSE_FILE_TAB_PROGRESS.viewType

    private var totalProgressLiveData: LiveData<List<WorkInfo>>? = null

    private val totalProgressObserver = Observer<List<WorkInfo>> {
        var totalProgress = 0
        if (it.all { it.state.isFinished }) {
            data.state = ProgressState.COMPLETED
            data.notifyPropertyChanged(BR.state)
        } else {
            it.forEach { workInfo ->
                val progress: FileSyncProgress? = if (workInfo.state.isFinished) {
                    workInfo.outputData.getString(FileSyncWorker.OUTPUT)?.fromJson()
                } else {
                    workInfo.progress.getString(FileSyncWorker.PROGRESS)?.fromJson()
                }
                totalProgress += progress?.progress ?: 0
            }
            data.updateProgress(totalProgress / it.size)
        }
    }

    private val progressLiveData = workManager.getWorkInfoByIdLiveData(UUID.fromString(data.courseWorkerId))

    private val progressObserver = Observer<WorkInfo> {
        val progress = if (it.state.isFinished) {
            it.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson<CourseProgress>() ?: return@Observer
        } else {
            it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>() ?: return@Observer
        }

        if (progress.fileSyncData == null) return@Observer

        if (progress.fileSyncData.isEmpty()) {
            data.state = ProgressState.COMPLETED
            data.notifyPropertyChanged(BR.state)
        } else {
            createFileItems(progress.fileSyncData)
            data.toggleable = true
            data.notifyPropertyChanged(BR.toggleable)
        }
    }

    init {
        progressLiveData.observeForever(progressObserver)
    }

    private fun createFileItems(fileSyncData: List<FileSyncData>) {
        val fileItems = mutableListOf<FileSyncProgressItemViewModel>()
        var totalSize = 0L
        val workerIds = mutableListOf<UUID>()
        fileSyncData.forEach {
            val item = FileSyncProgressItemViewModel(
                data = FileSyncProgressViewData(
                    fileName = it.fileName,
                    fileSize = NumberHelper.readableFileSize(context, it.fileSize),
                    progress = 0,
                    workerId = it.workerId,
                ),
                workManager = workManager
            )
            workerIds.add(UUID.fromString(it.workerId))
            fileItems.add(item)
            totalSize += it.fileSize
        }

        totalProgressLiveData = workManager.getWorkInfosLiveData(WorkQuery.fromIds(workerIds))
        totalProgressLiveData?.observeForever(totalProgressObserver)

        data.items = fileItems
        items = data.items
        data.updateTotalSize(NumberHelper.readableFileSize(context, totalSize))
    }

    override fun onCleared() {
        progressLiveData.removeObserver(progressObserver)
        totalProgressLiveData?.removeObserver(totalProgressObserver)
    }
}