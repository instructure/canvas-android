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

import android.content.res.ColorStateList
import android.view.Gravity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterCoursesBinding
import com.instructure.teacher.fragments.DashboardFragment
import com.instructure.teacher.utils.Const.COURSE_EDIT_COLOR_ID
import com.instructure.teacher.utils.Const.COURSE_EDIT_NAME_ID
import com.instructure.teacher.utils.TeacherPrefs

class CoursesViewHolder(private val binding: AdapterCoursesBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(course: Course, callback: DashboardFragment.CourseBrowserCallback?) = with(binding) {
        titleTextView.text = course.name
        courseCode.text = course.courseCode

        titleTextView.setTextColor(course.textAndIconColor)

        courseImageView.setCourseImage(course, course.backgroundColor, !TeacherPrefs.hideCourseColorOverlay)

        courseColorIndicator.backgroundTintList = ColorStateList.valueOf(course.backgroundColor)
        courseColorIndicator.setVisible(TeacherPrefs.hideCourseColorOverlay)

        cardView.setOnClickListener { callback?.onShowCourseDetails(course) }

        overflow.onClickWithRequireNetwork {
            val popup = PopupMenu(it.context, it, Gravity.START.and(Gravity.TOP),0, R.style.Widget_AppCompat_PopupMenu_Overflow)
            val menu = popup.menu

            // Add things to the popup menu
            menu.add(0, COURSE_EDIT_NAME_ID, COURSE_EDIT_NAME_ID, R.string.edit_nickname)
            menu.add(0, COURSE_EDIT_COLOR_ID, COURSE_EDIT_COLOR_ID, R.string.edit_course_color)

            // Add click listener
            popup.setOnMenuItemClickListener { item ->
                if(item.itemId == COURSE_EDIT_NAME_ID) {
                    callback?.onEditCourseNickname(course)
                } else if(item.itemId == COURSE_EDIT_COLOR_ID) {
                    callback?.onPickCourseColor(course)
                }
                true
            }

            popup.show()
        }
    }
}
