/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.events.SubmissionCommentsUpdated
import com.instructure.teacher.events.SubmissionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.models.CommentWrapper
import com.instructure.teacher.models.PendingCommentWrapper
import com.instructure.teacher.models.SubmissionCommentWrapper
import com.instructure.teacher.models.SubmissionWrapper
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.viewinterface.SpeedGraderCommentsView
import instructure.androidblueprint.ListPresenter
import kotlinx.coroutines.Job

class SpeedGraderCommentsPresenter(
        rawComments: List<SubmissionComment>,
        val submissionHistory: List<Submission>,
        val assignee: Assignee,
        val courseId: Long,
        val assignmentId: Long,
        val groupMessage: Boolean
) : ListPresenter<SubmissionCommentWrapper, SpeedGraderCommentsView>(SubmissionCommentWrapper::class.java) {

    val mPageId = "${ApiPrefs.domain}-$courseId-$assignmentId-${assignee.id}"

    private val comments = rawComments.map { CommentWrapper(it) }
    private var sendingJob: Job? = null

    override fun loadData(forceNetwork: Boolean) {

        if (forceNetwork) {
            // Grab new submission comments
            tryWeave {
                // Get updated submission
                val updatedSubmission: Submission = awaitApi { SubmissionManager.getSingleSubmission(courseId, assignmentId, assignee.id, it, true) }

                // Add normal comments
                addNormalComments(updatedSubmission.submissionComments)

                // Add submission history as comments
                addSubmissionHistoryAsComments(updatedSubmission.submissionHistory.mapNotNull { it })

                // Add pending comments
                addPendingComments()

                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            } catch {
                Logger.d("${it.stackTrace}")
            }
        } else {
            // Use cached comments

            // Add normal comments
            data.addOrUpdate(comments)

            // Add submission history as comments
            addSubmissionHistoryAsComments(submissionHistory)

            // Add pending comments
            addPendingComments()

            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
        }
    }

    fun addNormalComments(comments: List<SubmissionComment>) = data.addOrUpdate(comments.map { CommentWrapper(it) })

    fun addSubmissionHistoryAsComments(submissionHistory: List<Submission>) = data.addOrUpdate(submissionHistory.map { SubmissionWrapper(it) })

    fun addPendingComments() {
        // Add pending comments
        val (drafts, pending) = TeacherPrefs.pendingSubmissionComments
                .filter { it.pageId == mPageId }
                .map { PendingCommentWrapper(it) }
                .partition { it.pendingComment.status == CommentSendStatus.DRAFT }
        drafts.firstOrNull()?.let { viewCallback?.setDraftText(it.pendingComment.comment) }
        data.addOrUpdate(pending)
    }

    override fun refresh(forceNetwork: Boolean) {
        data.clear()
        loadData(forceNetwork)
    }

    override fun getItemId(item: SubmissionCommentWrapper) = item.id

    override fun compare(item1: SubmissionCommentWrapper, item2: SubmissionCommentWrapper): Int =
            item1.date.compareTo(item2.date)

    fun sendComment(text: String) {
        viewCallback?.setDraftText("")
        var pendingComments = TeacherPrefs.pendingSubmissionComments
        val drafts = pendingComments.filter { it.pageId == mPageId && it.status == CommentSendStatus.DRAFT }
        pendingComments -= drafts
        val newComment = PendingSubmissionComment(mPageId, text)
        pendingComments += newComment
        TeacherPrefs.pendingSubmissionComments = pendingComments
        val commentWrapper = PendingCommentWrapper(newComment)
        data.add(commentWrapper)
        viewCallback?.checkIfEmpty()
        viewCallback?.scrollToBottom()
        sendComment(commentWrapper)
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun sendComment(comment: PendingCommentWrapper) {

        // Update status in SharedPrefs
        var pendingComments = TeacherPrefs.pendingSubmissionComments
        pendingComments.find { it.id == comment.id }?.let {
            it.status = CommentSendStatus.SENDING
            TeacherPrefs.pendingSubmissionComments = pendingComments
        }

        // Update status in list
        comment.pendingComment.status = CommentSendStatus.SENDING
        data.addOrUpdate(comment)

        sendingJob = weave {
            try {
                val groupMessage = groupMessage
                val userId = when (assignee) {
                    is StudentAssignee -> assignee.student.id
                    is GroupAssignee -> assignee.students.firstOrNull()?.id ?: assignee.id
                }

                val submission = awaitApi<Submission> {
                    SubmissionManager.postSubmissionComment(courseId, assignmentId, userId, comment.pendingComment.comment ?: "", groupMessage, arrayListOf(), it)
                }

                submission.submissionComments.lastOrNull()?.let {
                    // Remove pending comment from sharedPrefs
                    pendingComments = TeacherPrefs.pendingSubmissionComments
                    pendingComments.find { it.id == comment.id }?.let {
                        pendingComments -= it
                        TeacherPrefs.pendingSubmissionComments = pendingComments
                    }

                    // Remove pending comment from list
                    data.remove(comment)

                    // Add new comment to list
                    data.add(CommentWrapper(it))
                    viewCallback?.checkIfEmpty()
                }
                SubmissionUpdatedEvent(submission).post()
                SubmissionCommentsUpdated().post()
                viewCallback?.scrollToBottom()
            } catch (e: Throwable) {
                // Update status in SharedPrefs
                pendingComments = TeacherPrefs.pendingSubmissionComments
                pendingComments.find { it.id == comment.id }?.let {
                    it.status = CommentSendStatus.ERROR
                    TeacherPrefs.pendingSubmissionComments = pendingComments
                }

                // Update status in list
                comment.pendingComment.status = CommentSendStatus.ERROR
                data.addOrUpdate(comment)
            }
        }
    }

    fun updatePendingComments(comments: MutableList<Pair<PendingSubmissionComment, SubmissionComment?>>) {

        for ((pendingComment, submissionComment) in comments) {
            if (submissionComment != null) {
                var pendingComments = TeacherPrefs.pendingSubmissionComments
                pendingComments.find { it.filePath == pendingComment.filePath }?.let {
                    pendingComments -= it
                    TeacherPrefs.pendingSubmissionComments = pendingComments
                }

                // Remove pending comment from list
                data.remove(PendingCommentWrapper(pendingComment))

                // Add new comment to list
                data.add(CommentWrapper(submissionComment))
                viewCallback?.checkIfEmpty()
            } else {
                // submissionComment is null if there was an error sending the pending intent
                data.addOrUpdate(PendingCommentWrapper(pendingComment))
            }
        }
    }

    fun createPendingMediaComment(filePath: String) {
        val newComment = PendingSubmissionComment(mPageId)
        newComment.filePath = filePath
        newComment.status = CommentSendStatus.SENDING
        var pendingComments = TeacherPrefs.pendingSubmissionComments
        pendingComments += newComment
        TeacherPrefs.pendingSubmissionComments = pendingComments
        val commentWrapper = PendingCommentWrapper(newComment)

        //add to list
        data.addOrUpdate(commentWrapper)

        viewCallback?.checkIfEmpty()
        viewCallback?.scrollToBottom()
    }

    override fun onDestroyed() {
        sendingJob?.cancel()
        super.onDestroyed()
    }

    fun saveDraft(text: String) {
        var pending = TeacherPrefs.pendingSubmissionComments
        val currentDrafts = pending.filter { it.pageId == mPageId && it.status == CommentSendStatus.DRAFT }
        pending -= currentDrafts
        if (!text.isBlank()) pending += PendingSubmissionComment(mPageId, text)
        TeacherPrefs.pendingSubmissionComments = pending
    }
}
