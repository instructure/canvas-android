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

package com.instructure.pandautils.features.dashboard.notifications.itemviewmodels

import android.content.res.Resources
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.SyncProgressViewData
import com.instructure.pandautils.features.offline.sync.AggregateProgressViewData
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.mvvm.ItemViewModel

data class SyncProgressItemViewModel(
    val data: SyncProgressViewData,
    val onClick: () -> Unit,
    val onDismiss: () -> Unit,
    private val resources: Resources
) : ItemViewModel {

    override val layoutId: Int = R.layout.item_sync_progress

    fun update(progressData: AggregateProgressViewData) {
        when (progressData.progressState) {
            ProgressState.IN_PROGRESS -> {
                data.title = resources.getString(R.string.syncProgress_syncingOfflineContent)
                data.subtitle = resources.getQuantityString(
                    R.plurals.syncProgress_itemCount,
                    progressData.itemCount,
                    progressData.itemCount
                )
            }

            ProgressState.ERROR -> {
                data.title = resources.getString(R.string.syncProgress_offlineContentSyncFailed)
                data.subtitle = resources.getString(R.string.syncProgress_syncErrorSubtitle)
            }

            ProgressState.COMPLETED -> {
                data.title = resources.getString(R.string.syncProgress_offlineContentSyncCompleted)
                data.subtitle = ""
            }

            ProgressState.STARTING -> {
                data.title = resources.getString(R.string.syncProgress_downloadStarting)
                data.subtitle = resources.getString(R.string.syncProgress_syncQueued)
            }
        }

        data.progress = progressData.progress
        data.progressState = progressData.progressState

        data.notifyChange()
    }
}