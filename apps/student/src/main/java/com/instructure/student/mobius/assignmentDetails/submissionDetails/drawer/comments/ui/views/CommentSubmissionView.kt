/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.text.format.Formatter
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.prettyPrint
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.iconRes
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.student.R
import com.instructure.student.databinding.ViewCommentSubmissionAttachmentBinding

@SuppressLint("ViewConstructor")
class CommentSubmissionView(
    context: Context,
    private val submission: Submission,
    private val tint: Int,
    private val onSubmissionClicked: (submission: Submission) -> Unit,
    private val onAttachmentClicked: (Submission, Attachment) -> Unit
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        gravity = Gravity.END
        val type = submission.submissionType?.let { Assignment.getSubmissionTypeFromAPIString(it) }
        val hasSubmission = submission.workflowState != "unsubmitted" && type != null
        if (hasSubmission) {
            if (type == SubmissionType.ONLINE_UPLOAD) {
                setupAttachments()
            } else {
                setupSubmissionAsAttachment(type!!)
            }
        }
    }

    private fun setupSubmissionAsAttachment(type: SubmissionType) {
        val binding = ViewCommentSubmissionAttachmentBinding.inflate(LayoutInflater.from(context), this, false)
        binding.iconImageView.setColorFilter(tint)

        val (icon: Int, title: String, subtitle: String?) = when (type) {
            SubmissionType.ONLINE_TEXT_ENTRY -> {
                Triple(
                    R.drawable.ic_document,
                    context.getString(R.string.commentSubmissionTypeText),
                    quotedFromHtml(submission.body)
                )
            }
            SubmissionType.EXTERNAL_TOOL -> {
                Triple(
                    R.drawable.ic_lti,
                    context.getString(R.string.commentSubmissionTypeExternalTool),
                    submission.url
                )
            }
            SubmissionType.DISCUSSION_TOPIC -> {
                Triple(
                    R.drawable.ic_discussion,
                    context.getString(R.string.commentSubmissionTypeDiscussion),
                    quotedFromHtml(submission.discussionEntries.firstOrNull()?.message)
                )
            }
            SubmissionType.ONLINE_QUIZ -> {
                Triple(
                    R.drawable.ic_quiz,
                    context.getString(R.string.commentSubmissionTypeQuiz),
                    context.getString(R.string.commentSubmissionTypeQuizAttempt, submission.attempt)
                )
            }
            SubmissionType.MEDIA_RECORDING -> {
                val media = submission.mediaComment ?: throw IllegalStateException("Media comment is null for media submission. WHY!?")
                val subtitle = when (media.mediaType) {
                    MediaComment.MediaType.AUDIO -> context.getString(R.string.commentSubmissionTypeAudio)
                    MediaComment.MediaType.VIDEO -> context.getString(R.string.commentSubmissionTypeVideo)
                    else -> ""
                }
                Triple(R.drawable.ic_media, context.getString(R.string.commentSubmissionTypeMediaFile), subtitle)
            }
            SubmissionType.ONLINE_URL -> {
                Triple(R.drawable.ic_link, context.getString(R.string.onlineURL), submission.url)
            }
            else -> Triple(R.drawable.ic_attachment, type.prettyPrint(context), "")
        }

        binding.iconImageView.setImageResource(icon)
        binding.titleTextView.text = title
        if (subtitle.isNullOrBlank()) {
            binding.subtitleTextView.setGone()
        } else {
            binding.subtitleTextView.text = subtitle
        }

        binding.root.onClick { onSubmissionClicked(submission) }
        addView(binding.root)
    }

    @Suppress("DEPRECATION")
    private fun quotedFromHtml(html: String?): String? {
        if (html == null) return null
        return "\"" + Html.fromHtml(html) + "\""
    }

    private fun setupAttachments() {
        submission.attachments.forEachIndexed { index, attachment ->
            val binding = ViewCommentSubmissionAttachmentBinding.inflate(LayoutInflater.from(context), this, false)
            binding.iconImageView.setColorFilter(tint)
            binding.iconImageView.setImageResource(attachment.iconRes)
            binding.titleTextView.text = attachment.displayName
            binding.subtitleTextView.text = Formatter.formatFileSize(context, attachment.size)
            binding.root.onClick { onAttachmentClicked(submission, attachment) }
            if (index > 0) {
                (binding.root.layoutParams as LayoutParams).topMargin = context.DP(4).toInt()
            }
            addView(binding.root)
        }
    }
}
