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
package com.instructure.student.holders

import android.content.Context
import android.graphics.Typeface
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.student.R
import com.instructure.student.util.BinderUtils
import com.instructure.student.interfaces.AdapterToFragmentCallback
import kotlinx.android.synthetic.main.viewholder_card_generic.view.*

class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(
        context: Context,
        assignment: Assignment,
        courseColor: Int,
        adapterToFragmentCallback: AdapterToFragmentCallback<Assignment>
    ) = with(itemView) {
        title.text = assignment.name

        setOnClickListener { adapterToFragmentCallback.onRowClicked(assignment, adapterPosition, true) }

        val courseId = assignment.courseId
        val color = CanvasContext.emptyCourseContext(courseId).textAndIconColor

        val submission = assignment.submission

        // Posted At now determines if an assignment is muted, even for old gradebook
        if (submission?.postedAt == null) {
            // Mute that score
            points.visibility = View.GONE
        } else {
            points.visibility = View.VISIBLE
            BinderUtils.setupGradeText(context, points, assignment, submission, courseColor)
        }

        val drawable = BinderUtils.getAssignmentIcon(assignment)
        icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, drawable, color))

        if (assignment.dueAt != null) {
            date.text = DateHelper.createPrefixedDateTimeString(context, R.string.toDoDue, assignment.dueDate)
        } else {
            date.text = context.resources.getString(R.string.toDoNoDueDate)
        }

        val descriptionText: String? // Set description to assignment description or excused
        if (submission != null && submission.excused) {
            descriptionText = context.getString(R.string.excusedAssignment)
        } else {
            descriptionText = BinderUtils.getHtmlAsText(assignment.description ?: "")
        }

        description.setTextForVisibility(descriptionText)
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_card_generic
    }
}
