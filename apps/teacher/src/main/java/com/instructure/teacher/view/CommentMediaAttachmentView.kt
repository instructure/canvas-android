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
import android.view.View
import android.widget.LinearLayout
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.teacher.R
import com.instructure.canvasapi2.utils.asAttachment
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import kotlinx.android.synthetic.main.comment_attachment_view.view.*

@SuppressLint("ViewConstructor")
class CommentMediaAttachmentView(context: Context, mediaComment: MediaComment, onAttachmentClicked: (Attachment) -> Unit) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        val view = View.inflate(context, R.layout.comment_attachment_view, this)
        when (mediaComment.mediaType) {
            MediaComment.MediaType.AUDIO -> {
                view.iconImageView.setImageResource(R.drawable.vd_audio)
                view.attachmentNameTextView.text = context.getString(R.string.mediaUploadAudio)
            }
            MediaComment.MediaType.VIDEO -> {
                view.iconImageView.setImageResource(R.drawable.vd_media)
                view.attachmentNameTextView.text = context.getString(R.string.mediaUploadVideo)
            }
        }
        view.onClickWithRequireNetwork { onAttachmentClicked(mediaComment.asAttachment()) }
    }
}
