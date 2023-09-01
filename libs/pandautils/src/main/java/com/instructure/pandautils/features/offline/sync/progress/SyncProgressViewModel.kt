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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.CourseProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.TabProgressItemViewModel
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.utils.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SyncProgressViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val syncProgressDao: SyncProgressDao
) : ViewModel() {

    val data: LiveData<SyncProgressViewData>
        get() = _data
    private val _data = MutableLiveData<SyncProgressViewData>()

    private val courseProgresses = mutableMapOf<Long, CourseProgress>()

    private val courseObserver = Observer<WorkInfo> {
        val progress = it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>() ?: return@Observer

        courseProgresses[progress.courseId] = progress
        createCourseItems()
    }

    init {
        viewModelScope.launch {
            syncProgressDao.findCourseProgresses().map {
                workManager.getWorkInfoByIdLiveData(UUID.fromString(it.uuid))
            }.forEach {
                it.observeForever(courseObserver)
            }
        }
    }

    private fun createCourseItems() {
        val courses = courseProgresses.values.toList()
            .map { createCourseItem(it) }

        _data.postValue(SyncProgressViewData(courses))
    }

    private fun createCourseItem(progress: CourseProgress): CourseProgressItemViewModel {
        val data = CourseProgressViewData(
            courseName = progress.courseName,
            tabs = progress.tabs.map { createTabItem(it) },
        )

        return CourseProgressItemViewModel(data)
    }

    private fun createTabItem(tab: Map.Entry<String, ProgressState>): TabProgressItemViewModel {
        val data = TabProgressViewData(
            tabName = tab.key,
            state = tab.value
        )

        return TabProgressItemViewModel(data)
    }

}