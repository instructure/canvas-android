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
package com.instructure.pandautils.features.inbox.list

import android.view.View
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Conversation
import com.instructure.pandautils.features.inbox.list.itemviewmodels.InboxEntryItemViewModel

data class InboxViewData(
    val scope: String,
    val selectedItems: String = "",
    val selectionMode: Boolean = false
)

data class InboxEntryViewData(
    val id: Long,
    val avatarUrl: String,
    val username: String,
    val subject: String,
    val message: String,
    val date: String,
    val unread: Boolean,
    val starred: Boolean,
    val hasAttachment: Boolean
)

sealed class InboxAction {
    data class OpenConversation(val conversation: Conversation, val scope: InboxApi.Scope) : InboxAction()
    object OpenScopeSelector : InboxAction()
    data class ItemSelectionChanged(val view: View, val selected: Boolean) : InboxAction()
    data class ShowConfirmationSnackbar(val text: String): InboxAction()
}