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
package com.instructure.pandautils.features.assignments.details

import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.pandautils.databinding.FragmentAssignmentDetailsBinding
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import java.io.File

abstract class AssignmentDetailsBehaviour {
    @get:ColorInt
    abstract val dialogColor: Int

    open fun showMediaDialog(activity: FragmentActivity, binding: FragmentAssignmentDetailsBinding?, recordCallback: (File?) -> Unit, startVideoCapture: () -> Unit, onLaunchMediaPicker: () -> Unit) = Unit

    open fun showSubmitDialog(activity: FragmentActivity, binding: FragmentAssignmentDetailsBinding?, recordCallback: (File?) -> Unit, startVideoCapture: () -> Unit, onLaunchMediaPicker: () -> Unit, assignment: Assignment, course: Course, isStudioEnabled: Boolean, studioLTITool: LTITool?) = Unit

    open fun applyTheme(activity: FragmentActivity,
                        binding: FragmentAssignmentDetailsBinding?,
                        bookmark: Bookmarker,
                        course: Course?,
                        toolbar: Toolbar) = Unit

    open fun setupAppSpecificViews(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        course: Course,
        assignment: Assignment?,
        routeToCompose: ((InboxComposeOptions) -> Unit)?
    ) = Unit

    open fun onOptionsItemSelected(activity: FragmentActivity, item: MenuItem): Boolean = false

    abstract fun getThemeColor(course: Course): Int
}