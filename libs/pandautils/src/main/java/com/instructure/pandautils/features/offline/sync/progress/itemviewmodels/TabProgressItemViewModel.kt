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
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.progress.TabProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.fromJson
import java.util.UUID

data class TabProgressItemViewModel(val data: TabProgressViewData, val workManager: WorkManager) : ItemViewModel,
    SyncProgressItemViewModel {
    override val layoutId = R.layout.item_tab_progress

    override val viewType = ViewType.COURSE_TAB_PROGRESS.viewType

    private val progressLiveData = workManager.getWorkInfoByIdLiveData(UUID.fromString(data.workerId))

    private val progressObserver = Observer<WorkInfo> {
        val progress = if (it.state.isFinished) {
            it.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson<CourseProgress>() ?: return@Observer
        } else {
            it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>() ?: return@Observer
        }

        progress.tabs[data.tabId]?.let { tabProgress ->
            data.updateState(tabProgress.state)
        }
    }

    init {
        progressLiveData.observeForever(progressObserver)
    }

    override fun onCleared() {
        progressLiveData.removeObserver(progressObserver)
    }
}