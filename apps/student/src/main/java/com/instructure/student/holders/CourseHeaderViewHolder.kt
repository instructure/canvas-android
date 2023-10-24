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

package com.instructure.student.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.onClick
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderCourseHeaderBinding
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback

class CourseHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_course_header
    }

    fun bind(callback: CourseAdapterToFragmentCallback) = with(ViewholderCourseHeaderBinding.bind(itemView)) {
        editDashboardTextView.setTextColor(ThemePrefs.textButtonColor)
        editDashboardTextView.onClick { callback.onSeeAllCourses() }
    }

}
