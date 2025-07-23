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
package com.instructure.horizon.features.inbox.list

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.horizonui.platform.LoadingState
import java.util.Date

data class HorizonInboxListUiState(
    val loadingState: LoadingState = LoadingState(),
    val items: List<HorizonInboxListItemState> = emptyList(),
    val recipientSearchQuery: TextFieldValue = TextFieldValue(""),
    val allRecipients: List<Recipient> = emptyList(),
    val selectedRecipients: List<Recipient> = emptyList(),
    val selectedScope: HorizonInboxScope = HorizonInboxScope.All,
    val updateRecipientSearchQuery: (TextFieldValue) -> Unit,
    val updateScopeFilter: (HorizonInboxScope) -> Unit,
    val onRecipientSelected: (Recipient) -> Unit = {},
    val onRecipientRemoved: (Recipient) -> Unit = {},
    val isOptionListLoading: Boolean = false,
    val showSnackbar: (String) -> Unit = {},
    val minQueryLength: Int = 3
)

data class HorizonInboxListItemState(
    val id: Long,
    val type: HorizonInboxItemType,
    val title: String,
    val description: String,
    val date: Date?,
    val isUnread: Boolean,
    val courseId: Long? = null,
)

enum class HorizonInboxScope(@StringRes val label: Int) {
    All(R.string.inboxAllMessagesScopeLabel),
    Announcements(R.string.inboxAnnouncementsScopeLabel),
    Unread(R.string.inboxUnreadScopeLabel),
    Sent(R.string.inboxSentScopeLabel)
}