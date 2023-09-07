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
import androidx.work.WorkQuery
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.progress.CourseProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.fromJson
import java.util.UUID

data class CourseProgressItemViewModel(
    val data: CourseProgressViewData,
    val workManager: WorkManager
) :
    GroupItemViewModel(collapsable = true, items = (data.tabs + data.files), collapsed = false) {

    override val layoutId: Int = R.layout.item_course_progress

    override val viewType: Int = ViewType.COURSE_PROGRESS.viewType

    private var courseProgressLiveData: LiveData<WorkInfo>? = null

    private var aggregateProgressLiveData: LiveData<List<WorkInfo>>? = null

    private val aggregateProgressObserver = Observer<List<WorkInfo>> {
        when {
            it.all { it.state == WorkInfo.State.SUCCEEDED } -> {
                data.updateState(WorkInfo.State.SUCCEEDED)
                clearAggregateObserver()
            }
            it.any { it.state == WorkInfo.State.CANCELLED || it.state == WorkInfo.State.FAILED } -> {
                data.updateState(WorkInfo.State.FAILED)
                clearAggregateObserver()
            }
            else -> {
                data.updateState(WorkInfo.State.RUNNING)
            }
        }
    }

    private val progressObserver = Observer<WorkInfo> {
        val courseProgress: CourseProgress = if (it.state.isFinished) {
            it.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson() ?: return@Observer
        } else {
            it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson() ?: return@Observer
        }

        if (courseProgress.fileProgresses == null) return@Observer

        aggregateProgressLiveData = workManager.getWorkInfosLiveData(WorkQuery.fromIds(courseProgress.fileProgresses.map { UUID.fromString(it.workerId) } + UUID.fromString(
            data.workerId
        )))

        aggregateProgressLiveData?.observeForever(aggregateProgressObserver)
        clearCourseObserver()
    }

    init {
        courseProgressLiveData = workManager.getWorkInfoByIdLiveData(UUID.fromString(data.workerId))
        courseProgressLiveData?.observeForever(progressObserver)
    }

    private fun clearCourseObserver() {
        courseProgressLiveData?.removeObserver(progressObserver)
    }

    private fun clearAggregateObserver() {
        aggregateProgressLiveData?.removeObserver(aggregateProgressObserver)
    }
}