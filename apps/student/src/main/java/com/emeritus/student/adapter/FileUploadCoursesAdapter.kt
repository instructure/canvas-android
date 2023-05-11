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
package com.emeritus.student.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.backgroundColor
import com.emeritus.student.R

class FileUploadCoursesAdapter(
    context: Context,
    private val inflater: LayoutInflater,
    private var courses: List<Course>
) : ArrayAdapter<Course>(context, R.layout.canvas_context_spinner_adapter_item, courses) {
    enum class Type { TEACHER, STUDENT, NONE }

    fun setCourses(courses: List<Course>) {
        this.courses = courses
        notifyDataSetChanged()
    }

    override fun getCount(): Int = courses.size

    @SuppressLint("InflateParams")
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return bindView(courses[position], convertView, false)
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return bindView(courses[position], convertView, true)
    }

    @SuppressLint("InflateParams")
    private fun bindView(item: Course, convertView: View?, bold: Boolean): View {
        val viewHolder: CourseViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.canvas_context_spinner_adapter_item, null)
            viewHolder = CourseViewHolder(view.findViewById(R.id.title), view.findViewById(R.id.icon))
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as CourseViewHolder
        }

        viewHolder.title.text = item.name
        if (!bold) viewHolder.title.setTypeface(null, Typeface.NORMAL)
        viewHolder.indicator.visibility = View.VISIBLE
        viewHolder.indicator.background = ShapeDrawable(OvalShape()).apply { paint.color = item.backgroundColor }

        return view
    }

    private data class CourseViewHolder (val title: TextView, val indicator: ImageView )

    companion object {
        fun getFilteredCourseList(courses: List<Course>, filterType: Type): List<Course> {
            return courses.filter {
                when (filterType) {
                    Type.STUDENT -> it.isStudent
                    Type.TEACHER -> it.isTeacher
                    else -> false
                }
            }
        }
    }

}
