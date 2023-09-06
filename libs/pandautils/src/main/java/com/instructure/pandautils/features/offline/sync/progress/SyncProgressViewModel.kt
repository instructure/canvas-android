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

import android.annotation.SuppressLint
import android.content.Context
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
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.FileSyncProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.FileTabProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.TabProgressItemViewModel
import com.instructure.pandautils.features.shareextension.progress.itemviewmodels.FileProgressItemViewModel
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity
import com.instructure.pandautils.room.offline.entities.TabEntity
import com.instructure.pandautils.utils.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class SyncProgressViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val syncProgressDao: SyncProgressDao,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val tabDao: TabDao,
) : ViewModel() {

    val data: LiveData<SyncProgressViewData>
        get() = _data
    private val _data = MutableLiveData<SyncProgressViewData>()

    init {
        viewModelScope.launch {
            val courses = syncProgressDao.findCourseProgresses().map {
                createCourseItem(it)
            }
            _data.postValue(SyncProgressViewData(courses))
        }
    }

    private suspend fun createCourseItem(syncProgress: SyncProgressEntity): CourseProgressItemViewModel {
        val courseSyncSettings = courseSyncSettingsDao.findWithFilesById(syncProgress.courseId)
        val data = CourseProgressViewData(
            courseName = syncProgress.title,
            workerId = syncProgress.uuid,
            tabs = createTabItems(syncProgress.courseId, syncProgress.uuid),
            files = if (courseSyncSettings?.files?.isNotEmpty() == true || courseSyncSettings?.courseSyncSettings?.fullFileSync == true)
                listOf(
                    FileTabProgressItemViewModel(
                        FileTabProgressViewData(courseWorkerId = syncProgress.uuid, items = emptyList()),
                        workManager,
                        context
                    )
                ) else emptyList()
        )

        return CourseProgressItemViewModel(data, workManager)
    }

    private suspend fun createTabItems(courseId: Long, workerId: String): List<TabProgressItemViewModel> {
        val courseSettings = courseSyncSettingsDao.findById(courseId) ?: return emptyList()
        val courseTabs = tabDao.findByCourseId(courseId)
        return courseTabs.filter { tab ->
            courseSettings.tabs[tab.id] == true
        }
            .map { createTabItem(it, workerId) }

    }

    private fun createTabItem(tab: TabEntity, workerId: String): TabProgressItemViewModel {
        val data = TabProgressViewData(
            tabId = tab.id,
            tabName = tab.label.orEmpty(),
            workerId = workerId
        )

        return TabProgressItemViewModel(data, workManager)
    }

}