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

import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
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

data class FileTabProgressItemViewModel(
    val data: FileTabProgressViewData,
    private val workManager: WorkManager
) : GroupItemViewModel(collapsable = true, items = data.items, collapsed = true) {
    override val layoutId = R.layout.item_file_tab_progress

    override val viewType = ViewType.COURSE_FILE_TAB_PROGRESS.viewType

    init {
        val progressLiveData = workManager.getWorkInfoByIdLiveData(UUID.fromString(data.courseWorkerId))

        progressLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(value: WorkInfo) {
                val progress = if (value.state.isFinished) {
                    value.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson<CourseProgress>() ?: return
                } else {
                    value.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>() ?: return
                }

                if (progress.fileWorkerIds == null) return

                if (progress.fileWorkerIds.isEmpty()) {
                    data.state = ProgressState.COMPLETED
                    data.notifyPropertyChanged(BR.state)
                } else {
                    createFileItems(progress.fileWorkerIds)
                    data.toggelable = true
                    data.notifyPropertyChanged(BR.toggelable)
                    toggleItems()
                }

                progressLiveData.removeObserver(this)
            }
        })
    }

    private fun createFileItems(fileWorkerIds: List<String>) {
        data.items = fileWorkerIds.map {
            FileSyncProgressItemViewModel(
                FileSyncProgressViewData(
                    "File name",
                    0,
                    it
                ),
                workManager
            )
        }
        items = data.items
        toggleItems()
    }
}