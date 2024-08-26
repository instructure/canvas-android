package com.instructure.pandautils.features.inbox.details

import com.instructure.canvasapi2.models.Conversation

data class InboxDetailsUiState(
    val conversationId: Long? = null,
    val conversations: List<Conversation> = emptyList(),
)

sealed class InboxDetailsFragmentAction {
    data object CloseFragment : InboxDetailsFragmentAction()
}

sealed class InboxDetailsAction {
    data object CloseFragment : InboxDetailsAction()
}