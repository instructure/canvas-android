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

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.progress.FileSyncProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity

data class FileSyncProgressItemViewModel(
    val data: FileSyncProgressViewData,
    val fileSyncProgressDao: FileSyncProgressDao
) : ItemViewModel {
    override val layoutId = R.layout.item_file_sync_progress

    override val viewType = ViewType.COURSE_FILE_PROGRESS.viewType

    private var fileSyncProgressLiveData: LiveData<FileSyncProgressEntity?>? = null

    private val fileSyncProgressObserver = Observer<FileSyncProgressEntity?> { progress ->
        progress?.let { notifyChange(it) }
    }

    init {
        fileSyncProgressLiveData = fileSyncProgressDao.findByFileIdLiveData(data.fileId)
        fileSyncProgressLiveData?.observeForever(fileSyncProgressObserver)
    }

    private fun notifyChange(fileSyncProgress: FileSyncProgressEntity) {
        data.updateProgress(fileSyncProgress.progress)
        data.updateState(fileSyncProgress.progressState)
    }

    override fun onCleared() {
        fileSyncProgressLiveData?.removeObserver(fileSyncProgressObserver)
    }
}