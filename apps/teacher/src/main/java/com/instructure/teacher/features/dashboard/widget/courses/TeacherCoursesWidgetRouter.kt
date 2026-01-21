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

package com.instructure.teacher.features.dashboard.widget.courses

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Recipient
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.dashboard.edit.EditDashboardFragment
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import com.instructure.pandautils.features.dashboard.widget.courses.customize.CustomizeCourseFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode
import com.instructure.teacher.fragments.AnnouncementListFragment
import com.instructure.teacher.fragments.CourseBrowserFragment
import com.instructure.teacher.router.RouteMatcher

class TeacherCoursesWidgetRouter : CoursesWidgetRouter {

    override fun routeToCourse(activity: FragmentActivity, course: Course) {
        RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(course))
    }

    override fun routeToGroup(activity: FragmentActivity, group: Group) {
        RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(group))
    }

    override fun routeToManageOfflineContent(activity: FragmentActivity, course: Course) {
        // TODO: Navigate to manage offline content screen
    }

    override fun routeToCustomizeCourse(activity: FragmentActivity, course: Course) {
        // TODO: Navigate to customize course screen (color/nickname)
    }

    override fun routeToAllCourses(activity: FragmentActivity) {
        RouteMatcher.route(activity, EditDashboardFragment.makeRoute())
    }

    override fun routeToAnnouncement(activity: FragmentActivity, course: Course, announcement: DiscussionTopicHeader) {
        // TODO: Navigate to announcement details
    }

    override fun routeToAnnouncementList(activity: FragmentActivity, course: Course) {
        // TODO: Navigate to announcement list
    }

    override fun routeToGroupMessage(activity: FragmentActivity, group: Group) {
        // TODO: Navigate to group message compose
    }
}
