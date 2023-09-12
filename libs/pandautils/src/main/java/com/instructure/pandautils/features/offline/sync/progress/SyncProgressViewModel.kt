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
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.CourseProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.FilesTabProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.TabProgressItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity
import com.instructure.pandautils.room.offline.entities.TabEntity
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
    private val tabDao: TabDao,
    private val offlineSyncHelper: OfflineSyncHelper
) : ViewModel() {

    val data: LiveData<SyncProgressViewData>
        get() = _data
    private val _data = MutableLiveData<SyncProgressViewData>()

    val progressData: LiveData<AggregateProgressViewData>
        get() = _progressData
    private val _progressData = MutableLiveData<AggregateProgressViewData>()

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>(ViewState.Loading)

    val events: LiveData<Event<SyncProgressAction>>
        get() = _events
    private val _events = MutableLiveData<Event<SyncProgressAction>>()

    private val courseIds = mutableListOf<Long>()

    private var aggregateProgressLiveData: LiveData<List<WorkInfo>>? = null
    private var courseProgressLiveData: LiveData<List<WorkInfo>>? = null

    private val aggregateProgressObserver = object : Observer<List<WorkInfo>> {
        override fun onChanged(value: List<WorkInfo>) {

            when {
                value.all { it.state == WorkInfo.State.SUCCEEDED } -> {
                    aggregateProgressLiveData?.removeObserver(this)
                    _state.postValue(ViewState.Success)
                }

                value.all { it.state.isFinished } && value.any { it.state == WorkInfo.State.FAILED } -> {
                    aggregateProgressLiveData?.removeObserver(this)
                    _state.postValue(ViewState.Error())
                }
            }

            val courseWorkInfos = value.filter { it.tags.contains(CourseSyncWorker.TAG) }
            val fileWorkInfos = value.filter { it.tags.contains(FileSyncWorker.TAG) }

            var totalSize = 0L
            var filesSize = 0L
            var downloadedTabSize = 0L
            var fileProgressSum = 0

            courseWorkInfos.forEach {
                val courseProgress = if (it.state.isFinished) {
                    it.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson<CourseProgress>() ?: return@forEach
                } else {
                    it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>()
                        ?: return@forEach
                }

                val tabSize = courseProgress.tabs.count() * 100 * 1000
                val courseFileSizes = courseProgress.fileSyncData?.map { it.fileSize }?.sum() ?: 0
                val courseSize = tabSize + courseFileSizes

                totalSize += courseSize
                filesSize += courseFileSizes
                downloadedTabSize += courseProgress.tabs.count { it.value.state == ProgressState.COMPLETED } * 100 * 1000
            }

            fileWorkInfos.forEach {
                val fileProgress = if (it.state.isFinished) {
                    it.outputData.getString(FileSyncWorker.OUTPUT)?.fromJson<FileSyncProgress>() ?: return@forEach
                } else {
                    it.progress.getString(FileSyncWorker.PROGRESS)?.fromJson<FileSyncProgress>() ?: return@forEach
                }

                fileProgressSum += fileProgress.progress
            }

            val fileProgress = if (fileWorkInfos.isEmpty()) 100 else fileProgressSum / fileWorkInfos.size
            val downloadedFileSize = filesSize.toDouble() * (fileProgress.toDouble() / 100.0)
            val downloadedSize = downloadedTabSize + downloadedFileSize.toLong()
            val progress = (downloadedSize.toDouble() / totalSize.toDouble() * 100.0).toInt()

            _progressData.postValue(
                AggregateProgressViewData(
                    totalSize = NumberHelper.readableFileSize(context, totalSize),
                    downloadedSize = NumberHelper.readableFileSize(context, downloadedSize),
                    progress = progress,
                    queued = 0
                )
            )
        }
    }

    private val courseProgressObserver = Observer<List<WorkInfo>> {
        val startedCourses = it.filter {
            it.state.isFinished || it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)
                ?.fromJson<CourseProgress>()?.fileSyncData != null
        }.toSet()

        val workerIds = mutableSetOf<UUID>()

        startedCourses.forEach {
            workerIds.add(it.id)

            val progress = if (it.state.isFinished) {
                it.outputData.getString(CourseSyncWorker.OUTPUT)?.fromJson<CourseProgress>()
            } else {
                it.progress.getString(CourseSyncWorker.COURSE_PROGRESS)?.fromJson<CourseProgress>()
            }

            progress?.fileSyncData?.map { UUID.fromString(it.workerId) }?.let {
                workerIds.addAll(it)
            }
        }

        if (workerIds.isNotEmpty()) {
            aggregateProgressLiveData?.removeObserver(aggregateProgressObserver)
            aggregateProgressLiveData = workManager.getWorkInfosLiveData(WorkQuery.fromIds(workerIds.toList()))
            aggregateProgressLiveData?.observeForever(aggregateProgressObserver)
        }
    }

    init {
        viewModelScope.launch {
            val courseSyncProgresses = syncProgressDao.findCourseProgresses()
            if (courseSyncProgresses.isEmpty()) {
                _events.postValue(Event(SyncProgressAction.Back))
                return@launch
            }
            courseIds.addAll(courseSyncProgresses.map { it.courseId })

            val workerIds = courseSyncProgresses.map { UUID.fromString(it.uuid) }
            courseProgressLiveData = workManager.getWorkInfosLiveData(WorkQuery.fromIds(workerIds))
            courseProgressLiveData?.observeForever(courseProgressObserver)

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

    private fun createTabItem(tab: TabEntity, workerId: String): TabProgressItemViewModel {
        val data = TabProgressViewData(
            tabId = tab.id,
            tabName = tab.label.orEmpty(),
            workerId = workerId
        )

        return TabProgressItemViewModel(data, workManager)
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
        when (_state.value) {
            is ViewState.Error -> {
                viewModelScope.launch {
                    syncProgressDao.deleteAll()
                }
                retry()
                _events.postValue(Event(SyncProgressAction.Back))
            }

            is ViewState.Loading -> _events.postValue(Event(SyncProgressAction.CancelConfirmation))

            else -> Unit
        }
    }

    override fun onCleared() {
        super.onCleared()
        aggregateProgressLiveData?.removeObserver(aggregateProgressObserver)
        courseProgressLiveData?.removeObserver(courseProgressObserver)
        _data.value?.items?.map {
            val itemViewModels = mutableListOf<ItemViewModel>()
            itemViewModels.add(it)
            it.data.tabs?.let { tabs -> itemViewModels.addAll(tabs) }
            itemViewModels.addAll(it.data.files)
            itemViewModels
        }
            ?.flatten()
            ?.forEach {
                it.onCleared()
            }

    }
}