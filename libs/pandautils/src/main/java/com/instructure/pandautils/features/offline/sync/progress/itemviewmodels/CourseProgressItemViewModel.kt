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

    private var totalSizeWithoutExternalFiles = 0L

    private var courseProgressLiveData: LiveData<WorkInfo>? = null

    private var aggregateProgressLiveData: LiveData<List<WorkInfo>>? = null

    private val aggregateProgressObserver = Observer<List<WorkInfo>> {
        data.updateSize(NumberHelper.readableFileSize(context, totalSizeWithoutExternalFiles + data.additionalFiles.totalSize))
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

        if (data.tabs == null && courseProgress.tabs.isNotEmpty()) {
            createTabs(courseProgress.tabs, it.id.toString())
        }

        if (courseProgress.fileSyncData == null && courseProgress.additionalFileSyncData == null) return@Observer

        val fileSnycDataSize = courseProgress.fileSyncData?.sumOf { it.fileSize } ?: 0
        totalSizeWithoutExternalFiles = courseProgress.tabs.size * TAB_PROGRESS_SIZE + fileSnycDataSize

        data.updateSize(NumberHelper.readableFileSize(context, totalSizeWithoutExternalFiles + data.additionalFiles.totalSize))

        aggregateProgressLiveData =
            workManager.getWorkInfosLiveData(WorkQuery.fromIds(courseProgress.fileSyncData?.map { UUID.fromString(it.workerId) }.orEmpty() +
                courseProgress.additionalFileSyncData?.map { UUID.fromString(it.workerId) }.orEmpty() + UUID.fromString(
                data.workerId
            )))

        aggregateProgressLiveData?.removeObserver(aggregateProgressObserver)
        aggregateProgressLiveData?.observeForever(aggregateProgressObserver)
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
        items = tabViewModels + listOf(data.files, data.additionalFiles).filterNotNull()
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
        data.files?.onCleared()
        data.additionalFiles.onCleared()
    }
}