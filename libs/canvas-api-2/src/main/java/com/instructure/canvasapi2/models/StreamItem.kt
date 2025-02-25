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
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.convertScoreToLetterGrade
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class StreamItem(
        override val id: Long = -1,
        @SerializedName("updated_at")
        val updatedAt: String = "",
        @SerializedName("submitted_at")
        val submittedAt: String? = null,
        var title: String? = null,
        private var message: String? = null,
        val type: String = "",
        private val context_type: String? = null,
        // Helper method to show that the stream item has been read without having to reload all the data.
        // this method does not get the data from the server, so make sure item is actually read.
        @SerializedName("read_state")
        var isReadState: Boolean = false,
        val url: String? = null,
        @SerializedName("html_url")
        val htmlUrl: String = "",
        private var course_id: Long = -1,
        private var group_id: Long = -1,
        var assignment_id: Long = -1,

        // message type, which is not a conversation, but stream notifications
        @SerializedName("message_id")
        val messageId: Long = -1,
        @SerializedName("notification_category")
        val notificationCategory: String = "",

        // Conversation
        @SerializedName("conversation_id")
        val conversationId: Long = -1,
        @SerializedName("private")
        val isPrivate: Boolean = false,
        @SerializedName("participant_count")
        val participantCount: Int = 0,

        // discussionTopic or announcement
        private val discussion_topic_id: Long = -1,
        private val announcement_id: Long = -1,
        private val require_initial_post: Boolean = false,
        private val user_has_posted: Boolean = false,
        @SerializedName("root_discussion_entries")
        val rootDiscussionEntries: List<DiscussionEntry> = ArrayList(),

        // submission
        val attempt: Int = 0,
        val body: String? = null,
        val grade: String? = null,
        private val grade_matches_current_submission: Boolean = false,
        private val graded_at: String? = null,
        @SerializedName("grader_id")
        val graderId: Long = -1,
        val score: Double = -1.0,
        @SerializedName("submission_type")
        val submissiTonType: String? = null,
        @SerializedName("workflow_state")
        val workflowState: String = "",
        val late: Boolean = false,
        @SerializedName("preview_url")
        val previewUrl: String = "",
        @SerializedName("submission_comments")
        val submissionComments: List<SubmissionComment> = ArrayList(),
        var canvasContext: CanvasContext? = null,
        val assignment: Assignment? = null,
        @SerializedName("user_id")
        val userId: Long = -1,
        val user: User = User(),
        val excused: Boolean = false,
        @SerializedName("latest_messages")
        val latestMessages: List<Message> = ArrayList(),
) : CanvasModel<StreamItem>() {
    // We want opposite of natural sorting order of date since we want the newest one to come first
    override val comparisonDate get() = updatedDate

    val gradedDate get() = graded_at.toDate()
    val submittedDate get() = submittedAt.toDate()
    val updatedDate: Date?
        get() {
            if (getStreamItemType() == Type.CONVERSATION && latestMessages.isNotEmpty()) {
                return latestMessages
                    .filter { it.createdAt.toDate() != null }
                    .maxBy { it.createdAt.toDate()!! }
                    .createdAt.toDate()
            }
            return updatedAt.toDate()
        }

    // Helper fields
    @IgnoredOnParcel
    private var canvasContextType: CanvasContext.Type? = CanvasContext.Type.USER
    @IgnoredOnParcel
    private var hasSetContextType = false
    @IgnoredOnParcel
    var conversation: Conversation? = null
        private set
    @IgnoredOnParcel
    var isChecked: Boolean = false

    val contextType: CanvasContext.Type?
        get() {
            if (!hasSetContextType) {
                if (context_type != null && (context_type.lowercase(Locale.getDefault()) == "course" || course_id > 0)) {
                    canvasContextType = CanvasContext.Type.COURSE
                } else if (context_type != null && (context_type.lowercase(Locale.getDefault()) == "group" || group_id > 0)) {
                    canvasContextType = CanvasContext.Type.GROUP
                }
                hasSetContextType = true
            }

            return canvasContextType
        }
    val courseId: Long
        get() {
            if (contextType === CanvasContext.Type.COURSE && course_id == -1L) {
                course_id = parseCourseId()
            }
            return course_id
        }

    val groupId: Long
        get() {
            if (contextType === CanvasContext.Type.GROUP && group_id == -1L) {
                group_id = parseGroupId()
            }
            return group_id
        }
    val assignmentId: Long
        get() {
            if (contextType === CanvasContext.Type.COURSE) {
                return parseAssignmentId()
            }
            return -1L
        }
    val discussionTopicId: Long
        get() = if (discussion_topic_id == -1L) {
            announcement_id
        } else discussion_topic_id

    enum class Type {
        DISCUSSION_TOPIC, SUBMISSION, ANNOUNCEMENT, CONVERSATION, MESSAGE, CONFERENCE, COLLABORATION, COLLECTION_ITEM, UNKNOWN, NOT_SET, DISCUSSION_MENTION, DISCUSSION_ENTRY;


        companion object {
            fun isDiscussionTopic(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == DISCUSSION_TOPIC
            fun isSubmission(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == SUBMISSION
            fun isAnnouncement(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == ANNOUNCEMENT
            fun isConversation(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == CONVERSATION
            fun isMessage(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == MESSAGE
            fun isConference(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == CONFERENCE
            fun isCollaboration(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == COLLABORATION
            fun isCollectionItem(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == COLLECTION_ITEM
            fun isUnknown(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == UNKNOWN
            fun isNotSet(streamItem: StreamItem): Boolean = streamItem.getStreamItemType() == NOT_SET
        }
    }

    fun getTitle(context: Context): String? {
        if (title == null && getStreamItemType() == Type.CONVERSATION) {
            title = context.getString(R.string.Message)
        }
        return title
    }

    fun getMessage(context: Context, restrictQuantitativeData: Boolean = false, gradingScheme: List<GradingSchemeRow> = emptyList()): String? {
        if (message == null) {
            message = createMessage(context, restrictQuantitativeData, gradingScheme)
        }
        return message
    }

    fun getStreamItemType(): Type = typeFromString(type)

    private fun typeFromString(type: String): Type = when {
        type.lowercase(Locale.getDefault()) == "conversation" -> Type.CONVERSATION
        type.lowercase(Locale.getDefault()) == "submission" -> Type.SUBMISSION
        type.lowercase(Locale.getDefault()) == "discussiontopic" -> Type.DISCUSSION_TOPIC
        type.lowercase(Locale.getDefault()) == "announcement" -> Type.ANNOUNCEMENT
        type.lowercase(Locale.getDefault()) == "message" && notificationCategory.lowercase() == "discussionmention" -> Type.DISCUSSION_MENTION
        type.lowercase(Locale.getDefault()) == "message" -> Type.MESSAGE
        type.lowercase(Locale.getDefault()) == "conference" -> Type.CONFERENCE
        type.lowercase(Locale.getDefault()) == "webconference" -> Type.CONFERENCE
        type.lowercase(Locale.getDefault()) == "collaboration" -> Type.COLLABORATION
        type.lowercase(Locale.getDefault()) == "collectionitem" -> Type.COLLECTION_ITEM
        type.lowercase(Locale.getDefault()) == "discussionentry" -> Type.DISCUSSION_ENTRY
        else -> Type.UNKNOWN
    }

    fun setConversation(context: Context, conversation: Conversation?, myUserId: Long, monologueDefault: String) {
        this.conversation = conversation ?: return
        title = conversation.getMessageTitle(context, myUserId, monologueDefault).toString()
        message = createMessage(context)
    }

    fun setCanvasContextFromMap(courseMap: Map<Long, Course>, groupMap: Map<Long, Group>) {
        canvasContext = if (contextType === CanvasContext.Type.COURSE) {
            courseMap[courseId]
        } else {
            groupMap[groupId]
        }
    }

    private fun createMessage(context: Context, restrictQuantitativeData: Boolean = false, gradingScheme: List<GradingSchemeRow> = emptyList()): String? {
        when (getStreamItemType()) {
            StreamItem.Type.CONVERSATION -> {
                if (conversation == null) {
                    return context.getString(R.string.Loading)
                } else if (conversation!!.lastMessagePreview == null) {
                    return ""
                }
                return conversation!!.lastMessagePreview
            }
            StreamItem.Type.SUBMISSION -> {
                // Get comments from assignment
                var comment: String = ""
                if (submissionComments.isNotEmpty()) {
                    val lastComment = submissionComments.last().comment
                    if (lastComment != null && lastComment != "null") comment = lastComment
                }

                val displayedGrade = when {
                    excused -> context.getString(R.string.gradeExcused)
                    restrictQuantitativeData -> getGradeWhenQuantitativeDataRestricted(context, gradingScheme, score, assignment?.pointsPossible)
                    score != -1.0 -> score.toString().orEmpty()
                    else -> ""
                }

                return "$displayedGrade $comment"
            }
            StreamItem.Type.DISCUSSION_TOPIC ->
                // If it's a discussionTopic, get the last entry for the message.
                if (rootDiscussionEntries.isNotEmpty()) {
                    return rootDiscussionEntries[rootDiscussionEntries.size - 1].getMessage(context.getString(R.string.Deleted))
                }
            else -> {
            }
        }

        return if (message == null) {
            ""
        } else message
    }

    private fun getGradeWhenQuantitativeDataRestricted(context: Context, gradingScheme: List<GradingSchemeRow>, score: Double, maxScore: Double?): String {
        return if (assignment?.isGradingTypeQuantitative == true) {
            if (gradingScheme.isEmpty() || maxScore == null) {
                context.getString(R.string.gradeUpdated)
            } else {
                convertScoreToLetterGrade(score, maxScore, gradingScheme)
            }
        } else {
            grade.orEmpty()
        }
    }

    private fun parseAssignmentId(): Long {
        // Get the assignment from the url
        if (htmlUrl.isNotEmpty() && htmlUrl != "null") {
            val searchFor = "assignments/"
            var start = htmlUrl.indexOf(searchFor)
            if (start == -1) {
                return 0
            }
            start += searchFor.length
            var end = htmlUrl.indexOf("/", start)
            //in some urls the assignmentID might be the last thing so there wouldn't be a final /
            if (end == -1) {
                end = htmlUrl.length
            }
            val assignmentId = htmlUrl.substring(start, end)

            return APIHelper.expandTildeId(assignmentId).toLong()
        }
        return 0
    }

    private fun parseCourseId(): Long {
        if (htmlUrl.isNotEmpty() && htmlUrl != "null") {
            val searchFor = "courses/"
            var start = htmlUrl.indexOf(searchFor)
            if (start == -1) {
                return 0
            }
            start += searchFor.length
            val end = htmlUrl.indexOf("/", start)

            val courseIdString = htmlUrl.substring(start, end)

            return APIHelper.expandTildeId(courseIdString).toLong()
        }
        return 0
    }

    private fun parseGroupId(): Long {
        if (htmlUrl.isNotEmpty() && htmlUrl != "null") {
            val searchFor = "groups/"
            var start = htmlUrl.indexOf(searchFor)
            if (start == -1) {
                return 0
            }
            start += searchFor.length
            val end = htmlUrl.indexOf("/", start)

            val groupIdString = htmlUrl.substring(start, end)

            return APIHelper.expandTildeId(groupIdString).toLong()
        }
        return 0
    }
}
