package com.instructure.parentapp.features.inbox.compose

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.features.inbox.compose.InboxComposeRepository

class ParentInboxComposeRepository(
    private val courseAPI: CourseAPI.CoursesInterface,
    private val recipientAPI: RecipientAPI.RecipientInterface,
    private val inboxAPI: InboxApi.InboxInterface,
): InboxComposeRepository {
    override suspend fun getCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)

        val coursesResult = courseAPI.getCoursesByEnrollmentType(Enrollment.EnrollmentType.Observer.apiTypeString, params)
            .depaginate { nextUrl -> courseAPI.next(nextUrl, params) }

        val courses = coursesResult.dataOrNull ?: return coursesResult

        val validCourses = courses.filter { it.isValidTerm() && it.hasActiveEnrollment() }

        return DataResult.Success(validCourses)
    }

    override suspend fun getGroups(forceRefresh: Boolean): DataResult<List<Group>> {
        return DataResult.Success(emptyList())
    }

    override suspend fun getRecipients(searchQuery: String, context: CanvasContext, forceRefresh: Boolean): DataResult<List<Recipient>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return recipientAPI.getFirstPageRecipientListNoSyntheticContexts(
            searchQuery = searchQuery,
            context = context.contextId,
            restParams = params,
        ).depaginate {
            recipientAPI.getNextPageRecipientList(it, params)
        }
    }

    override suspend fun createConversation(
        recipients: List<Recipient>,
        subject: String,
        message: String,
        context: CanvasContext,
        attachments: List<Attachment>,
        isIndividual: Boolean,
    ): DataResult<List<Conversation>> {
        val restParams = RestParams()

        return inboxAPI.createConversation(
            recipients = recipients.mapNotNull { it.stringId },
            subject = subject,
            message = message,
            contextCode = context.contextId,
            attachmentIds = attachments.map { it.id }.toLongArray(),
            isBulk = if (isIndividual) { 0 } else { 1 },
            params = restParams
        )
    }

    override suspend fun addMessage(
        conversationId: Long,
        recipients: List<Recipient>,
        message: String,
        includedMessages: List<Message>,
        attachments: List<Attachment>,
        context: CanvasContext,
    ): DataResult<Conversation> {
        val restParams = RestParams()

        return inboxAPI.addMessage(
            conversationId = conversationId,
            recipientIds = recipients.mapNotNull { it.stringId },
            body = message,
            includedMessageIds = includedMessages.map { it.id }.toLongArray(),
            attachmentIds = attachments.map { it.id }.toLongArray(),
            contextCode = context.contextId,
            params = restParams
        )
    }

    override suspend fun canSendToAll(context: CanvasContext): DataResult<Boolean> {
        val restParams = RestParams()
        val permissionResponse =  courseAPI.getCoursePermissions(context.id, listOf(CanvasContextPermission.SEND_MESSAGES_ALL), restParams)

        return permissionResponse.map {
            it.send_messages_all
        }
    }
}