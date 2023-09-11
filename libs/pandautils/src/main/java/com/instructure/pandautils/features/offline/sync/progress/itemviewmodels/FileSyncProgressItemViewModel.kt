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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.progress.FileSyncProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.fromJson
import java.util.UUID

data class FileSyncProgressItemViewModel(
    val data: FileSyncProgressViewData,
    val workManager: WorkManager
) : ItemViewModel, SyncProgressItemViewModel {
    override val layoutId = R.layout.item_file_sync_progress

    override val viewType = ViewType.COURSE_FILE_PROGRESS.viewType

    private var fileSyncProgressLiveData: LiveData<WorkInfo>? = null

    private val fileSyncProgressObserver = Observer<WorkInfo> {
        val progress: FileSyncProgress = if (it.state.isFinished) {
            it.outputData.getString(FileSyncWorker.OUTPUT)?.fromJson() ?: return@Observer
        } else {
            it.progress.getString(FileSyncWorker.PROGRESS)?.fromJson() ?: return@Observer
        }
        notifyChange(progress)
    }

    init {
        fileSyncProgressLiveData = workManager.getWorkInfoByIdLiveData(UUID.fromString(data.workerId))
        fileSyncProgressLiveData?.observeForever(fileSyncProgressObserver)
    }

    private fun notifyChange(fileSyncProgress: FileSyncProgress) {
        data.updateProgress(fileSyncProgress.progress)
        data.updateState(fileSyncProgress.progressState)
    }

    override fun onCleared() {
        fileSyncProgressLiveData?.removeObserver(fileSyncProgressObserver)
    }
}