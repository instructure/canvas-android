/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.pandautils.features.dashboard.notifications.itemviewmodels

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.UploadViewData
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker.Companion.PROGRESS_DATA_FULL_SIZE
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker.Companion.PROGRESS_DATA_UPLOADED_SIZE
import com.instructure.pandautils.mvvm.ItemViewModel
import java.util.*

class UploadItemViewModel(
    val workerId: UUID,
    val workManager: WorkManager,
    val data: UploadViewData,
    @get:Bindable var progress: Int = 0,
    val open: (UUID) -> Unit,
    val remove: () -> Unit,
    @get:Bindable var loading: Boolean = false
) : ItemViewModel, BaseObservable() {

    override val layoutId = R.layout.item_dashboard_upload

    private val observer = Observer<WorkInfo> {
        val uploadedSize = it.progress.getLong(PROGRESS_DATA_UPLOADED_SIZE, 0L)
        val fullSize = it.progress.getLong(PROGRESS_DATA_FULL_SIZE, 1L)

        progress = ((uploadedSize.toDouble() / fullSize.toDouble()) * 100.0).toInt()
        notifyPropertyChanged(BR.progress)
    }

    init {
        workManager.getWorkInfoByIdLiveData(workerId).observeForever(observer)
    }

    fun clear() {
        workManager.getWorkInfoByIdLiveData(workerId).removeObserver(observer)
    }

    fun open() = open.invoke(workerId)

    fun remove() = remove.invoke()
}