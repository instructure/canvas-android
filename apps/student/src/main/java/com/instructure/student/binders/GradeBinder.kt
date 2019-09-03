/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.binders

import android.content.Context
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.GradesListRecyclerAdapter
import com.instructure.student.dialog.WhatIfDialogStyled
import com.instructure.student.holders.GradeViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

object GradeBinder : BaseBinder() {

    @JvmStatic
    fun bind(
            holder: GradeViewHolder,
            context: Context,
            courseColor: Int,
            assignment: Assignment,
            isEdit: Boolean,
            whatIfDialogCallback: WhatIfDialogStyled.WhatIfDialogCallback,
            adapterToFragmentCallback: AdapterToFragmentCallback<Assignment>,
            selectedItemCallback: GradesListRecyclerAdapter.SetSelectedItemCallback) {

        holder.itemView.setOnClickListener {
            adapterToFragmentCallback.onRowClicked(assignment, holder.adapterPosition, true)
            selectedItemCallback.setSelected(holder.adapterPosition)
        }

        holder.title.text = assignment.name

        holder.icon.setIcon(BaseBinder.getAssignmentIcon(assignment), courseColor)
        holder.icon.hideNestedIcon()

        holder.points.setTextColor(ThemePrefs.brandColor)

        if ((assignment.muted && assignment.submission?.postedAt == null) && !isEdit) {
            // Mute that score
            holder.points.setGone()
        } else {
            val submission = assignment.submission
            if (submission != null && Const.PENDING_REVIEW == submission.workflowState) {
                holder.points.setGone()
                holder.icon.setNestedIcon(R.drawable.vd_published, courseColor)
            } else {
                holder.points.setVisible()
                val (grade, contentDescription) = getGrade(assignment, submission, context)
                holder.points.text = grade
                holder.points.contentDescription = contentDescription ?: grade
            }
        }


        // Configures whatIf editing boxes and listener for dialog
        holder.edit.setVisible(isEdit)
        if (isEdit) {
            holder.edit.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_edit, ContextCompat.getColor(context, R.color.defaultTextDark)))
            holder.edit.setOnClickListener { whatIfDialogCallback.onClick(assignment, holder.adapterPosition) }
        }

        if (assignment.dueAt != null) {
            holder.date.text = DateHelper.getDayMonthDateString(context, assignment.dueDate)
        } else {
            holder.date.text = ""
        }
        holder.date.setVisible(holder.date.text.isNotBlank())
    }
}
