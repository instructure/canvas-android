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

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.isValid
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAvatarA11y
import com.instructure.teacher.R
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.databinding.AdapterSubmissionCommentBinding
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
import com.instructure.teacher.view.CommentAttachmentsView
import com.instructure.teacher.view.CommentDirection
import com.instructure.teacher.view.CommentMediaAttachmentView
import com.instructure.teacher.view.CommentSubmissionView
import com.instructure.teacher.view.CommentView

class SpeedGraderCommentHolder(private val binding: AdapterSubmissionCommentBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        wrapper: SubmissionCommentWrapper,
        currentUser: User,
        courseId: Long,
        assignee: Assignee,
        gradeAnonymously: Boolean,
        onAttachmentClicked: (Attachment) -> Unit,
        presenter: SpeedGraderCommentsPresenter,
        index: Int
    ): Unit = with(binding.commentHolder) {

        // Reset extra view container
        setExtraView(null)

        // Reset status
        alpha = 1f
        binding.errorLayout.setGone()
        binding.sendingLayout.setGone()
        setOnClickListener(null)
        isClickable = false

        val avatarView = binding.commentHolder.findViewById<ImageView>(R.id.avatarView)

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
                            RouteMatcher.route(context.getFragmentActivity(), Route(StudentContextFragment::class.java, null, bundle))
                        }
                    }
                    Triple(
                        comment.comment,
                        Pronouns.span(
                            comment.authorName ?: comment.author?.displayName,
                            comment.authorPronouns ?: comment.author?.pronouns
                        ),
                        comment.author?.avatarImageUrl
                    )
                } else {
                    avatarView.setAnonymousAvatar()
                    val authorName = if (comment.authorId == 0L && comment.authorName.isValid()) {
                        // When grading anonymously, Canvas may redact the author ID and provide an anonymous author
                        // name such as "Anonymous User" which we'll want to use to ensure that we're not displaying
                        // all comments as being authored by "Student."
                        comment.authorName
                    } else {
                        "${context.getString(R.string.anonymousStudentLabel)} ${index + 1}"
                    }
                    Triple(comment.comment, authorName, null)
                }
            }

        // Pending (uploading) comment
            is PendingCommentWrapper -> {
                alpha = 0.35f
                setDirection(CommentDirection.OUTGOING)
                when (wrapper.pendingComment.status) {
                    CommentSendStatus.SENDING -> binding.sendingLayout.setVisible()
                    CommentSendStatus.ERROR -> {
                        binding.errorLayout.setVisible()
                        onClick {
                            wrapper.pendingComment.workerInputData?.let {
                                presenter.retryFileUpload(wrapper.pendingComment)
                            } ?: presenter.sendComment(wrapper)
                        }
                    }
                    CommentSendStatus.DRAFT -> { /* Drafts should not display in the list, only in the EditText */ }
                }
                Triple(
                    wrapper.pendingComment.comment,
                    Pronouns.span(currentUser.name, currentUser.pronouns),
                    currentUser.avatarUrl
                )
            }

        // Submission Files
            is SubmissionWrapper -> {
                setDirection(CommentDirection.INCOMING)
                setExtraView(CommentSubmissionView(context, wrapper.submission))
                if (gradeAnonymously) {
                    avatarView.setAnonymousAvatar()
                    Triple(null, "${context.getString(R.string.anonymousStudentLabel)} ${index + 1}", null)
                } else when (assignee) {
                    is StudentAssignee -> {
                        avatarView.setupAvatarA11y(assignee.name)
                        avatarView.onClick {
                            val bundle = StudentContextFragment.makeBundle(assignee.id, courseId)
                            RouteMatcher.route(context.getFragmentActivity(), Route(StudentContextFragment::class.java, null, bundle))
                        }
                        Triple(null, Pronouns.span(assignee.name, assignee.pronouns), assignee.student.avatarUrl)
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
        if (avatarUrl != null) setAvatar(avatarUrl, authorName.toString())
    }

    @Suppress("LiftReturnOrAssignment")
    private fun CommentView.setDirection(commentDirection: CommentDirection) {
        direction = commentDirection
        when (commentDirection) {
            CommentDirection.INCOMING -> {
                setCommentBubbleColor(context.getColorCompat(R.color.backgroundLight))
                commentTextColor = context.getColorCompat(R.color.textDarkest)
            }
            CommentDirection.OUTGOING -> {
                setCommentBubbleColor(context.getColorCompat(R.color.backgroundInfo))
                commentTextColor = context.getColor(R.color.textLightest)
            }
        }
    }
}
