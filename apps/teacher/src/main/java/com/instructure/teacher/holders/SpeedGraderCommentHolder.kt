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
package com.instructure.teacher.holders

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAvatarA11y
import com.instructure.teacher.R
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.models.CommentWrapper
import com.instructure.teacher.models.PendingCommentWrapper
import com.instructure.teacher.models.SubmissionCommentWrapper
import com.instructure.teacher.models.SubmissionWrapper
import com.instructure.teacher.presenters.SpeedGraderCommentsPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.getSubmissionFormattedDate
import com.instructure.teacher.utils.iconRes
import com.instructure.teacher.utils.setAnonymousAvatar
import com.instructure.teacher.view.*
import kotlinx.android.synthetic.main.adapter_submission_comment.view.*
import kotlinx.android.synthetic.main.view_comment.view.*

class SpeedGraderCommentHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_submission_comment
    }

    fun bind(
            wrapper: SubmissionCommentWrapper,
            currentUser: User,
            courseId: Long,
            assignee: Assignee,
            gradeAnonymously: Boolean,
            onAttachmentClicked: (Attachment) -> Unit,
            presenter: SpeedGraderCommentsPresenter
    ): Unit = with(itemView.commentHolder) {

        // Reset extra view container
        setExtraView(null)

        // Reset status
        alpha = 1f
        itemView.errorLayout.setGone()
        itemView.sendingLayout.setGone()
        setOnClickListener(null)
        isClickable = false


        val (text, authorName, avatarUrl) = when (wrapper) {

        // Comment
            is CommentWrapper -> {
                val comment = wrapper.comment
                setDirection(if (currentUser.id == comment.authorId) CommentDirection.OUTGOING else CommentDirection.INCOMING)
                if (comment.mediaComment != null) {
                    setExtraView(CommentMediaAttachmentView(context, comment.mediaComment!!, onAttachmentClicked))
                    comment.comment = null
                } else {
                    setExtraView(CommentAttachmentsView(context, comment.attachments, onAttachmentClicked))
                }
                if (currentUser.id == comment.authorId || !gradeAnonymously) {
                    if (currentUser.id != comment.authorId) {
                        avatarView.setupAvatarA11y(comment.authorName)
                        avatarView.onClick {
                            val bundle = StudentContextFragment.makeBundle(comment.authorId, courseId)
                            RouteMatcher.route(context, Route(StudentContextFragment::class.java, null, bundle))
                        }
                    }
                    Triple(comment.comment, comment.authorName ?: comment.author?.displayName ?: "", comment.author?.avatarImageUrl)
                } else {
                    avatarView.setAnonymousAvatar()
                    Triple(comment.comment, context.getString(R.string.anonymousStudentLabel), null)
                }
            }

        // Pending (uploading) comment
            is PendingCommentWrapper -> {
                alpha = 0.35f
                setDirection(CommentDirection.OUTGOING)
                when (wrapper.pendingComment.status) {
                    CommentSendStatus.SENDING -> itemView.sendingLayout.setVisible()
                    CommentSendStatus.ERROR -> {
                        itemView.errorLayout.setVisible()
                        onClick { presenter.sendComment(wrapper) }
                    }
                    CommentSendStatus.DRAFT -> { /* Drafts should not display in the list, only in the EditText */ }
                }
                Triple(wrapper.pendingComment.comment, currentUser.name, currentUser.avatarUrl)
            }

        // Submission Files
            is SubmissionWrapper -> {
                setDirection(CommentDirection.INCOMING)
                setExtraView(CommentSubmissionView(context, wrapper.submission))
                if (gradeAnonymously) {
                    avatarView.setAnonymousAvatar()
                    Triple(null, context.getString(R.string.anonymousStudentLabel), null)
                } else when (assignee) {
                    is StudentAssignee -> {
                        avatarView.setupAvatarA11y(assignee.name)
                        avatarView.onClick {
                            val bundle = StudentContextFragment.makeBundle(assignee.id, courseId)
                            RouteMatcher.route(context, Route(StudentContextFragment::class.java, null, bundle))
                        }
                        Triple(null, assignee.name, assignee.student.avatarUrl)
                    }
                    is GroupAssignee -> {
                        avatarView.setImageResource(assignee.iconRes)
                        Triple(null, assignee.name, null)
                    }
                }
            }
        }

        usernameText = authorName
        dateText = wrapper.date.getSubmissionFormattedDate(context)
        commentText = text
        if (avatarUrl != null) setAvatar(avatarUrl, authorName)
    }

    @Suppress("LiftReturnOrAssignment")
    private fun CommentView.setDirection(commentDirection: CommentDirection) {
        direction = commentDirection
        when (commentDirection) {
            CommentDirection.INCOMING -> {
                setCommentBubbleColor(context.getColorCompat(R.color.commentBubbleIncoming))
                commentTextColor = context.getColorCompat(R.color.defaultTextDark)
            }
            CommentDirection.OUTGOING -> {
                setCommentBubbleColor(context.getColorCompat(R.color.commentBubbleOutgoing))
                commentTextColor = Color.WHITE
            }
        }
    }
}
