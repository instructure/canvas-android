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

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.work.WorkInfo
import com.instructure.pandautils.BR
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.AdditionalFilesProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.CourseProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.FileSyncProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.FilesTabProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.TabProgressItemViewModel

data class SyncProgressViewData(val items: List<CourseProgressItemViewModel>)

data class CourseProgressViewData(
    val courseName: String,
    val courseId: Long,
    val files: FilesTabProgressItemViewModel?,
    val additionalFiles: AdditionalFilesProgressItemViewModel,
    @Bindable var tabs: List<TabProgressItemViewModel>? = null,
    @Bindable var state: ProgressState = ProgressState.STARTING,
    @Bindable var size: String = "",
    @Bindable var failed: Boolean = false
) : BaseObservable() {

    fun updateState(newState: ProgressState) {
        state = newState
        notifyPropertyChanged(BR.state)

        if (state == ProgressState.ERROR) {
            failed = true
            notifyPropertyChanged(BR.failed)
        }
    }

    fun updateSize(size: String) {
        this.size = size
        notifyPropertyChanged(BR.size)
    }
}

data class TabProgressViewData(
    val tabId: String,
    val tabName: String,
    val courseId: Long,
    @Bindable var state: ProgressState = ProgressState.IN_PROGRESS
) : BaseObservable() {

    fun updateState(newState: ProgressState) {
        state = newState
        notifyPropertyChanged(BR.state)
    }
}

data class FileSyncProgressViewData(
    val fileName: String,
    val fileSize: String,
    val fileId: Long,
    @Bindable var progress: Int,
    @Bindable var state: ProgressState = ProgressState.IN_PROGRESS
) : BaseObservable() {

    fun updateProgress(newProgress: Int) {
        progress = newProgress
        notifyPropertyChanged(BR.progress)
    }

    fun updateState(newState: ProgressState) {
        state = newState
        notifyPropertyChanged(BR.state)
    }
}

data class FileTabProgressViewData(
    val courseId: Long,
    var items: List<FileSyncProgressItemViewModel>,
    @Bindable var totalSize: String = "",
    @Bindable var progress: Int = 0,
    @Bindable var state: ProgressState = ProgressState.IN_PROGRESS,
    @Bindable var toggleable: Boolean = false
) : BaseObservable() {

    fun updateTotalSize(totalSize: String) {
        this.totalSize = totalSize
        notifyPropertyChanged(BR.totalSize)
    }

    fun updateProgress(progress: Int) {
        this.progress = progress
        notifyPropertyChanged(BR.progress)
    }
}

data class AdditionalFilesProgressViewData(
    val courseId: Long,
    @Bindable var totalSize: String = "",
    @Bindable var state: ProgressState = ProgressState.IN_PROGRESS
) : BaseObservable() {

    fun updateTotalSize(totalSize: String) {
        this.totalSize = totalSize
        notifyPropertyChanged(BR.totalSize)
    }
}

enum class ViewType(val viewType: Int) {
    COURSE_PROGRESS(0),
    COURSE_TAB_PROGRESS(1),
    COURSE_FILE_TAB_PROGRESS(2),
    COURSE_FILE_PROGRESS(3),
    COURSE_ADDITIONAL_FILES_PROGRESS(4),
}

sealed class SyncProgressAction {
    object CancelConfirmation : SyncProgressAction()
    object Back : SyncProgressAction()
}