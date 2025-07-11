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
package com.instructure.horizon.features.inbox.details

import androidx.annotation.DrawableRes
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachment
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import java.util.Date

data class HorizonInboxDetailsUiState(
    val loadingState: LoadingState = LoadingState(),
    val title: String = "",
    @DrawableRes val titleIcon: Int? = null,
    val items: List<HorizonInboxDetailsItem> = emptyList(),
    val replyState: HorizonInboxReplyState? = null,
    val bottomLayout: Boolean = false,
)

data class HorizonInboxReplyState(
    val isLoading: Boolean = false,
    val replyTextValue: TextFieldValue = TextFieldValue(""),
    val onReplyTextValueChange: (TextFieldValue) -> Unit = {},
    val onSendReply: () -> Unit = {},
    val showAttachmentPicker: Boolean = false,
    val onShowAttachmentPickerChanged: (Boolean) -> Unit = {},
    val attachments: List<HorizonInboxAttachment> = emptyList(),
    val onAttachmentsChanged: (List<HorizonInboxAttachment>) -> Unit = {}
)

data class HorizonInboxDetailsItem(
    val author: String,
    val date: Date,
    val isHtmlContent: Boolean,
    val content: String,
    val attachments: List<HorizonInboxDetailsAttachment>,
)

data class HorizonInboxDetailsAttachment(
    val id: Long,
    val name: String,
    val url: String,
    val contentType: String?,
    var thumbnailUrl: String? = null,
    val onDownloadClick: (HorizonInboxDetailsAttachment) -> Unit = {},
    val onCancelDownloadClick: (Long) -> Unit = {},
    val downloadState: FileDownloadProgressState = FileDownloadProgressState.COMPLETED,
    val downloadProgress: Float = 0f,
)