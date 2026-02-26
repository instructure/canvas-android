/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.progress

import android.content.res.Resources
import androidx.lifecycle.asFlow
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.AggregateProgressViewData
import com.instructure.pandautils.features.offline.sync.ProgressState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveSyncProgressUseCase @Inject constructor(
    private val aggregateProgressObserver: AggregateProgressObserver,
    private val resources: Resources
) {
    operator fun invoke(): Flow<SyncProgressItem?> {
        return aggregateProgressObserver.progressData.asFlow()
            .map { aggregateProgress ->
                aggregateProgress?.let { data ->
                    if (data.progressState == ProgressState.COMPLETED) {
                        null
                    } else {
                        createSyncProgressItem(data)
                    }
                }
            }
    }

    private fun createSyncProgressItem(data: AggregateProgressViewData): SyncProgressItem {
        val (title, subtitle) = when (data.progressState) {
            ProgressState.IN_PROGRESS -> {
                Pair(
                    resources.getString(R.string.syncProgress_syncingOfflineContent),
                    resources.getQuantityString(
                        R.plurals.syncProgress_itemCount,
                        data.itemCount,
                        data.itemCount
                    )
                )
            }
            ProgressState.ERROR -> {
                Pair(
                    resources.getString(R.string.syncProgress_offlineContentSyncFailed),
                    resources.getString(R.string.syncProgress_syncErrorSubtitle)
                )
            }
            ProgressState.COMPLETED -> {
                Pair(
                    resources.getString(R.string.syncProgress_offlineContentSyncCompleted),
                    ""
                )
            }
            ProgressState.STARTING -> {
                Pair(
                    resources.getString(R.string.syncProgress_downloadStarting),
                    resources.getString(R.string.syncProgress_syncQueued)
                )
            }
        }

        return SyncProgressItem(
            title = title,
            subtitle = subtitle,
            progress = data.progress,
            state = data.progressState,
            itemCount = data.itemCount
        )
    }
}