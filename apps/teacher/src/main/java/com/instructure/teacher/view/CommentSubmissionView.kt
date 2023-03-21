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
package com.instructure.teacher.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Html
import android.text.format.Formatter
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.prettyPrint
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.CommentSubmissionAttachmentViewBinding
import com.instructure.teacher.utils.getColorCompat
import org.greenrobot.eventbus.EventBus

@SuppressLint("ViewConstructor")
class CommentSubmissionView(context: Context, val submission: Submission) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        val type = submission.submissionType?.let { Assignment.getSubmissionTypeFromAPIString(it) }
        val hasSubmission = submission.workflowState != "unsubmitted" && type != null
        if (hasSubmission) {
            if (type == SubmissionType.ONLINE_UPLOAD) {
                setupAttachmentLabel()
                setupAttachments()
            } else {
                setupSubmissionAsAttachment(type!!)
            }
        }
    }

    private fun setupSubmissionAsAttachment(type: SubmissionType) {
        val binding = CommentSubmissionAttachmentViewBinding.inflate(LayoutInflater.from(context), this, false)
        binding.iconImageView.setColorFilter(ThemePrefs.brandColor)

        val (icon: Int, title: String, subtitle: String?) = when (type) {
            SubmissionType.ONLINE_TEXT_ENTRY -> {
                Triple(R.drawable.ic_document, context.getString(R.string.speedGraderTextSubmission), quotedFromHtml(submission.body))
            }
            SubmissionType.EXTERNAL_TOOL -> {
                Triple(R.drawable.ic_lti, context.getString(R.string.speedGraderExternalToolSubmission), submission.url)
            }
            SubmissionType.DISCUSSION_TOPIC -> {
                Triple(R.drawable.ic_discussion, context.getString(R.string.speedGraderDiscussionSubmission), quotedFromHtml(submission.discussionEntries.firstOrNull()?.message))
            }
            SubmissionType.ONLINE_QUIZ -> {
                Triple(R.drawable.ic_quiz, context.getString(R.string.speedGraderQuizSubmission), context.getString(R.string.speedgraderCommentQuizAttempt, submission.attempt))
            }
            SubmissionType.MEDIA_RECORDING -> {
                val media = submission.mediaComment ?: throw IllegalStateException("Media comment is null for media submission. WHY!?")
                val subtitle = when (media.mediaType) {
                    MediaComment.MediaType.AUDIO -> context.getString(R.string.submissionTypeAudio)
                    MediaComment.MediaType.VIDEO -> context.getString(R.string.submissionTypeVideo)
                    else -> ""
                }
                Triple(R.drawable.ic_media, context.getString(R.string.speedGraderMediaFile), subtitle)
            }
            SubmissionType.ONLINE_URL -> {
                Triple(R.drawable.ic_link, context.getString(R.string.speedGraderUrlSubmission), submission.url)
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

        binding.root.onClick { EventBus.getDefault().post(SubmissionSelectedEvent(submission)) }
        addView(binding.root)
    }

    @Suppress("DEPRECATION")
    private fun quotedFromHtml(html: String?): String? {
        if (html == null) return null
        return "\"" + Html.fromHtml(html) + "\""
    }

    private fun setupAttachmentLabel() {
        val titleView = AppCompatTextView(context)
        titleView.typeface = Typeface.create("sans-serif-medium", Typeface.ITALIC)
        addView(titleView)
        titleView.text = context.getString(R.string.speedgraderCommentSubmittedFiles)
        titleView.setTextColor(context.getColorCompat(R.color.textDark))
    }

    private fun setupAttachments() {
        for (attachment in submission.attachments) {
            val binding = CommentSubmissionAttachmentViewBinding.inflate(LayoutInflater.from(context), this, false)
            binding.iconImageView.setColorFilter(ThemePrefs.brandColor)
            binding.iconImageView.setImageResource(attachment.iconRes)
            binding.titleTextView.text = attachment.displayName
            binding.subtitleTextView.text = Formatter.formatFileSize(context, attachment.size)
            binding.root.onClick {
                EventBus.getDefault().post(SubmissionSelectedEvent(submission))
                EventBus.getDefault().post(SubmissionFileSelectedEvent(submission.id, attachment))
            }
            (binding.root.layoutParams as LayoutParams).topMargin = context.DP(4).toInt()
            addView(binding.root)
        }
    }
}
