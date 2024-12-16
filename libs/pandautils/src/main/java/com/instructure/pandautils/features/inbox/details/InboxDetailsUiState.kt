/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.features.inbox.details

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxMessageUiState

data class InboxDetailsUiState(
    val conversationId: Long? = null,
    val conversation: Conversation? = null,
    val messageStates: List<InboxMessageUiState> = emptyList(),
    val state: ScreenState = ScreenState.Loading,
    val confirmationDialogState: ConfirmationDialogState = ConfirmationDialogState(),
    val showBackButton: Boolean = true,
)

data class ConfirmationDialogState(
    val showDialog: Boolean = false,
    val title: String = "",
    val message: String = "",
    val positiveButton: String = "",
    val negativeButton: String = "",
    val onPositiveButtonClick: () -> Unit = {},
    val onNegativeButtonClick: () -> Unit = {}

)

sealed class InboxDetailsFragmentAction {
    data object CloseFragment : InboxDetailsFragmentAction()
    data class ShowScreenResult(val message: String) : InboxDetailsFragmentAction()
    data class UrlSelected(val url: String) : InboxDetailsFragmentAction()
    data class OpenAttachment(val attachment: Attachment) : InboxDetailsFragmentAction()
    data object UpdateParentFragment : InboxDetailsFragmentAction()
    data class NavigateToCompose(val options: InboxComposeOptions) : InboxDetailsFragmentAction()
}

sealed class InboxDetailsAction {
    data object CloseFragment : InboxDetailsAction()
    data object RefreshCalled : InboxDetailsAction()
    data class Reply(val message: Message) : InboxDetailsAction()
    data class ReplyAll(val message: Message) : InboxDetailsAction()
    data class Forward(val message: Message) : InboxDetailsAction()
    data class DeleteConversation(val conversationId: Long) : InboxDetailsAction()
    data class DeleteMessage(val conversationId: Long, val message: Message) : InboxDetailsAction()
    data class UpdateState(val conversationId: Long, val workflowState: Conversation.WorkflowState) : InboxDetailsAction()
    data class UpdateStarred(val conversationId: Long, val newStarValue: Boolean) : InboxDetailsAction()
}

sealed class ScreenState {
    data object Loading : ScreenState()
    data object Error : ScreenState()
    data object Empty : ScreenState()
    data object Success : ScreenState()
}