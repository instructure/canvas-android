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

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Attendance
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.teacher.R
import com.instructure.teacher.interfaces.AttendanceToFragmentCallback
import kotlinx.android.synthetic.main.adapter_attendance.view.*

class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(attendance: Attendance, callback: AttendanceToFragmentCallback<Attendance>, position: Int) = with(itemView){
        // Set student avatar
        val basicUser = BasicUser()
        basicUser.name = attendance.student?.name
        basicUser.pronouns = attendance.student?.pronouns
        basicUser.avatarUrl = attendance.student?.avatarUrl
        ProfileUtils.loadAvatarForUser(studentAvatar, basicUser)

        // Set student name
        userName.text = attendance.student?.let { Pronouns.span(it.name, it.pronouns) }

        itemView.onClickWithRequireNetwork { callback.onRowClicked(attendance, position) }
        studentAvatar.onClick { callback.onAvatarClicked(attendance, position) }

        when(attendance.attendanceStatus()) {
            Attendance.Attendance.ABSENT -> attendanceIndicator.setImageResource(R.drawable.ic_attendance_missing)
            Attendance.Attendance.LATE -> attendanceIndicator.setImageResource(R.drawable.ic_attendance_late)
            Attendance.Attendance.PRESENT -> attendanceIndicator.setImageResource(R.drawable.ic_attendance_present)
            else -> attendanceIndicator.setImageResource(R.drawable.ic_attendance_unmarked)
        }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_attendance
    }
}
