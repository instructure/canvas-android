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

package com.instructure.pandautils.features.offline.sync.progress

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.TabProgressItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel

data class SyncProgressViewData(val items: List<ItemViewModel>)

data class CourseProgressViewData(
    val courseName: String,
    val tabs: List<TabProgressItemViewModel>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseProgressViewData

        if (courseName != other.courseName) return false
        if (tabs != other.tabs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseName.hashCode()
        result = 31 * result + tabs.hashCode()
        return result
    }
}

data class TabProgressViewData(
    val tabName: String,
    val state: ProgressState
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TabProgressViewData

        if (tabName != other.tabName) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tabName.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }
}

enum class ViewType(val viewType: Int) {
    COURSE_PROGRESS(0),
    COURSE_TAB_PROGRESS(1),
    COURSE_FILE_PROGRESS(2)
}