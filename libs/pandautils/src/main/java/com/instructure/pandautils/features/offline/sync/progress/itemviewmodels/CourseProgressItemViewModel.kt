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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.features.offline.sync.progress.CourseProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.TabProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.utils.fromJson
import java.util.UUID

const val TAB_PROGRESS_SIZE = 100 * 1000

data class CourseProgressItemViewModel(
    val data: CourseProgressViewData,
    private val workManager: WorkManager,
    private val context: Context
) :
    GroupItemViewModel(collapsable = true, items = emptyList(), collapsed = true) {

    override val layoutId: Int = R.layout.item_course_progress

    override val viewType: Int = ViewType.COURSE_PROGRESS.viewType

    private var courseProgressLiveData: LiveData<WorkInfo>? = null

    private var aggregateProgressLiveData: LiveData<List<WorkInfo>>? = null

    private val aggregateProgressObserver = Observer<List<WorkInfo>> {
        val fileWorkProgresses = it.filter { it.tags.contains(FileSyncWorker.TAG) }.map {
            if (it.state.isFinished) {
                it.outputData.getString(FileSyncWorker.OUTPUT)?.fromJson<FileSyncProgress>()
            } else {
                it.progress.getString(FileSyncWorker.PROGRESS)?.fromJson<FileSyncProgress>()
            }
        }

        when {
            it.all { it.state == WorkInfo.State.SUCCEEDED }
                    && fileWorkProgresses.all { it?.progressState == ProgressState.COMPLETED } -> {
                data.updateState(WorkInfo.State.SUCCEEDED)
                clearAggregateObserver()
            }

            it.all { it.state.isFinished }
                    && (it.any { it.state == WorkInfo.State.CANCELLED || it.state == WorkInfo.State.FAILED }
                    || fileWorkProgresses.any { it?.progressState == ProgressState.ERROR }) -> {
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

        if (data.tabs == null && courseProgress.tabs.isNotEmpty()) {
            createTabs(courseProgress.tabs, it.id.toString())
        }

        if (courseProgress.fileSyncData == null) return@Observer

        data.updateSize(
            NumberHelper.readableFileSize(
                context,
                courseProgress.tabs.size * TAB_PROGRESS_SIZE + courseProgress.fileSyncData.sumOf { it.fileSize })
        )

        aggregateProgressLiveData =
            workManager.getWorkInfosLiveData(WorkQuery.fromIds(courseProgress.fileSyncData.map { UUID.fromString(it.workerId) } + UUID.fromString(
                data.workerId
            )))

        aggregateProgressLiveData?.observeForever(aggregateProgressObserver)
        clearCourseObserver()
    }

    init {
        courseProgressLiveData = workManager.getWorkInfoByIdLiveData(UUID.fromString(data.workerId))
        courseProgressLiveData?.observeForever(progressObserver)
    }

    private fun createTabs(tabs: Map<String, TabSyncData>, courseWorkerId: String) {
        val tabViewModels = tabs.map { tabEntry ->
            TabProgressItemViewModel(
                TabProgressViewData(
                    tabEntry.key,
                    tabEntry.value.tabName,
                    courseWorkerId,
                    tabEntry.value.state
                ),
                workManager
            )
        }

        data.tabs = tabViewModels
        items = tabViewModels + data.files
        data.notifyPropertyChanged(BR.tabs)
    }

    private fun clearCourseObserver() {
        courseProgressLiveData?.removeObserver(progressObserver)
    }

    private fun clearAggregateObserver() {
        aggregateProgressLiveData?.removeObserver(aggregateProgressObserver)
    }

    override fun onCleared() {
        clearCourseObserver()
        clearAggregateObserver()
        data.tabs?.forEach { it.onCleared() }
        data.files.forEach { it.onCleared() }
    }
}