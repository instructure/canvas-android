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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Attendance
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.asStateList
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterAttendanceBinding
import com.instructure.teacher.interfaces.AttendanceToFragmentCallback

class AttendanceViewHolder(private val binding: AdapterAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        attendance: Attendance,
        callback: AttendanceToFragmentCallback<Attendance>,
        position: Int,
        context: Context
    ) = with(binding) {
        // Set student avatar
        val basicUser = BasicUser()
        basicUser.name = attendance.student?.name
        basicUser.pronouns = attendance.student?.pronouns
        basicUser.avatarUrl = attendance.student?.avatarUrl
        ProfileUtils.loadAvatarForUser(studentAvatar, basicUser.name, basicUser.avatarUrl)

        // Set student name
        userName.text = attendance.student?.let { Pronouns.span(it.name, it.pronouns) }

        itemView.onClickWithRequireNetwork { callback.onRowClicked(attendance, position) }
        studentAvatar.onClick { callback.onAvatarClicked(attendance, position) }

        val (drawable: Int, color: Int) = when(attendance.attendanceStatus()) {
            Attendance.Attendance.ABSENT -> R.drawable.ic_attendance_missing to R.color.textDanger
            Attendance.Attendance.LATE -> R.drawable.ic_clock to R.color.textWarning
            Attendance.Attendance.PRESENT -> R.drawable.ic_complete to R.color.textSuccess
            else -> R.drawable.ic_no to R.color.textDark
        }
        attendanceIndicator.setImageResource(drawable)
        attendanceIndicator.imageTintList = ContextCompat.getColor(context, color).asStateList()
    }
}
