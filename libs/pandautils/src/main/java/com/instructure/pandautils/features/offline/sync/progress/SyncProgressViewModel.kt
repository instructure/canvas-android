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
import androidx.work.WorkQuery
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.AggregateProgressViewData
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.CourseProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.FilesTabProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.TAB_PROGRESS_SIZE
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity
import com.instructure.pandautils.utils.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val offlineSyncHelper: OfflineSyncHelper,
    private val aggregateProgressObserver: AggregateProgressObserver
) : ViewModel() {

    val data: LiveData<SyncProgressViewData>
        get() = _data
    private val _data = MutableLiveData<SyncProgressViewData>()

    val progressData: LiveData<AggregateProgressViewData>
        get() = aggregateProgressObserver.progressData

    val events: LiveData<Event<SyncProgressAction>>
        get() = _events
    private val _events = MutableLiveData<Event<SyncProgressAction>>()

    private val courseIds = mutableListOf<Long>()

    init {
        viewModelScope.launch {
            val courseSyncProgresses = syncProgressDao.findCourseProgresses()
            if (courseSyncProgresses.isEmpty()) {
                _events.postValue(Event(SyncProgressAction.Back))
                return@launch
            }
            courseIds.addAll(courseSyncProgresses.map { it.courseId })

            val courses = courseSyncProgresses.map {
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
            size = context.getString(R.string.syncProgress_syncQueued),
            files = if (courseSyncSettings?.files?.isNotEmpty() == true || courseSyncSettings?.courseSyncSettings?.fullFileSync == true) {
                listOf(
                    FilesTabProgressItemViewModel(
                        data = FileTabProgressViewData(courseWorkerId = syncProgress.uuid, items = emptyList()),
                        workManager = workManager,
                        context = context
                    )
                )
            } else {
                emptyList()
            }
        )

        return CourseProgressItemViewModel(data, workManager, context)
    }

    fun cancel() {
        cancelRunningWorkers()
        viewModelScope.launch {
            syncProgressDao.deleteAll()
        }
        _events.postValue(Event(SyncProgressAction.Back))
    }

    private fun cancelRunningWorkers() {
        workManager.cancelAllWorkByTag(CourseSyncWorker.TAG)
        workManager.cancelAllWorkByTag(FileSyncWorker.TAG)
    }

    private fun retry() {
        offlineSyncHelper.syncOnce(courseIds)
    }

    fun onActionClicked() {
        when (progressData.value?.progressState) {
            ProgressState.ERROR -> {
                viewModelScope.launch {
                    syncProgressDao.deleteAll()
                }
                retry()
                _events.postValue(Event(SyncProgressAction.Back))
            }

            ProgressState.IN_PROGRESS -> _events.postValue(Event(SyncProgressAction.CancelConfirmation))

            else -> Unit
        }
    }

    override fun onCleared() {
        super.onCleared()
        aggregateProgressObserver.onCleared()
        _data.value?.items?.forEach { it.onCleared() }
    }
}