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

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.navigation.Navigation

class ParentAssignmentDetailsRouter(
    private val navigation: Navigation,
    private val parentPrefs: ParentPrefs,
    private val apiPrefs: ApiPrefs,
    private val analytics: Analytics
) : AssignmentDetailsRouter() {
    override fun navigateToSendMessage(activity: FragmentActivity, options: InboxComposeOptions) {
        val route = navigation.inboxComposeRoute(options)
        navigation.navigate(activity, route)
    }

    override fun navigateToSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentUrl: String?,
        isAssignmentEnhancementEnabled: Boolean,
        isObserver: Boolean,
        initialSelectedSubmissionAttempt: Long?
    ) {
        assignmentUrl ?: return
        val parentId = apiPrefs.user?.id ?: return
        val currentStudentId = parentPrefs.currentStudent?.id ?: return
        val cookies = mapOf("k5_observed_user_for_$parentId" to "$currentStudentId")
        val url = if (isAssignmentEnhancementEnabled) {
            assignmentUrl
        } else {
            "$assignmentUrl/submissions/$currentStudentId"
        }.plus("?embedded=true")

        analytics.logEvent(AnalyticsEventConstants.SUBMISSION_AND_RUBRIC_INTERACTION)

        navigation.navigate(
            activity, navigation.internalWebViewRoute(
                url = url,
                title = activity.getString(R.string.submission),
                initialCookies = cookies
            )
        )
    }
}
