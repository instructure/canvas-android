package com.instructure.pandautils.features.inbox

import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Message

data class InboxMessageUiState(
    val message: Message? = null,
    val author: BasicUser? = null,
    val recipients: List<BasicUser> = emptyList(),
    val enabledActions: Boolean = true
)

sealed class MessageAction {
    data object Reply : MessageAction()
    data object ReplyAll : MessageAction()
    data object Forward : MessageAction()
}
