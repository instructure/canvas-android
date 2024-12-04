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

package com.instructure.teacher.holders

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterTodoBinding
import com.instructure.teacher.interfaces.AdapterToFragmentCallback
import java.util.Date

class ToDoViewHolder(private val binding: AdapterTodoBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        val ungradedCount = itemView.findViewById<TextView>(R.id.ungradedCount)
        ungradedCount.setTextColor(ThemePrefs.brandColor)
        DrawableCompat.setTint(ungradedCount.background, ThemePrefs.brandColor)
    }

    fun bind(context: Context, toDo: ToDo, callback: AdapterToFragmentCallback<ToDo>, position: Int) = with(binding) {
        toDoLayout.setOnClickListener { callback.onRowClicked(toDo, position) }
        toDoTitle.text = toDo.title

        var color = ThemePrefs.brandColor

        toDo.canvasContext?.let {
            color = toDo.canvasContext.color
            toDoCourse.setVisible()
            toDoCourse.setTextColor(color)
            toDoCourse.text = toDo.canvasContext!!.name
        } ?: toDoCourse.setGone()


        toDoIcon.setImageDrawable(ContextCompat.getDrawable(context, toDo.assignment!!.getAssignmentIcon()))
        toDoIcon.setColorFilter(color)

        // String to track if the assignment is closed. If it isn't, we'll prepend the due date string with an empty string and it will look the same
        // Otherwise, we want it to say "Closed" and the due date with a dot as a separator
        var closedString = ""
        if (toDo.assignment!!.lockDate?.before(Date()) == true) {
            closedString = context.getString(R.string.cmp_closed) + context.getString(R.string.utils_dotWithSpaces)
        }

        if (toDo.assignment!!.allDates.size > 1) {
            // We have multiple due dates
            dueDate.text = closedString + context.getString(R.string.multiple_due_dates)
        } else {
            if (toDo.assignment!!.dueAt != null) {
                dueDate.text = closedString + context.getString(
                    R.string.due,
                    DateHelper.getMonthDayAtTime(context, toDo.assignment!!.dueDate, context.getString(R.string.at))
                )
            } else {
                dueDate.text = closedString + context.getString(R.string.no_due_date)
            }
        }

        if (toDo.assignment!!.needsGradingCount == 0L) {
            ungradedCount.setGone().text = ""
        } else {
            ungradedCount.setVisible().text = context.resources.getQuantityString(
                R.plurals.needsGradingCount,
                toDo.assignment?.needsGradingCount?.toInt() ?: -1,
                NumberHelper.formatInt(toDo.assignment?.needsGradingCount)
            )
            ungradedCount.isAllCaps = true
        }

        // Set the content description on the container so we can tell the user that it is published as the last piece of information. When a content description is on a container
        toDoLayout.contentDescription = toDo.title + " " + dueDate.text + " " + ungradedCount.text + " " + if(toDo.assignment?.published == true) context.getString(R.string.published) else context.getString(R.string.not_published)
    }
}
