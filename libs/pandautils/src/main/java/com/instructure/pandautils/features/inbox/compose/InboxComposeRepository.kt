package com.instructure.pandautils.features.inbox.compose

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.DataResult

interface InboxComposeRepository {
    suspend fun getCourses(forceRefresh: Boolean = false): DataResult<List<Course>>
    suspend fun getGroups(forceRefresh: Boolean = false): DataResult<List<Group>>
    suspend fun getRecipients(searchQuery: String, context: CanvasContext, forceRefresh: Boolean = false): DataResult<List<Recipient>>
    suspend fun createConversation(recipients: List<Recipient>, subject: String, message: String, context: CanvasContext, attachments: List<Attachment>, isIndividual: Boolean): DataResult<List<Conversation>>
    suspend fun addMessage(conversationId: Long, recipients: List<Recipient>, message: String, includedMessages: List<Message>, attachments: List<Attachment>, context: CanvasContext, ): DataResult<Conversation>
    suspend fun canSendToAll(context: CanvasContext): DataResult<Boolean>
}