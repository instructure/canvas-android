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
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterCourseBrowserBinding
import com.instructure.teacher.utils.TeacherPrefs

class CourseBrowserViewHolder(private val binding: AdapterCourseBrowserBinding, val iconTint: Int) : RecyclerView.ViewHolder(binding.root) {

    // For instrumentation testing
    lateinit var labelText: TextView

    fun bind(tab: Tab, clickedCallback: (Tab) -> Unit) {
        val res: Int = when (tab.tabId) {
            Tab.ASSIGNMENTS_ID -> R.drawable.ic_assignment
            Tab.QUIZZES_ID -> R.drawable.ic_quiz
            Tab.DISCUSSIONS_ID -> R.drawable.ic_discussion
            Tab.ANNOUNCEMENTS_ID -> R.drawable.ic_announcement
            Tab.PEOPLE_ID -> R.drawable.ic_people
            Tab.FILES_ID -> R.drawable.ic_files
            Tab.PAGES_ID -> R.drawable.ic_pages
            Tab.MODULES_ID -> R.drawable.ic_modules
            Tab.SYLLABUS_ID -> R.drawable.ic_syllabus
            Tab.STUDENT_VIEW -> R.drawable.ic_user
            else -> {
                //Determine if its the attendance tool
                val attendanceExternalToolId = TeacherPrefs.attendanceExternalToolId
                if (attendanceExternalToolId.isNotBlank() && attendanceExternalToolId == tab.tabId) {
                    R.drawable.ic_attendance
                } else if (tab.type == Tab.TYPE_EXTERNAL) {
                    R.drawable.ic_lti
                } else R.drawable.ic_canvas_logo
            }
        }

        var d = VectorDrawableCompat.create(itemView.context.resources, res, null)
        d = DrawableCompat.wrap(d!!) as VectorDrawableCompat?
        DrawableCompat.setTint(d!!, iconTint)

        setupTab(tab, d, clickedCallback, binding)
    }

    /**
     * Fill in the view with tabby goodness
     *
     * @param tab The tab (Assignment, Discussions, etc)
     * @param res The image resource for the tab
     * @param callback What we do when the user clicks this tab
     */
    private fun setupTab(tab: Tab, drawable: Drawable, callback: (Tab) -> Unit, binding: AdapterCourseBrowserBinding) = with(binding) {
        labelText = label

        // Manually set the text for Student View tab since it doesn't have any other info other than tabId
        if (tab.tabId == Tab.STUDENT_VIEW) {
            label.text = itemView.context.getText(R.string.tab_student_view)
            endIcon.setImageDrawable(itemView.context.getDrawable(R.drawable.ic_open_externally))
            description.setVisible()
            description.text = itemView.context.getText(R.string.opensInCanvasApp)
        } else {
            label.text = tab.label
            endIcon.setImageDrawable(null)
            description.setGone()
        }

        icon.setImageDrawable(drawable)
        root.setOnClickListener {
            callback(tab)
        }
    }
}

