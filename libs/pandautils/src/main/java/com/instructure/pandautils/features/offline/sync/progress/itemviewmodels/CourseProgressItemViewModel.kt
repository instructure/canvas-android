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
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.CourseProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import java.util.UUID

data class CourseProgressItemViewModel(
    val data: CourseProgressViewData,
    val workManager: WorkManager
) :
    GroupItemViewModel(collapsable = true, items = (data.tabs + data.files), collapsed = false) {

    override val layoutId: Int = R.layout.item_course_progress

    override val viewType: Int = ViewType.COURSE_PROGRESS.viewType

    private val progressObserver = Observer<WorkInfo> {
        data.updateState(it.state)
    }

    init {
        workManager.getWorkInfoByIdLiveData(UUID.fromString(data.workerId)).observeForever(progressObserver)
    }

    fun clearObserver() {
        workManager.getWorkInfoByIdLiveData(UUID.fromString(data.workerId)).removeObserver(progressObserver)
    }
}