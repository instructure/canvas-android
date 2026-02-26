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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.features.offline.sync.ProgressState
import java.util.UUID

data class ProgressUiState(
    val loading: Boolean = true,
    val uploadItems: List<UploadProgressItem> = emptyList(),
    val syncProgress: SyncProgressItem? = null,
    val snackbarMessage: SnackbarMessage? = null,
    val onRefresh: () -> Unit = {},
    val onUploadClick: (FragmentActivity, UploadProgressItem) -> Unit = { _, _ -> },
    val onUploadDismiss: (UploadProgressItem) -> Unit = {},
    val onSyncClick: (FragmentActivity) -> Unit = {},
    val onSyncDismiss: () -> Unit = {},
    val onClearSnackbar: () -> Unit = {}
)

data class UploadProgressItem(
    val workerId: UUID,
    val title: String,
    val subtitle: String,
    val progress: Int,
    val state: UploadState,
    @DrawableRes val icon: Int,
    @ColorRes val iconBackground: Int,
    val courseId: Long?,
    val assignmentId: Long?,
    val attemptId: Long?,
    val folderId: Long?
)

enum class UploadState {
    UPLOADING,
    SUCCEEDED,
    FAILED
}

data class SyncProgressItem(
    val title: String,
    val subtitle: String,
    val progress: Int,
    val state: ProgressState,
    val itemCount: Int
)