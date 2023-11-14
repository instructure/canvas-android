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
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.features.offline.sync.progress.CourseProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.TabProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity

data class CourseProgressItemViewModel(
    val data: CourseProgressViewData,
    private val context: Context,
    private val courseSyncProgressDao: CourseSyncProgressDao,
    private val fileSyncProgressDao: FileSyncProgressDao
) :
    GroupItemViewModel(collapsable = true, items = emptyList(), collapsed = true) {

    override val layoutId: Int = R.layout.item_course_progress

    override val viewType: Int = ViewType.COURSE_PROGRESS.viewType

    private var courseProgressLiveData: LiveData<CourseSyncProgressEntity?>? = null
    private var fileProgressLiveData: LiveData<List<FileSyncProgressEntity>>? = null

    private var courseSyncProgressEntity: CourseSyncProgressEntity? = null
    private val fileProgresses = mutableMapOf<Long, FileSyncProgressEntity>()

    private val fileProgressObserver = Observer<List<FileSyncProgressEntity>> {
        it.forEach { fileProgress ->
            fileProgresses[fileProgress.fileId] = fileProgress
        }

        updateProgress()
    }

    private val progressObserver = Observer<CourseSyncProgressEntity?> { courseProgress ->
        if (courseProgress == null) return@Observer

        courseSyncProgressEntity = courseProgress

        if (data.tabs == null && courseProgress.tabs.isNotEmpty()) {
            createTabs(courseProgress.tabs, courseProgress.courseId)
        }

        updateProgress()
    }

    init {
        courseProgressLiveData = courseSyncProgressDao.findByCourseIdLiveData(data.courseId)
        courseProgressLiveData?.observeForever(progressObserver)

        fileProgressLiveData = fileSyncProgressDao.findByCourseIdLiveData(data.courseId)
        fileProgressLiveData?.observeForever(fileProgressObserver)
    }

    private fun createTabs(tabs: Map<String, TabSyncData>, courseId: Long) {
        val tabViewModels = tabs.map { tabEntry ->
            TabProgressItemViewModel(
                TabProgressViewData(
                    tabEntry.key,
                    tabEntry.value.tabName,
                    courseId,
                    tabEntry.value.state
                ),
                courseSyncProgressDao
            )
        }

        data.tabs = tabViewModels
        items = tabViewModels + listOf(data.files, data.additionalFiles).filterNotNull()
        data.notifyPropertyChanged(BR.tabs)
    }

    private fun updateProgress() {
        val fileProgresses = fileProgresses.values
        data.updateSize(
            NumberHelper.readableFileSize(
                context,
                (courseSyncProgressEntity?.totalSize() ?: 0)
                        + fileProgresses.sumOf { it.fileSize })
        )

        when {
            courseSyncProgressEntity?.progressState == ProgressState.COMPLETED && fileProgresses.all { it.progressState == ProgressState.COMPLETED } -> {
                data.updateState(ProgressState.COMPLETED)
            }

            courseSyncProgressEntity?.progressState?.isFinished() == true && fileProgresses.all { it.progressState.isFinished() }
                    && (courseSyncProgressEntity?.progressState == ProgressState.ERROR) || fileProgresses.any { it.progressState == ProgressState.ERROR } -> {
                data.updateState(ProgressState.ERROR)
            }

            else -> {
                data.updateState(ProgressState.IN_PROGRESS)
            }
        }
    }

    private fun clearObservers() {
        courseProgressLiveData?.removeObserver(progressObserver)
        fileProgressLiveData?.removeObserver(fileProgressObserver)
    }

    override fun onCleared() {
        clearObservers()
        data.tabs?.forEach { it.onCleared() }
        data.files?.onCleared()
        data.additionalFiles.onCleared()
    }
}