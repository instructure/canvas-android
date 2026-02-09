/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.dashboard.widget.courses

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandares.R
import com.instructure.pandautils.features.dashboard.edit.EditDashboardFragment
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import com.instructure.pandautils.features.dashboard.widget.courses.customize.CustomizeCourseFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode
import com.instructure.pandautils.features.offline.offlinecontent.OfflineContentFragment
import com.instructure.student.features.coursebrowser.CourseBrowserFragment
import com.instructure.student.fragment.AnnouncementListFragment
import com.instructure.student.router.RouteMatcher

class StudentCoursesWidgetRouter : CoursesWidgetRouter {

    override fun routeToCourse(activity: FragmentActivity, course: Course) {
        RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(course))
    }

    override fun routeToGroup(activity: FragmentActivity, group: Group) {
        RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(group))
    }

    override fun routeToManageOfflineContent(activity: FragmentActivity, course: Course) {
        RouteMatcher.route(activity, OfflineContentFragment.makeRoute(course))
    }

    override fun routeToCustomizeCourse(activity: FragmentActivity, course: Course) {
        RouteMatcher.route(activity, CustomizeCourseFragment.makeRoute(course))
    }

    override fun routeToAllCourses(activity: FragmentActivity) {
        RouteMatcher.route(activity, EditDashboardFragment.makeRoute())
    }

    override fun routeToAnnouncement(activity: FragmentActivity, course: Course, announcement: DiscussionTopicHeader) {
        RouteMatcher.route(activity, DiscussionRouterFragment.makeRoute(course, announcement, isAnnouncement = true))
    }

    override fun routeToAnnouncementList(activity: FragmentActivity, course: Course) {
        RouteMatcher.route(activity, AnnouncementListFragment.makeRoute(course))
    }

    override fun routeToGroupMessage(activity: FragmentActivity, group: Group) {
        val allInGroupRecipient = Recipient(
            stringId = group.contextId,
            name = activity.getString(R.string.all_recipients_in_selected_context, group.name),
            userCount = group.users.size
        )

        val options = InboxComposeOptions(
            mode = InboxComposeOptionsMode.NEW_MESSAGE,
            defaultValues = InboxComposeOptionsDefaultValues(
                contextCode = group.contextId,
                contextName = group.name,
                recipients = listOf(allInGroupRecipient)
            )
        )
        RouteMatcher.route(activity, InboxComposeFragment.makeRoute(options))
    }
}