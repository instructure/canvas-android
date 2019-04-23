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

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.instructure.canvasapi2.models.Tab
import com.instructure.teacher.R
import com.instructure.teacher.utils.TeacherPrefs
import kotlinx.android.synthetic.main.adapter_course_browser.view.*

class CourseBrowserViewHolder(view: View, val color: Int) : RecyclerView.ViewHolder(view) {

    // For instrumentation testing
    lateinit var labelText: TextView

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_course_browser
    }

    fun bind(tab: Tab, clickedCallback: (Tab) -> Unit) {
        val res: Int = when (tab.tabId) {
            Tab.ASSIGNMENTS_ID -> R.drawable.vd_assignment
            Tab.QUIZZES_ID -> R.drawable.vd_quiz
            Tab.DISCUSSIONS_ID -> R.drawable.vd_discussion
            Tab.ANNOUNCEMENTS_ID -> R.drawable.vd_announcement
            Tab.PEOPLE_ID -> R.drawable.vd_people
            Tab.FILES_ID -> R.drawable.vd_files
            Tab.PAGES_ID -> R.drawable.vd_pages
            Tab.MODULES_ID -> R.drawable.vd_modules
            else -> {
                //Determine if its the attendance tool
                val attendanceExternalToolId = TeacherPrefs.attendanceExternalToolId
                if(attendanceExternalToolId.isNotBlank() && attendanceExternalToolId == tab.tabId) {
                    R.drawable.vd_attendance
                } else if(tab.type == Tab.TYPE_EXTERNAL) {
                    R.drawable.vd_lti
                } else R.drawable.vd_canvas_logo
            }
        }

        var d = VectorDrawableCompat.create(itemView.context.resources, res, null)
        d = DrawableCompat.wrap(d!!) as VectorDrawableCompat?
        DrawableCompat.setTint(d!!, color)

        setupTab(tab, d, clickedCallback)
    }

    /**
     * Fill in the view with tabby goodness
     *
     * @param tab The tab (Assignment, Discussions, etc)
     * @param res The image resource for the tab
     * @param callback What we do when the user clicks this tab
     */
    private fun setupTab(tab: Tab, drawable: Drawable, callback: (Tab) -> Unit) {
        labelText = itemView.label
        itemView.label.text = tab.label
        itemView.icon.setImageDrawable(drawable)
        itemView.setOnClickListener {
            callback(tab)
        }
    }
}

