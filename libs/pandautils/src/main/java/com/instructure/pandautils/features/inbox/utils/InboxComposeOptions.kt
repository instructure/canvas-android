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

import android.content.Context
import android.os.Parcelable
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class InboxComposeOptions(
    val mode: InboxComposeOptionsMode = InboxComposeOptionsMode.NEW_MESSAGE,
    val previousMessages: InboxComposeOptionsPreviousMessages? = null,
    val disabledFields: InboxComposeOptionsDisabledFields = InboxComposeOptionsDisabledFields(),
    val hiddenFields: InboxComposeOptionsHiddenFields = InboxComposeOptionsHiddenFields(),
    val defaultValues: InboxComposeOptionsDefaultValues = InboxComposeOptionsDefaultValues(),
    val hiddenBodyMessage: String? = null,
    val autoSelectRecipientsFromRoles: List<EnrollmentType>? = null,
): Parcelable {
    companion object {
        const val COMPOSE_PARAMETERS = "InboxComposeOptions"
        fun buildNewMessage(): InboxComposeOptions {
            return InboxComposeOptions(
                mode = InboxComposeOptionsMode.NEW_MESSAGE
            )
        }

        fun buildReply(context: Context, conversation: Conversation, selectedMessage: Message): InboxComposeOptions {
            val currentUser = ApiPrefs.user?.id
            val recipients = if (selectedMessage.authorId == currentUser) {
                conversation.participants.filter { it.id != currentUser }
            } else {
                conversation.participants.filter { it.id == selectedMessage.authorId }
            }

            return InboxComposeOptions(
                mode = InboxComposeOptionsMode.REPLY,
                previousMessages = InboxComposeOptionsPreviousMessages(
                    conversation,
                    conversation.messages
                        .filter {
                            if (it.createdAt != null && selectedMessage.createdAt != null)
                                ZonedDateTime.parse(it.createdAt) <= ZonedDateTime.parse(selectedMessage.createdAt)
                            else
                                true
                        }
                ),
                disabledFields = InboxComposeOptionsDisabledFields(isContextDisabled = true, isSubjectDisabled = true),
                hiddenFields = InboxComposeOptionsHiddenFields(isSendIndividualHidden = true),
                defaultValues = InboxComposeOptionsDefaultValues(
                    contextCode = conversation.contextCode,
                    contextName = conversation.contextName,
                    recipients = recipients.map { Recipient(it.id.toString(), it.name, it.avatarUrl) },
                    subject = context.getString(
                        R.string.inboxReplySubjectRePrefix,
                        conversation.subject
                    ),
                )
            )
        }

        fun buildReplyAll(context: Context, conversation: Conversation, selectedMessage: Message): InboxComposeOptions {
            val currentUser = ApiPrefs.user?.id
            val recipients = conversation.participants.filter { it.id != currentUser }

            return InboxComposeOptions(
                mode = InboxComposeOptionsMode.REPLY_ALL,
                previousMessages = InboxComposeOptionsPreviousMessages(
                    conversation,
                    conversation.messages
                        .filter {
                            if (it.createdAt != null && selectedMessage.createdAt != null)
                                ZonedDateTime.parse(it.createdAt) <= ZonedDateTime.parse(selectedMessage.createdAt)
                            else
                                true
                        }
                ),
                disabledFields = InboxComposeOptionsDisabledFields(isContextDisabled = true, isSubjectDisabled = true),
                hiddenFields = InboxComposeOptionsHiddenFields(isSendIndividualHidden = true),
                defaultValues = InboxComposeOptionsDefaultValues(
                    contextCode = conversation.contextCode,
                    contextName = conversation.contextName,
                    recipients = recipients.map { Recipient(it.id.toString(), it.name, it.avatarUrl) },
                    subject = context.getString(
                        R.string.inboxReplySubjectRePrefix,
                        conversation.subject
                    ),
                )
            )
        }

        fun buildForward(context: Context, conversation: Conversation, selectedMessage: Message): InboxComposeOptions {
            return InboxComposeOptions(
                mode = InboxComposeOptionsMode.FORWARD,
                previousMessages = InboxComposeOptionsPreviousMessages(
                    conversation,
                    conversation.messages
                        .filter {
                            if (it.createdAt != null && selectedMessage.createdAt != null)
                                ZonedDateTime.parse(it.createdAt) <= ZonedDateTime.parse(selectedMessage.createdAt)
                            else
                                true
                        }
                ),
                disabledFields = InboxComposeOptionsDisabledFields(isContextDisabled = true, isSubjectDisabled = true),
                hiddenFields = InboxComposeOptionsHiddenFields(isSendIndividualHidden = true),
                defaultValues = InboxComposeOptionsDefaultValues(
                    contextCode = conversation.contextCode,
                    contextName = conversation.contextName,
                    subject = context.getString(
                        R.string.inboxForwardSubjectFwPrefix,
                        conversation.subject
                    ),
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