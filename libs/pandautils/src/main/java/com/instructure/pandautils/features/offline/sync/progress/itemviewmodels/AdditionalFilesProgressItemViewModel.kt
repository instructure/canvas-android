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
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.AdditionalFilesProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity

data class AdditionalFilesProgressItemViewModel(
    val data: AdditionalFilesProgressViewData,
    private val courseSyncProgressDao: CourseSyncProgressDao,
    private val fileSyncProgressDao: FileSyncProgressDao,
    private val context: Context
) : ItemViewModel {
    override val layoutId = R.layout.item_additional_files_progress

    override val viewType = ViewType.COURSE_ADDITIONAL_FILES_PROGRESS.viewType

    private val totalFilesProgressObserver = Observer<List<FileSyncProgressEntity>> {
        when {
            it.all { it.progressState == ProgressState.COMPLETED } -> {
                data.state = ProgressState.COMPLETED
                data.notifyPropertyChanged(BR.state)
            }

            it.any { it.progressState == ProgressState.ERROR } -> {
                data.state = ProgressState.ERROR
                data.notifyPropertyChanged(BR.state)
            }
        }

        val totalSize = it.sumOf { it.fileSize }

        data.updateTotalSize(NumberHelper.readableFileSize(context, totalSize))
    }

    private val courseProgressLiveData = courseSyncProgressDao.findByCourseIdLiveData(data.courseId)
    private var fileProgressLiveData: LiveData<List<FileSyncProgressEntity>>? = null

    private val courseProgressObserver = Observer<CourseSyncProgressEntity?> { progress ->
        if (progress == null) return@Observer
        if (!progress.additionalFilesStarted) return@Observer

        fileProgressLiveData = fileSyncProgressDao.findAdditionalFilesByCourseIdLiveData(progress.courseId)
        fileProgressLiveData?.observeForever(totalFilesProgressObserver)
    }

    init {
        courseProgressLiveData.observeForever(courseProgressObserver)
    }

    override fun onCleared() {
        courseProgressLiveData.removeObserver(courseProgressObserver)
        fileProgressLiveData?.removeObserver(totalFilesProgressObserver)
    }
}