/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.inbox.attachment

import com.instructure.horizon.R
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState

data class HorizonInboxAttachment(
    val id: Long,
    val fileName: String,
    val fileSize: Long,
    val filePath: String,
    val state: HorizonInboxAttachmentState,
    val onActionClicked: (() -> Unit)? = null,
) {
    fun toFileDropItemState(): FileDropItemState {
        return when (this.state) {
            is HorizonInboxAttachmentState.InProgress -> {
                FileDropItemState.InProgress(
                    fileName = this.fileName,
                    progress = this.state.progress,
                    onActionClick = this.onActionClicked
                )
            }

            is HorizonInboxAttachmentState.Success -> {
                FileDropItemState.Success(
                    fileName = this.fileName,
                    onActionClick = this.onActionClicked
                )
            }

            else -> {
                FileDropItemState.Error(
                    fileName = this.fileName,
                    onActionClick = this.onActionClicked,
                    actionIconRes = R.drawable.delete
                )
            }
        }
    }
}

sealed class HorizonInboxAttachmentState {
    data class InProgress(val progress: Float): HorizonInboxAttachmentState()
    data object Success: HorizonInboxAttachmentState()
    data object Error: HorizonInboxAttachmentState()
}