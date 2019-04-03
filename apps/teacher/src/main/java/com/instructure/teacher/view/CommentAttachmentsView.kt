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
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.utils.onClick
import com.instructure.teacher.R
import com.instructure.teacher.utils.iconRes
import kotlinx.android.synthetic.main.comment_attachment_view.view.*

@SuppressLint("ViewConstructor")
class CommentAttachmentsView(context: Context, val attachments: List<Attachment>, val onClicked: (Attachment) -> Unit) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        for (attachment in attachments) {
            val view = LayoutInflater.from(context).inflate(R.layout.comment_attachment_view, this, false)
            view.iconImageView.setImageResource(attachment.iconRes)
            view.attachmentNameTextView.text = attachment.displayName
            view.onClick { onClicked(attachment) }
            addView(view)
        }
    }
}
