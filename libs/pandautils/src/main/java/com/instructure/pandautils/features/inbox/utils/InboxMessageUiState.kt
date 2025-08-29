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
package com.instructure.pandautils.features.inbox.utils

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Message

data class InboxMessageUiState(
    val message: Message? = null,
    val author: BasicUser? = null,
    val recipients: List<BasicUser> = emptyList(),
    val enabledActions: Boolean = true,
    val cannotReply: Boolean = false,
    val canReplyAll: Boolean = true,
    val canDelete: Boolean = true
)

sealed class MessageAction {
    data class Reply(val message: Message) : MessageAction()
    data class ReplyAll(val message: Message) : MessageAction()
    data class Forward(val message: Message) : MessageAction()
    data class DeleteMessage(val message: Message) : MessageAction()
    data class OpenAttachment(val attachment: Attachment) : MessageAction()
    data class UrlSelected(val url: String) : MessageAction()
}
