package com.instructure.pandautils.features.inbox.utils

import com.instructure.canvasapi2.models.Attachment
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
    data class DeleteMessage(val message: Message) : MessageAction()
    data class OpenAttachment(val attachment: Attachment) : MessageAction()
}
