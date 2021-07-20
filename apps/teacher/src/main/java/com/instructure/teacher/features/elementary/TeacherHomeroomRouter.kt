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
package com.instructure.teacher.features.elementary

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter

class TeacherHomeroomRouter : HomeroomRouter {
    override fun canRouteInternally(url: String): Boolean = false

    override fun routeInternally(url: String) = Unit

    override fun openMedia(url: String) = Unit

    override fun openAnnouncements(canvasContext: CanvasContext) = Unit

    override fun openCourse(course: Course) = Unit

    override fun openAssignments(course: Course) = Unit

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) = Unit

    override fun openCalendarEvent(canvasContext: CanvasContext, scheduleItemId: Long) = Unit

    override fun openAnnouncementDetails(course: Course, announcement: DiscussionTopicHeader) = Unit

    override fun updateColors() = Unit

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) = Unit

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long, discussionTitle: String) = Unit
}