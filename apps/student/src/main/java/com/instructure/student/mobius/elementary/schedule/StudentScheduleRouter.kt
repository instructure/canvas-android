/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.student.mobius.elementary.schedule

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.features.elementary.schedule.ScheduleRouter
import com.instructure.student.BuildConfig
import com.instructure.student.features.elementary.course.ElementaryCourseFragment
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.CalendarEventFragment
import com.instructure.student.fragment.CourseBrowserFragment
import com.instructure.student.fragment.DiscussionDetailsFragment
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher

class StudentScheduleRouter(private val activity: FragmentActivity) : ScheduleRouter {

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        RouteMatcher.route(activity, AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId))
    }

    override fun openCalendarEvent(canvasContext: CanvasContext, scheduleItemId: Long) {
        RouteMatcher.route(activity, CalendarEventFragment.makeRoute(canvasContext, scheduleItemId))
    }

    override fun openAnnouncementDetails(course: Course, announcement: DiscussionTopicHeader) {
        RouteMatcher.route(activity, DiscussionDetailsFragment.makeRoute(course, announcement))
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        RouteMatcher.route(activity, BasicQuizViewFragment.makeRoute(canvasContext, htmlUrl))
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long, discussionTitle: String) {
        RouteMatcher.route(
            activity,
            DiscussionDetailsFragment.makeRoute(canvasContext, discussionId, title = discussionTitle)
        )
    }

    override fun openCourse(course: Course) {
        RouteMatcher.route(activity, ElementaryCourseFragment.makeRoute(course))
    }
}