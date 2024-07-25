package com.instructure.pandautils.features.inbox.compose

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

interface InboxComposeRepository {
    suspend fun getCourses(): List<Course>
    suspend fun getGroups(): List<Group>
    suspend fun getRecipients(searchQuery: String, context: CanvasContext): List<Recipient>
    suspend fun createConversation(recipients: List<Recipient>, subject: String, message: String, context: CanvasContext, attachments: List<Attachment>, isIndividual: Boolean, ): List<Conversation>
}

class InboxComposeRepositoryImpl @Inject constructor(
    private val courseAPI: CourseAPI.CoursesInterface,
    private val groupAPI: GroupAPI.GroupInterface,
    private val recipientAPI: RecipientAPI.RecipientInterface,
    private val inboxAPI: InboxApi.InboxInterface,
): InboxComposeRepository {
    override suspend fun getCourses(): List<Course> {
        val params = RestParams(usePerPageQueryParam = true)

        return courseAPI.getFirstPageCourses(params).depaginate {
            courseAPI.next(it, params)
        }.dataOrNull ?: emptyList()
    }

    override suspend fun getGroups(): List<Group> {
        val params = RestParams(usePerPageQueryParam = true)

        return groupAPI.getFirstPageGroups(params).depaginate {
            groupAPI.getNextPageGroups(it, params)
        }.dataOrNull ?: emptyList()
    }

    override suspend fun getRecipients(searchQuery: String, context: CanvasContext): List<Recipient> {
        val params = RestParams(usePerPageQueryParam = true)
        return recipientAPI.getFirstPageRecipientList(
            searchQuery = searchQuery,
            context = context.apiContext(),
            restParams = params,
        ).depaginate {
            recipientAPI.getNextPageRecipientList(it, params)
        }.dataOrNull ?: emptyList()
    }

    override suspend fun createConversation(
        recipients: List<Recipient>,
        subject: String,
        message: String,
        context: CanvasContext,
        attachments: List<Attachment>,
        isIndividual: Boolean,
    ): List<Conversation> {
        val restParams = RestParams()

        return inboxAPI.createConversation(
            recipients = recipients.mapNotNull { it.stringId },
            subject = subject,
            message = message,
            contextCode = context.contextId,
            attachmentIds = attachments.map { it.id }.toLongArray(),
            isBulk = if (isIndividual) { 0 } else { 1 },
            params = restParams
        ).dataOrNull ?: emptyList()
    }
}