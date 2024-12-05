/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.presenters

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.InboxManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.weave.inParallel
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.viewinterface.AddMessageView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response
import java.net.URLEncoder
import java.util.*

class AddMessagePresenter(val conversation: Conversation?, private val mParticipants: ArrayList<Recipient>?, private val mMessages: ArrayList<Message>?, val isReply: Boolean) : FragmentPresenter<AddMessageView>() {

    private val mAttachments = ArrayList<Attachment>()
    private var mCourse: Course? = null

    private var mAPICalls: Job? = null

    private var fetchedCourses: Boolean = false

    override fun loadData(forceNetwork: Boolean) {}
    override fun refresh(forceNetwork: Boolean) {}

    fun getAllCoursesAndGroups(forceNetwork: Boolean) {
        // Skip if we have already fetched, to avoid resetting the course spinner
        if (fetchedCourses && !forceNetwork) return

        mAPICalls = weave {
            viewCallback?.onRefreshStarted()
            try {
                var courses: ArrayList<Course>? = null
                var groups: ArrayList<Group>? = null
                inParallel {

                    // Get Courses
                    await<List<Course>>({ CourseManager.getAllFavoriteCourses(forceNetwork, it) }) {
                        courses = it as ArrayList<Course>
                    }

                    // Get graded submission count
                    await<List<Group>>({ GroupManager.getFavoriteGroups(it, forceNetwork) }) {
                        groups = it as ArrayList<Group>
                    }

                }
                fetchedCourses = true
                viewCallback?.addCoursesAndGroups(courses!!, groups!!)
            } catch (ignore: Throwable) {
            }
        }
    }

    private val mCreateConversationCallback = object : StatusCallback<List<Conversation>>() {
        override fun onResponse(response: Response<List<Conversation>>, linkHeaders: LinkHeaders, type: ApiType) {
            if (viewCallback != null) {
                viewCallback?.messageSuccess()
            }
        }

        override fun onFail(call: Call<List<Conversation>>?, error: Throwable, response: Response<*>?) {
            if (viewCallback != null) {
                viewCallback?.messageFailure()
            }
        }
    }

    fun sendNewMessage(selectedRecipients: List<Recipient>, message: String, subject: String, contextId: String, isBulk: Boolean) {

        val attachmentIDs = LongArray(mAttachments.size)
        for (i in attachmentIDs.indices) {
            attachmentIDs[i] = mAttachments[i].id
        }
        // Assemble list of recipient IDs
        val recipientIds = selectedRecipients.mapNotNull { it.stringId }

        InboxManager.createConversation(recipientIds, message, subject, contextId, attachmentIDs, isBulk, mCreateConversationCallback)
    }

    fun sendMessage(selectedRecipients: List<Recipient>, message: String) {

        if (mAddConversationCallback.isCallInProgress) return

        // Assemble list of recipient IDs
        val recipientIds = selectedRecipients.mapNotNull { it.stringId }

        // Assemble list of attachment IDs
        val attachmentIDs = LongArray(mAttachments.size)
        for (i in attachmentIDs.indices) {
            attachmentIDs[i] = mAttachments[i].id
        }

        // Assemble list of Message IDs
        val messageIds = LongArray(mMessages?.size ?: 0)
        for (i in messageIds.indices) {
            messageIds[i] = mMessages?.get(i)?.id ?: 0
        }

        // Send message
        InboxManager.addMessage(conversation?.id ?: 0, message, recipientIds, messageIds, attachmentIDs, conversation?.contextCode, mAddConversationCallback)
    }

    private val mAddConversationCallback = object : StatusCallback<Conversation>() {
        override fun onResponse(response: Response<Conversation>, linkHeaders: LinkHeaders, type: ApiType) {
            if (viewCallback != null) {
                viewCallback!!.messageSuccess()
            }
        }

        override fun onFail(call: Call<Conversation>?, error: Throwable, response: Response<*>?) {
            if (viewCallback != null) {
                viewCallback!!.messageFailure()
            }
        }
    }

    val attachments: List<Attachment>
        get() = mAttachments

    fun addAttachments(attachments: List<Attachment>) {
        mAttachments.addAll(attachments)
        viewCallback?.refreshAttachments()
    }

    fun removeAttachment(attachment: Attachment) {
        mAttachments.remove(attachment)
    }

    val course: Course?
        get() {
            if (mCourse == null) {
                var courseId: Long = 0

                if (conversation?.contextCode == null) {
                    mCourse = Course()
                    return mCourse
                }
                try {
                    courseId = conversation.contextCode!!.replace("course_", "").toLong()
                } catch (ignore: NumberFormatException) {
                }

                mCourse = Course(id = courseId, name = conversation.contextName!!)
            }
            return mCourse
        }

    fun getParticipantById(recipientId: String): Recipient? = mParticipants?.find { it.stringId == recipientId }
}
