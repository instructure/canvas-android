package com.instructure.pandautils.features.inbox.details

import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.pandautils.features.inbox.utils.InboxMessageUiState

data class InboxDetailsUiState(
    val conversationId: Long? = null,
    val conversation: Conversation? = null,
    val messageStates: List<InboxMessageUiState> = emptyList(),
    val state: ScreenState = ScreenState.Loading,
    val alertDialogState: AlertDialogState = AlertDialogState()
)

data class AlertDialogState(
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
}

sealed class InboxDetailsAction {
    data object CloseFragment : InboxDetailsAction()
    data object RefreshCalled : InboxDetailsAction()
    data object Reply : InboxDetailsAction()
    data object ReplyAll : InboxDetailsAction()
    data object Forward : InboxDetailsAction()
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