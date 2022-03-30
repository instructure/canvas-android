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
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.utils.Const.COURSE_EDIT_COLOR_ID
import com.instructure.teacher.utils.Const.COURSE_EDIT_NAME_ID
import com.instructure.teacher.utils.TeacherPrefs
import kotlinx.android.synthetic.main.adapter_courses.view.*


class CoursesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_courses
    }

    fun bind(course: Course, callback: com.instructure.teacher.fragments.AllCoursesFragment.CourseBrowserCallback?) = with(itemView) {
        titleTextView.text = course.name
        courseCode.text = course.courseCode

        val courseColor = ColorKeeper.getOrGenerateColor(course)
        titleTextView.setTextColor(courseColor)

        courseImageView.setCourseImage(course, courseColor, !TeacherPrefs.hideCourseColorOverlay)

        courseColorIndicator.backgroundTintList = ColorStateList.valueOf(course.color)
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
