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
import android.text.SpannableStringBuilder
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterAssignmentDueDateBinding
import com.instructure.teacher.models.DueDateGroup
import java.util.*


class DueDateViewHolder(private val binding: AdapterAssignmentDueDateBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        date: DueDateGroup,
        allDatesCount: Int,
        sections: Map<Long, Section>,
        groups: Map<Long, Group>,
        students: Map<Long, User>
    ) = with(binding) {
        val context = binding.root.context
        val atSeparator = context.getString(R.string.at)
        val noDateFiller = context.getString(R.string.no_date_filler)

        val assignees = arrayListOf<CharSequence>()

        if (date.isEveryone) {
            assignees += context.getString(
                    if (allDatesCount > 1 || date.hasOverrideAssignees) R.string.everyone_else else R.string.everyone
            )
        }
        assignees += date.sectionIds.map { sections[it]?.name!! }
        assignees += date.groupIds.map { groups[it]?.name!! }
        assignees += date.studentIds.map {
            students[it]?.let { user -> Pronouns.span(user.name, user.pronouns) }
                ?: context.getString(R.string.unknown_student)
        }
        dueForTextView.text = assignees.joinTo(SpannableStringBuilder())

        dueDateTextView.text = if (date.coreDates.dueDate == null) context.getString(R.string.no_due_date) else getFormattedDueDate(context, date.coreDates.dueDate)
        availableFromTextView.text = if (date.coreDates.unlockDate != null) DateHelper.getMonthDayAtTime(context, date.coreDates.unlockDate, atSeparator) else noDateFiller
        availableToTextView.text = if (date.coreDates.lockDate != null) DateHelper.getMonthDayAtTime(context, date.coreDates.lockDate, atSeparator) else noDateFiller
    }

    private fun getFormattedDueDate(context: Context, date: Date?): String {
        val dueDate = DateHelper.dayMonthDateFormatUniversal.format(date)
        val dueTime = DateHelper.getPreferredTimeFormat(context).format(date)
        return context.getString(R.string.due_date_at_time).format(dueDate, dueTime)
    }
}
