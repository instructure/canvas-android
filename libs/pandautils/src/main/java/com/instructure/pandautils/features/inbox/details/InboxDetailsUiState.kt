package com.instructure.pandautils.features.inbox.details

import com.instructure.canvasapi2.models.Conversation
import com.instructure.pandautils.features.inbox.util.InboxMessageUiState

data class InboxDetailsUiState(
    val conversationId: Long? = null,
    val conversation: Conversation? = null,
    val messageStates: List<InboxMessageUiState> = emptyList(),
    val state: ScreenState = ScreenState.Loading
)

sealed class InboxDetailsFragmentAction {
    data object CloseFragment : InboxDetailsFragmentAction()
}

sealed class InboxDetailsAction {
    data object CloseFragment : InboxDetailsAction()
    data object RefreshCalled : InboxDetailsAction()
}

sealed class ScreenState {
    data object Loading : ScreenState()
    data object Error : ScreenState()
    data object Empty : ScreenState()
    data object Success : ScreenState()
}