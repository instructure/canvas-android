package com.instructure.pandautils.features.inbox

import com.instructure.canvasapi2.models.Author
import com.instructure.canvasapi2.models.Message

data class InboxMessageUiState(
    val message: Message? = null,
    val author: Author? = null,
    val enabledActions: List<MessageAction> = emptyList()
)

sealed class MessageAction {
    data object Reply : MessageAction()
    data object ReplyAll : MessageAction()
    data object Forward : MessageAction()
}
