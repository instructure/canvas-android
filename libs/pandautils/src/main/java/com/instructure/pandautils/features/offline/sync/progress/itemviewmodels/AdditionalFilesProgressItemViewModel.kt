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
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.FileSyncData
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.AdditionalFilesProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.fromJson
import java.util.UUID

data class AdditionalFilesProgressItemViewModel(
    val data: AdditionalFilesProgressViewData,
    private val workManager: WorkManager,
    private val context: Context
) : ItemViewModel {
    override val layoutId = R.layout.item_additional_files_progress

    override val viewType = ViewType.COURSE_ADDITIONAL_FILES_PROGRESS.viewType

    private var totalFilesProgressLiveData: LiveData<List<WorkInfo>>? = null

    private val filesCounted = mutableSetOf<String>()
    var totalSize = 0L
        private set

    private val totalFilesProgressObserver = Observer<List<WorkInfo>> {
        var totalProgress = 0
        if (it.all { it.state.isFinished }) {
            data.state = ProgressState.COMPLETED
            data.notifyPropertyChanged(BR.state)
        }

        it.forEach { workInfo ->
            val progress: FileSyncProgress? = if (workInfo.state.isFinished) {
                workInfo.outputData.getString(FileSyncWorker.OUTPUT)?.fromJson()
            } else {
                workInfo.progress.getString(FileSyncWorker.PROGRESS)?.fromJson()
            }
            if (progress != null && !filesCounted.contains(progress.fileName) && progress.totalBytes > 0) {
                filesCounted.add(progress.fileName)
                totalSize += progress.totalBytes
            }
            totalProgress += progress?.progress ?: 0
        }
        data.updateProgress(totalProgress / it.size)
        data.updateTotalSize(NumberHelper.readableFileSize(context, totalSize))
    }

    private val courseProgressLiveData = workManager.getWorkInfoByIdLiveData(UUID.fromString(data.courseWorkerId))

    private val courseProgressObserver = Observer<WorkInfo> {
        val progress = if (it.state.isFinished) {
            it.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson<CourseProgress>() ?: return@Observer
        } else {
            it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>() ?: return@Observer
        }

        if (progress.additionalFileSyncData == null) return@Observer

        if (progress.additionalFileSyncData.isEmpty()) {
            data.state = ProgressState.COMPLETED
            data.notifyPropertyChanged(BR.state)
        } else {
            startObserving(progress.additionalFileSyncData)
        }
    }

    init {
        courseProgressLiveData.observeForever(courseProgressObserver)
    }

    private fun startObserving(fileSyncData: List<FileSyncData>) {
        val workerIds = mutableListOf<UUID>()
        fileSyncData.forEach {
            workerIds.add(UUID.fromString(it.workerId))
        }

        totalFilesProgressLiveData = workManager.getWorkInfosLiveData(WorkQuery.fromIds(workerIds))
        totalFilesProgressLiveData?.observeForever(totalFilesProgressObserver)
    }

    override fun onCleared() {
        courseProgressLiveData.removeObserver(courseProgressObserver)
        totalFilesProgressLiveData?.removeObserver(totalFilesProgressObserver)
    }
}