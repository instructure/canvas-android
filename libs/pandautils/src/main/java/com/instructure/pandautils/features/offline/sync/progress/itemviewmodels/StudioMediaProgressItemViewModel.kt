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
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.StudioMediaProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.ViewType
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import com.instructure.pandautils.room.offline.entities.StudioMediaProgressEntity

data class StudioMediaProgressItemViewModel(
    val data: StudioMediaProgressViewData,
    private val studioMediaProgressDao: StudioMediaProgressDao,
    private val context: Context
) : ItemViewModel {
    override val layoutId = R.layout.item_studio_media_progress

    override val viewType = ViewType.STUDIO_MEDIA_PROGRESS.viewType

    private val studioMediaProgressObserver = Observer<List<StudioMediaProgressEntity>> { studioMediaProgressEntities ->
        if (studioMediaProgressEntities.isEmpty()) return@Observer

        data.visible = true
        data.notifyPropertyChanged(BR.visible)

        when {
            studioMediaProgressEntities.all { it.progressState == ProgressState.COMPLETED } -> {
                data.state = ProgressState.COMPLETED
                data.notifyPropertyChanged(BR.state)
            }

            studioMediaProgressEntities.any { it.progressState == ProgressState.ERROR } -> {
                data.state = ProgressState.ERROR
                data.notifyPropertyChanged(BR.state)
            }
        }

        val totalSize = studioMediaProgressEntities.sumOf { it.fileSize }
        data.updateTotalSize(NumberHelper.readableFileSize(context, totalSize))
    }

    private val studioMediaProgressLiveData = studioMediaProgressDao.findAllLiveData()

    init {
        studioMediaProgressLiveData.observeForever(studioMediaProgressObserver)
    }

    override fun onCleared() {
        studioMediaProgressLiveData.removeObserver(studioMediaProgressObserver)
    }
}