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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation

data class InboxViewData(
    val scope: String,
    val selectedItemsCount: String = "",
    val filterText: String = "",
    val selectionMode: Boolean = false,
    val editMenuItems: Set<InboxMenuItem> = emptySet()
)

data class InboxEntryViewData(
    val id: Long,
    val avatar: AvatarViewData,
    val title: String,
    val subject: String,
    val message: String,
    val date: String,
    val unread: Boolean,
    val starred: Boolean,
    val hasAttachment: Boolean
)

data class AvatarViewData(
    val avatarUrl: String,
    val firstUserName: String,
    val group: Boolean
)

sealed class InboxAction {
    data class OpenConversation(val conversation: Conversation, val scope: InboxApi.Scope) : InboxAction()
    object OpenScopeSelector : InboxAction()
    data class ItemSelectionChanged(val view: View, val selected: Boolean) : InboxAction()
    data class ShowConfirmationSnackbar(val text: String, val undoAction: (() -> Unit)? = null) : InboxAction()
    object CreateNewMessage : InboxAction()
    object FailedToLoadNextPage : InboxAction()
    object UpdateUnreadCount : InboxAction()
    data class UpdateUnreadCountOffline(val increaseBy: Int) : InboxAction()
    data class OpenContextFilterSelector(val canvasContexts: List<CanvasContext>) : InboxAction()
    object RefreshFailed : InboxAction()
    data class ConfirmDelete(val count: Int) : InboxAction()
    data class AvatarClickedCallback(val conversation: Conversation, val scope: InboxApi.Scope) : InboxAction()
    object DismissSnackbar : InboxAction()
}