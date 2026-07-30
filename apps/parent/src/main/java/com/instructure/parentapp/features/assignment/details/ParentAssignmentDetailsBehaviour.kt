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

import android.content.Context
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour
import com.instructure.pandautils.features.assignments.details.MessageFabConfig
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class ParentAssignmentDetailsBehaviour @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parentPrefs: ParentPrefs,
    private val apiPrefs: ApiPrefs,
) : AssignmentDetailsBehaviour() {
    @ColorInt
    override val dialogColor: Int = parentPrefs.currentStudent.studentColor

    override fun applyStatusBarTheme(activity: FragmentActivity, course: Course?) {
        ViewStyler.setStatusBarDark(activity, parentPrefs.currentStudent.studentColor)
    }

    override fun getMessageFabConfig(course: Course?, assignment: Assignment?): MessageFabConfig {
        return MessageFabConfig(
            iconRes = R.drawable.ic_chat,
            contentDescription = context.getString(R.string.sendMessageAboutAssignment),
            options = getInboxComposeOptions(context, course, assignment)
        )
    }

    private fun getInboxComposeOptions(context: Context, course: Course?, assignment: Assignment?): InboxComposeOptions {
        val courseContextId = course?.contextId.orEmpty()
        var options = InboxComposeOptions.buildNewMessage()
        options = options.copy(
            defaultValues = options.defaultValues.copy(
                contextCode = courseContextId,
                contextName = course?.name.orEmpty(),
                subject = context.getString(
                    R.string.regardingHiddenMessageWithAssignmentPrefix,
                    parentPrefs.currentStudent?.name.orEmpty(),
                    assignment?.name.orEmpty()
                )
            ),
            disabledFields = options.disabledFields.copy(
                isContextDisabled = true
            ),
            autoSelectRecipientsFromRoles = listOf(EnrollmentType.TeacherEnrollment),
            hiddenBodyMessage = context.getString(
                R.string.regardingHiddenMessage,
                parentPrefs.currentStudent?.name.orEmpty(),
                getContextURL(course?.id.orDefault(), assignment?.id.orDefault())
            )
        )

        return options
    }

    private fun getContextURL(courseId: Long, assignmentId: Long): String {
        return "${apiPrefs.fullDomain}/courses/$courseId/assignments/$assignmentId"
    }

    override fun getThemeColor(course: Course): Int {
        return parentPrefs.currentStudent.studentColor
    }
}
