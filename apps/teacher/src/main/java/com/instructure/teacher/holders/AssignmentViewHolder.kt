/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.holders

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterAssignmentBinding
import java.util.Date

class AssignmentViewHolder(private val binding: AdapterAssignmentBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        val ungradedCount = binding.root.findViewById<TextView>(R.id.ungradedCount)
        ungradedCount.setTextColor(ThemePrefs.brandColor)
        DrawableCompat.setTint(ungradedCount.background, ThemePrefs.brandColor)
    }

    fun bind(
        context: Context,
        assignment: Assignment,
        iconColor: Int, callback: (Assignment) -> Unit
    ) = with(binding) {
        assignmentLayout.setOnClickListener { callback(assignment) }
        assignmentTitle.text = assignment.name
        assignmentIcon.setIcon(assignment.getAssignmentIcon(), iconColor)
        assignmentIcon.setPublishedStatus(assignment.published)
        publishedBar.visibility = if (assignment.published) View.VISIBLE else View.INVISIBLE

        // String to track if the assignment is closed. If it isn't, we'll prepend the due date string with an empty string and it will look the same
        // Otherwise, we want it to say "Closed" and the due date with a dot as a separator
        var closedString: String = ""
        if (assignment.lockDate?.before(Date()) == true) {
            closedString = context.getString(R.string.cmp_closed) + context.getString(R.string.utils_dotWithSpaces)
        }

        if (assignment.allDates.size > 1) {
            //we have multiple due dates
            dueDate.text = closedString + context.getString(R.string.multiple_due_dates)
        } else {
            if (assignment.dueAt != null) {
                dueDate.text = closedString + context.getString(
                    R.string.due,
                    DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(context, assignment.dueDate, R.string.at)
                )
            } else if (assignment.allDates.size == 1 && assignment.allDates.get(0).dueAt != null) {
                // If a due date is to one section and nothing for everyone else, we can still show the due date
                dueDate.text = closedString + context.getString(
                    R.string.due,
                    DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(context, assignment.allDates.get(0).dueDate, R.string.at)
                )
            } else {
                dueDate.text = closedString + context.getString(R.string.no_due_date)
            }
        }

        if (assignment.needsGradingCount == 0L || assignment.gradingType == Assignment.NOT_GRADED_TYPE) {
            ungradedCount.setGone().text = ""
        } else {
            ungradedCount.setVisible().text = context.resources.getQuantityString(
                R.plurals.needsGradingCount,
                assignment.needsGradingCount.toInt(),
                NumberHelper.formatInt(assignment.needsGradingCount)
            )
            ungradedCount.isAllCaps = true
        }

        // set the content description on the container so we can tell the user that it is published as the last piece of information. When a content description is on a container
        assignmentLayout.contentDescription = assignment.name + " " + dueDate.text + " " + ungradedCount.text + " " + if (assignment.published) context.getString(R.string.published) else context.getString(
                R.string.not_published
            )
    }
}
