package com.instructure.pandautils.features.inbox.utils

import android.os.Parcelable
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ApiPrefs
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class InboxComposeOptions(
    val mode: InboxComposeOptionsMode = InboxComposeOptionsMode.NEW_MESSAGE,
    val previousMessages: InboxComposeOptionsPreviousMessages? = null,
    val disabledFields: InboxComposeOptionsDisabledFields = InboxComposeOptionsDisabledFields(),
    val hiddenFields: InboxComposeOptionsHiddenFields = InboxComposeOptionsHiddenFields(),
    val defaultValues: InboxComposeOptionsDefaultValues = InboxComposeOptionsDefaultValues(),
): Parcelable {
    companion object {
        const val COMPOSE_PARAMETERS = "InboxComposeOptions"
        fun buildNewMessage(): InboxComposeOptions {
            return InboxComposeOptions(
                mode = InboxComposeOptionsMode.NEW_MESSAGE
            )
        }

        fun buildReply(conversation: Conversation, selectedMessage: Message): InboxComposeOptions {
            val currentUser = ApiPrefs.user?.id
            val recipients = if (selectedMessage.authorId == currentUser) {
                conversation.participants.filter { it.id != currentUser }
            } else {
                conversation.participants.filter { it.id == selectedMessage.authorId }
            }

            return InboxComposeOptions(
                mode = InboxComposeOptionsMode.REPLY,
                previousMessages = InboxComposeOptionsPreviousMessages(conversation, conversation.messages.filter { ZonedDateTime.parse(it.createdAt ?: "") <= ZonedDateTime.parse(selectedMessage.createdAt ?: "") }),
                disabledFields = InboxComposeOptionsDisabledFields(isContextDisabled = true, isSubjectDisabled = true),
                defaultValues = InboxComposeOptionsDefaultValues(
                    contextCode = conversation.contextCode,
                    contextName = conversation.contextName,
                    recipients = recipients.map { Recipient(it.id.toString(), it.name, it.avatarUrl) },
                    subject = "Re: ${conversation.subject}",
                )
            )
        }

        fun buildReplyAll(conversation: Conversation, selectedMessage: Message): InboxComposeOptions {
            val currentUser = ApiPrefs.user?.id
            val recipients = conversation.participants.filter { it.id != currentUser }

            return InboxComposeOptions(
                mode = InboxComposeOptionsMode.REPLY_ALL,
                previousMessages = InboxComposeOptionsPreviousMessages(conversation, conversation.messages.filter { ZonedDateTime.parse(it.createdAt ?: "") <= ZonedDateTime.parse(selectedMessage.createdAt ?: "") }),
                disabledFields = InboxComposeOptionsDisabledFields(isContextDisabled = true, isSubjectDisabled = true),
                defaultValues = InboxComposeOptionsDefaultValues(
                    contextCode = conversation.contextCode,
                    contextName = conversation.contextName,
                    recipients = recipients.map { Recipient(it.id.toString(), it.name, it.avatarUrl) },
                    subject = "Re: ${conversation.subject}",
                )
            )
        }

        fun buildForward(conversation: Conversation, selectedMessage: Message): InboxComposeOptions {
            return InboxComposeOptions(
                mode = InboxComposeOptionsMode.FORWARD,
                previousMessages = InboxComposeOptionsPreviousMessages(conversation, conversation.messages.filter { ZonedDateTime.parse(it.createdAt ?: "") <= ZonedDateTime.parse(selectedMessage.createdAt ?: "") }),
                disabledFields = InboxComposeOptionsDisabledFields(isContextDisabled = true, isSubjectDisabled = true),
                defaultValues = InboxComposeOptionsDefaultValues(
                    contextCode = conversation.contextCode,
                    contextName = conversation.contextName,
                    subject = "Fwd: ${conversation.subject}",
                )
            )
        }
    }
}

@Parcelize
data class InboxComposeOptionsDisabledFields(
    val isContextDisabled: Boolean = false,
    val isRecipientsDisabled: Boolean = false,
    val isSendIndividualDisabled: Boolean = false,
    val isSubjectDisabled: Boolean = false,
    val isBodyDisabled: Boolean = false,
    val isAttachmentDisabled: Boolean = false,
): Parcelable

@Parcelize
data class InboxComposeOptionsHiddenFields(
    val isContextHidden: Boolean = false,
    val isRecipientsHidden: Boolean = false,
    val isSendIndividualHidden: Boolean = false,
    val isSubjectHidden: Boolean = false,
    val isBodyHidden: Boolean = false,
    val isAttachmentHidden: Boolean = false,
): Parcelable

@Parcelize
data class InboxComposeOptionsDefaultValues(
    val contextCode: String? = null,
    val contextName: String? = null,
    val recipients: List<Recipient> = emptyList(),
    val sendIndividual: Boolean = false,
    val subject: String = "",
    val body: String = "",
    val attachments: List<Attachment> = emptyList(),
): Parcelable

@Parcelize
data class InboxComposeOptionsPreviousMessages(
    val conversation: Conversation,
    val previousMessages: List<Message>,
): Parcelable

enum class InboxComposeOptionsMode {
    REPLY,
    REPLY_ALL,
    FORWARD,
    NEW_MESSAGE,
}