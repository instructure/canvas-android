/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.mobius.elementary.resources

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesRouter
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.student.router.RouteMatcher

class StudentResourcesRouter(private val activity: FragmentActivity) : ResourcesRouter {

    override fun openLti(ltiTool: LTITool) {
        val course = Course(id = ltiTool.contextId ?: 0, name = ltiTool.contextName ?: "")
        val route = LtiLaunchFragment.makeRoute(
            course,
            ltiTool.url ?: ltiTool.courseNavigation?.url ?: "",
            ltiTool.courseNavigation?.text ?: ltiTool.name ?: "",
            sessionLessLaunch = true,
            assignmentLti = false,
            ltiTool = ltiTool)
        RouteMatcher.route(activity, route)
    }

    override fun openComposeMessage(user: User) {
        val recipient = Recipient.from(user)
        val context = Course(id = user.enrollments[0].courseId, homeroomCourse = true)
        val options = InboxComposeOptions.buildNewMessage().copy(
            defaultValues = InboxComposeOptionsDefaultValues(
                contextName = context.name,
                contextCode = context.contextId,
                recipients = arrayListOf(recipient)
            )

        )
        val route = InboxComposeFragment.makeRoute(options)
        RouteMatcher.route(activity, route)
    }
}