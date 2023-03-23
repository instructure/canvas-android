/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.models

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.Locale

@Parcelize
data class Conversation(
        override val id: Long = 0, // The unique id for the conversation.
        val subject: String? = null, // Message Subject
        @SerializedName("workflow_state")
        var workflowState: WorkflowState? = WorkflowState.UNKNOWN, // The workflowState of the conversation (unread, read, archived)
        @SerializedName("last_message")
        val lastMessage: String? = null, // 100 character preview of the last message.
        @SerializedName("last_message_at")
        val lastMessageAt: String? = null, // Date of the last message sent.
        @SerializedName("last_authored_message_at")
        val lastAuthoredMessageAt: String? = null,
        @SerializedName("message_count")
        val messageCount: Int = 0, // Number of messages in the conversation.
        @SerializedName("subscribed")
        val isSubscribed: Boolean = false, // Whether or not the user is subscribed to the current message.
        @SerializedName("starred")
        var isStarred: Boolean = false, // Whether or not the message is starred.
        val properties: List<String> = arrayListOf(),
        @SerializedName("avatar_url")
        val avatarUrl: String? = null, // The avatar to display. Knows if group, user, etc.
        @SerializedName("visible")
        val isVisible: Boolean = false, // Whether this conversation is visible in the current context. Not 100% what that means.
        val audience: List<Long>? = arrayListOf(), // The IDs of all people in the conversation. EXCLUDING the current user unless it's a monologue.
        val participants: MutableList<BasicUser> = arrayListOf(), // The name and IDs of all participants in the conversation.
        val messages: List<Message> = arrayListOf(), // Messages attached to the conversation.
        @SerializedName("context_name")
        val contextName: String? = null,
        @SerializedName("context_code")
        val contextCode: String? = null
) : CanvasModel<Conversation>() {
    // Helper variables
    @IgnoredOnParcel
    var lastMessageDate: Date? = null
    @IgnoredOnParcel
    var isDeleted = false    // Used to set whether or not we've determined it to be deleted with a failed retrofit call.
    @IgnoredOnParcel
    var deletedString = ""    // The string to show if something is deleted.

    val lastMessagePreview: String?
        get() = if (isDeleted) {
            deletedString
        } else lastMessage

    val lastMessageSent: Date
        get() {
            if (lastMessageDate == null) {
                lastMessageDate = lastMessageAt.toDate()
            }
            return lastMessageDate!!
        }

    val lastAuthoredMessageSent: Date?
        get() {
            var lastAuthoredDate: Date? = null
            if (lastAuthoredMessageAt != null) {
                lastAuthoredDate = lastAuthoredMessageAt.toDate()
            }
            return lastAuthoredDate
        }

    // We want opposite of natural sorting order of date since we want the newest one to come first
    // sent messages have a last_authored_message_at that other messages won't. In that case last_message_at can be null,
    // but last_authored_message isn't
    override val comparisonDate: Date?
        get() = if (lastMessageAt != null && lastAuthoredMessageAt == null) {
            lastMessageSent
        } else if (lastMessageAt == null && lastAuthoredMessageAt != null) {
            lastAuthoredMessageSent
        } else {
            if (lastMessageSent.after(lastAuthoredMessageSent)) {
                lastMessageSent
            } else {
                lastAuthoredMessageSent
            }
        }

    enum class WorkflowState(val apiString: String) {
        @SerializedName("read") READ("read"),
        @SerializedName("unread") UNREAD("unread"),
        @SerializedName("archived") ARCHIVED("archived"),
        UNKNOWN("")
    }

    fun isMonologue(myUserID: Long): Boolean {
        return determineMonologue(myUserID)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    fun hasAttachments(): Boolean {
        for (i in properties.indices) {
            if (properties[i] == "attachments") {
                return true
            }
        }
        return false
    }

    fun hasMedia(): Boolean {
        for (i in properties.indices) {
            if (properties[i] == "media_objects") {
                return true
            }
        }
        return false
    }

    private fun determineMonologue(userID: Long): Boolean {
        if (audience == null) {
            return false
        } else if (audience.isEmpty()) {
            return true
        }

        for (i in audience.indices) {
            if (audience[i] == userID) {
                return true
            }
        }
        return false
    }

    fun getMessageTitle(context: Context, myUserID: Long, monologue: String): CharSequence {
        return determineMessageTitle(context, myUserID, monologue)
    }

    private fun determineMessageTitle(context: Context, myUserID: Long, monologueDefault: String): CharSequence {
        if (isDeleted) return deletedString
        if (isMonologue(myUserID)) return monologueDefault

        val normalized = participants
            .filter { it.id != myUserID }
            .map { Pronouns.span(it.name, it.pronouns) }

        return if (normalized.size > 2) {
            TextUtils.concat(
                normalized[0],
                String.format(Locale.getDefault(), context.getString(R.string.andMore), normalized.size - 1)
            )
        } else {
            normalized.joinTo(SpannableStringBuilder())
        }
    }

    companion object {
        fun getWorkflowStateAPIString(workFlowState: Conversation.WorkflowState?): String =
                when (workFlowState) {
                    WorkflowState.UNREAD -> WorkflowState.UNREAD.apiString
                    WorkflowState.ARCHIVED -> WorkflowState.ARCHIVED.apiString
                    WorkflowState.READ -> WorkflowState.READ.apiString
                    else -> WorkflowState.UNKNOWN.apiString
                }
    }
}
