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

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions

abstract class AssignmentDetailsBehaviour {
    @get:ColorInt
    abstract val dialogColor: Int

    abstract fun getThemeColor(course: Course): Int

    open fun applyStatusBarTheme(activity: FragmentActivity, course: Course?) = Unit

    open fun showMediaDialog(
        activity: FragmentActivity,
        onLaunchAudioRecording: () -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit
    ) = Unit

    open fun showSubmitDialog(
        activity: FragmentActivity,
        onLaunchAudioRecording: () -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit,
        assignment: Assignment,
        course: Course,
        isStudioEnabled: Boolean,
        studioLTITool: LTITool?
    ) = Unit

    /**
     * App-specific configuration for the message floating action button (e.g. the Parent app's
     * "send message about this assignment" FAB). Returns null when the app has no message action
     * (e.g. Student), in which case no FAB is shown.
     */
    open fun getMessageFabConfig(course: Course?, assignment: Assignment?): MessageFabConfig? = null
}

data class MessageFabConfig(
    @DrawableRes val iconRes: Int,
    val contentDescription: String,
    val options: InboxComposeOptions
)
