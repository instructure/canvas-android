/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.parentapp.features.assignment.details

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.navigation.Navigation

class ParentAssignmentDetailsRouter(private val navigation: Navigation): AssignmentDetailsRouter() {
    override fun navigateToSendMessage(activity: FragmentActivity, assignment: Assignment, course: Course) {
        val route = navigation.inboxComposeRoute(getInboxComposeOptions(activity, assignment, course))
        navigation.navigate(activity, route)
    }

    private fun getInboxComposeOptions(context: Context, assignment: Assignment, course: Course): InboxComposeOptions {
        val courseContextId = course.contextId
        var options = InboxComposeOptions.buildNewMessage()
        options = options.copy(
            defaultValues = options.defaultValues.copy(
                contextCode = courseContextId,
                contextName = course.name,
                subject = context.getString(
                    R.string.regardingHiddenMessage,
                    ParentPrefs.currentStudent?.name.orEmpty(),
                    context.getString(R.string.messageAssignmentPrefix) + assignment.name.orEmpty()
                )
            ),
            disabledFields = options.disabledFields.copy(
                isContextDisabled = true
            ),
            autoSelectRecipientsFromRoles = listOf(EnrollmentType.TEACHERENROLLMENT),
            hiddenBodyMessage = context.getString(
                R.string.regardingHiddenMessage,
                ParentPrefs.currentStudent?.name.orEmpty(),
                getContextURL(course.id, assignment.id)
            )
        )

        return options
    }
    
    private fun getContextURL(courseId: Long, assignmentId: Long): String {
        return "${ApiPrefs.fullDomain}/courses/$courseId/assignments/$assignmentId"
    }
}