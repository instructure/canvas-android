/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.assignment.details

import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.pandautils.databinding.FragmentAssignmentDetailsBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import javax.inject.Inject

class ParentAssignmentDetailsBehaviour @Inject constructor(
    private val parentPrefs: ParentPrefs
): AssignmentDetailsBehaviour() {
    @ColorInt override val dialogColor: Int = parentPrefs.currentStudent.studentColor

    override fun applyTheme(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        bookmark: Bookmarker,
        toolbar: Toolbar,
        course: Course?
    ) {
        ViewStyler.themeToolbarColored(activity, toolbar, parentPrefs.currentStudent.studentColor, activity.getColor(R.color.textLightest))
        ViewStyler.setStatusBarDark(activity, parentPrefs.currentStudent.studentColor)
    }
}