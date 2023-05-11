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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.utils.asAttachment
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.emeritus.student.R
import kotlinx.android.synthetic.main.view_comment_attachment.view.*

@SuppressLint("ViewConstructor")
class CommentMediaAttachmentView(
    context: Context,
    mediaComment: MediaComment,
    tint: Int,
    onAttachmentClicked: (Attachment) -> Unit
) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        val view = View.inflate(context, R.layout.view_comment_attachment, this)
        when (mediaComment.mediaType) {
            MediaComment.MediaType.AUDIO -> {
                view.iconImageView.setImageResource(R.drawable.ic_audio)
                view.attachmentNameTextView.text = context.getString(R.string.mediaUploadAudio)
            }
            MediaComment.MediaType.VIDEO -> {
                view.iconImageView.setImageResource(R.drawable.ic_media)
                view.attachmentNameTextView.text = context.getString(R.string.mediaUploadVideo)
            }
            else -> {
                view.iconImageView.setImageResource(R.drawable.ic_media)
                view.attachmentNameTextView.text = context.getString(R.string.mediaUpload)
            }
        }
        view.iconImageView.setColorFilter(tint)
        view.onClickWithRequireNetwork { onAttachmentClicked(mediaComment.asAttachment()) }
    }
}
