/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.AnnotationComments

import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderAnnotationCommentBinding

class AnnotationCommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        const val holderRes = R.layout.viewholder_annotation_comment
    }

    fun bind(annotation: CanvaDocAnnotation, canEdit: Boolean, canDelete: Boolean, editCallback: (CanvaDocAnnotation, Int) -> Unit, deleteCallback: (CanvaDocAnnotation, Int) -> Unit) = with(itemView) {
        val binding = ViewholderAnnotationCommentBinding.bind(itemView)
        binding.commentAuthorTextView.text = annotation.userName
        binding.commentDateTextView.text = DateHelper.getMonthDayAtTime(context, DateHelper.stringToDateWithMillis(annotation.createdAt), context.getString(R.string.at))
        binding.commentContentsTextView.text = annotation.contents

        binding.commentEditIcon.setVisible((canEdit || canDelete) && !annotation.deleted)

        if(annotation.deleted) {
            binding.commentRemovedLabel.setVisible()
            val date = DateHelper.getMonthDayAtTime(context, DateHelper.stringToDateWithMillis(annotation.deletedAt), context.getString(R.string.at))
            binding.commentRemovedLabel.text = resources.getString(R.string.removedComment, date, annotation.deletedBy)
        } else {
            binding.commentRemovedLabel.setGone()
        }

        binding.commentEditIcon.onClick {
            val popup = PopupMenu(context, it, Gravity.TOP, 0,
                    R.style.Base_Widget_AppCompat_PopupMenu_Overflow)
            popup.inflate(R.menu.menu_edit_annotation_comment)
            if(!canEdit) popup.menu.removeItem(R.id.edit)
            if(!canDelete) popup.menu.removeItem(R.id.delete)
            popup.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.edit -> {
                        editCallback(annotation, adapterPosition)
                    }
                    R.id.delete -> { deleteCallback(annotation, adapterPosition) }
                }
                true
            }
            popup.show()
        }
    }
}
