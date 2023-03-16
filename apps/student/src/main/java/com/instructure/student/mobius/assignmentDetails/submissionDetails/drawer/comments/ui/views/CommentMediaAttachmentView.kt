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
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.utils.asAttachment
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.student.R
import com.instructure.student.databinding.ViewCommentAttachmentBinding

@SuppressLint("ViewConstructor")
class CommentMediaAttachmentView(
    context: Context,
    mediaComment: MediaComment,
    tint: Int,
    onAttachmentClicked: (Attachment) -> Unit
) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        val binding = ViewCommentAttachmentBinding.inflate(LayoutInflater.from(context), this, false)
        when (mediaComment.mediaType) {
            MediaComment.MediaType.AUDIO -> {
                binding.iconImageView.setImageResource(R.drawable.ic_audio)
                binding.attachmentNameTextView.text = context.getString(R.string.mediaUploadAudio)
            }
            MediaComment.MediaType.VIDEO -> {
                binding.iconImageView.setImageResource(R.drawable.ic_media)
                binding.attachmentNameTextView.text = context.getString(R.string.mediaUploadVideo)
            }
            else -> {
                binding.iconImageView.setImageResource(R.drawable.ic_media)
                binding.attachmentNameTextView.text = context.getString(R.string.mediaUpload)
            }
        }
        binding.iconImageView.setColorFilter(tint)
        binding.root.onClickWithRequireNetwork { onAttachmentClicked(mediaComment.asAttachment()) }
    }
}
