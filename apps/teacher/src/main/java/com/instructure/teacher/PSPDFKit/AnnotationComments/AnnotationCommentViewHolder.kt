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
package com.instructure.teacher.PSPDFKit.AnnotationComments

import android.view.Gravity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterAnnotationCommentBinding

class AnnotationCommentViewHolder(private val binding: AdapterAnnotationCommentBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        annotation: CanvaDocAnnotation,
        canEdit: Boolean,
        canDelete: Boolean,
        editCallback: (CanvaDocAnnotation, Int) -> Unit,
        deleteCallback: (CanvaDocAnnotation, Int) -> Unit
    ) = with(binding) {
        val context = binding.root.context
        commentAuthorTextView.text = annotation.userName
        commentDateTextView.text = DateHelper.getMonthDayAtTime(context, DateHelper.stringToDateWithMillis(annotation.createdAt), context.getString(R.string.at))
        commentContentsTextView.text = annotation.contents

        commentEditIcon.setVisible((canEdit || canDelete) && !annotation.deleted)

        if(annotation.deleted) {
            commentRemovedLabel.setVisible()
            val date = DateHelper.getMonthDayAtTime(context, DateHelper.stringToDateWithMillis(annotation.deletedAt), context.getString(R.string.at))
            commentRemovedLabel.text = context.resources.getString(R.string.removedComment, date, annotation.deletedBy)
        } else {
            commentRemovedLabel.setGone()
        }

        commentEditIcon.onClick {
            val popup = PopupMenu(context, it, Gravity.TOP, 0, com.google.android.material.R.style.Widget_Material3_PopupMenu_Overflow)
            popup.inflate(R.menu.menu_edit_annotation_comment)
            if(!canEdit) popup.menu.removeItem(R.id.edit)
            if(!canDelete) popup.menu.removeItem(R.id.delete)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit -> editCallback(annotation, adapterPosition)
                    R.id.delete -> deleteCallback(annotation, adapterPosition)
                }
                true
            }
            popup.show()
        }
    }
}
