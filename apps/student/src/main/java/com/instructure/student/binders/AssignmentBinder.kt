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
import android.graphics.Typeface
import android.view.View
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R
import com.instructure.student.holders.AssignmentViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

class AssignmentBinder : BaseBinder() {
    companion object {

        fun bind(
                context: Context,
                holder: AssignmentViewHolder,
                assignment: Assignment,
                courseColor: Int,
                adapterToFragmentCallback: AdapterToFragmentCallback<Assignment>) {

            holder.title.text = assignment.name

            holder.itemView.setOnClickListener { adapterToFragmentCallback.onRowClicked(assignment, holder.adapterPosition, true) }

            val courseId = assignment.courseId
            val color = ColorKeeper.getOrGenerateColor(CanvasContext.makeContextId(CanvasContext.Type.COURSE, courseId))

            val submission = assignment.submission

            if (assignment.muted) {
                // Mute that score
                holder.points.visibility = View.GONE
            } else {
                holder.points.visibility = View.VISIBLE
                BaseBinder.setupGradeText(context, holder.points, assignment, submission, courseColor)
            }


            val drawable = BaseBinder.getAssignmentIcon(assignment)
            holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, drawable, color))

            if (assignment.dueAt != null) {
                holder.date.text = DateHelper.createPrefixedDateTimeString(context, R.string.toDoDue, assignment.dueDate)
            } else {
                holder.date.text = context.resources.getString(R.string.toDoNoDueDate)
            }

            val description: String? // Set description to assignment description or excused
            if (submission != null && submission.excused) {
                description = context.getString(R.string.excusedAssignment)
                holder.description.setTypeface(null, Typeface.BOLD)
            } else {
                description = BaseBinder.getHtmlAsText(assignment.description ?: "")
                holder.description.setTypeface(null, Typeface.NORMAL)
            }

            BaseBinder.setCleanText(holder.description, description)
            if (description.isNullOrBlank()) {
                BaseBinder.setGone(holder.description)
            } else {
                BaseBinder.setVisible(holder.description)
            }

        }
    }
}
