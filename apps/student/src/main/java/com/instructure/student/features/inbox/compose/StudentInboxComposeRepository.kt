package com.instructure.student.features.inbox.compose

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.inbox.compose.InboxComposeRepository

class StudentInboxComposeRepository: InboxComposeRepository {
    override suspend fun getCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        TODO("Not yet implemented")
    }

    override suspend fun getGroups(forceRefresh: Boolean): DataResult<List<Group>> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecipients(
        searchQuery: String,
        context: CanvasContext,
        forceRefresh: Boolean
    ): DataResult<List<Recipient>> {
        TODO("Not yet implemented")
    }

    override suspend fun createConversation(
        recipients: List<Recipient>,
        subject: String,
        message: String,
        context: CanvasContext,
        attachments: List<Attachment>,
        isIndividual: Boolean
    ): DataResult<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun addMessage(
        conversationId: Long,
        recipients: List<Recipient>,
        message: String,
        includedMessages: List<Message>,
        attachments: List<Attachment>,
        context: CanvasContext
    ): DataResult<Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun canSendToAll(context: CanvasContext): DataResult<Boolean> {
        TODO("Not yet implemented")
    }
}