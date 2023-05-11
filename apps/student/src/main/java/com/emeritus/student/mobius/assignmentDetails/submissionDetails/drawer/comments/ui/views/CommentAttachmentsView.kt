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
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.utils.iconRes
import com.instructure.pandautils.utils.onClick
import com.emeritus.student.R
import kotlinx.android.synthetic.main.view_comment_attachment.view.*

@SuppressLint("ViewConstructor")
class CommentAttachmentsView(
    context: Context,
    val attachments: List<Attachment>,
    val tint: Int,
    val onClicked: (Attachment) -> Unit
) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        gravity = Gravity.END
        for (attachment in attachments) {
            val view = LayoutInflater.from(context).inflate(R.layout.view_comment_attachment, this, false)
            view.iconImageView.setImageResource(attachment.iconRes)
            view.iconImageView.setColorFilter(tint)
            view.attachmentNameTextView.text = attachment.displayName
            view.onClick { onClicked(attachment) }
            addView(view)
        }
    }
}
