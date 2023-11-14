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
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.progress.TabProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity

data class TabProgressItemViewModel(
    val data: TabProgressViewData,
    val courseSyncProgressDao: CourseSyncProgressDao
) : ItemViewModel {
    override val layoutId = R.layout.item_tab_progress

    override val viewType = ViewType.COURSE_TAB_PROGRESS.viewType

    private val progressLiveData = courseSyncProgressDao.findByCourseIdLiveData(data.courseId)

    private val progressObserver = Observer<CourseSyncProgressEntity?> { progress ->
        progress?.tabs?.get(data.tabId)?.let { tabProgress ->
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