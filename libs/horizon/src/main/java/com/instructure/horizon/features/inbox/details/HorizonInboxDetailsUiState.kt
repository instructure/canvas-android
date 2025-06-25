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
import com.instructure.canvasapi2.models.Attachment
import com.instructure.horizon.horizonui.platform.LoadingState
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
    val replyTextValue: TextFieldValue = TextFieldValue(""),
    val onReplyTextValueChange: (TextFieldValue) -> Unit = {},
    val onSendReply: () -> Unit = {},
)

data class HorizonInboxDetailsItem(
    val author: String,
    val date: Date,
    val content: String,
    val attachments: List<Attachment>,
)