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

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.instructure.student.R
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.textAndIconColor
import kotlinx.android.synthetic.main.viewholder_group_card.view.*

class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_group_card
    }

    fun bind(group: Group, courseMap: Map<Long, Course>, callback: CourseAdapterToFragmentCallback) = with(itemView) {
        accentBar.setBackgroundColor(group.textAndIconColor)
        groupCourseView.setTextColor(group.textAndIconColor)
        groupNameView.text = group.name
        courseMap[group.courseId]?.let {
            groupCourseView.text = it.name
            termView.text = it.term?.name ?: it.courseCode ?: ""
        } ?: run {
            groupCourseView.text = context.getString(R.string.accountGroup)
            termView.text = ""
        }
        onClick { callback.onGroupSelected(group) }
    }

}
