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
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.FileSyncProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.FileTabProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import java.util.UUID

data class FilesTabProgressItemViewModel(
    val data: FileTabProgressViewData,
    private val context: Context,
    private val courseSyncProgressDao: CourseSyncProgressDao,
    private val fileSyncProgressDao: FileSyncProgressDao
) : GroupItemViewModel(collapsable = true, items = data.items, collapsed = true) {
    override val layoutId = R.layout.item_file_tab_progress

    override val viewType = ViewType.COURSE_FILE_TAB_PROGRESS.viewType

    private var fileProgressLiveData: LiveData<List<FileSyncProgressEntity>>? = null
    private val courseProgressLiveData = courseSyncProgressDao.findByCourseIdLiveData(data.courseId)

    private val fileProgressObserver = Observer<List<FileSyncProgressEntity>> { progresses ->
        if (progresses.isEmpty()) {
            data.state = ProgressState.COMPLETED
            data.notifyPropertyChanged(BR.state)
        } else {
            if (data.items.isEmpty()) {
                createFileItems(progresses)
                data.toggleable = true
                data.notifyPropertyChanged(BR.toggleable)
            }

            if (progresses.all { it.progressState.isFinished() }) {
                when {
                    progresses.all { it.progressState == ProgressState.COMPLETED } -> {
                        data.state = ProgressState.COMPLETED
                        data.notifyPropertyChanged(BR.state)
                    }

                    progresses.any { it.progressState == ProgressState.ERROR } -> {
                        data.state = ProgressState.ERROR
                        data.notifyPropertyChanged(BR.state)
                    }
                }
            }

            val totalProgress = progresses.sumOf { it.progress }

            data.updateProgress(totalProgress / progresses.size)
        }
    }

    private val courseProgressObserver = Observer<CourseSyncProgressEntity?> { progress ->
        if (progress == null) return@Observer
        if (progress.progressState == ProgressState.STARTING) return@Observer

        fileProgressLiveData = fileSyncProgressDao.findCourseFilesByCourseIdLiveData(progress.courseId)
        fileProgressLiveData?.observeForever(fileProgressObserver)
    }

    init {
        courseProgressLiveData.observeForever(courseProgressObserver)
    }

    private fun createFileItems(fileSyncData: List<FileSyncProgressEntity>) {
        val fileItems = mutableListOf<FileSyncProgressItemViewModel>()
        var totalSize = 0L
        fileSyncData.forEach {
            val item = FileSyncProgressItemViewModel(
                data = FileSyncProgressViewData(
                    fileName = it.fileName,
                    fileSize = NumberHelper.readableFileSize(context, it.fileSize),
                    progress = 0,
                    fileId = it.fileId,
                ),
                fileSyncProgressDao = fileSyncProgressDao
            )
            fileItems.add(item)
            totalSize += it.fileSize
        }

        data.items = fileItems
        items = data.items
        data.updateTotalSize(NumberHelper.readableFileSize(context, totalSize))
    }

    override fun onCleared() {
        courseProgressLiveData.removeObserver(courseProgressObserver)
        fileProgressLiveData?.removeObserver(fileProgressObserver)
    }
}