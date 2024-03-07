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
package com.instructure.pandautils.features.elementary.homeroom

import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.ThemedColor

data class HomeroomViewData(val greetingMessage: String, val announcements: List<ItemViewModel>, val courseCards: List<ItemViewModel>) {
    fun isEmpty() = announcements.isEmpty() && courseCards.isEmpty()
}

data class AnnouncementViewData(val courseName: String, val title: String, val htmlContent: String)

data class CourseCardViewData(
    val courseName: String,
    val assignmentsDueText: String,
    val assignmentsMissingText: String,
    val announcementText: String,
    val color: ThemedColor,
    val imageUrl: String)

sealed class HomeroomAction {
    data class OpenAnnouncements(val canvasContext: CanvasContext) : HomeroomAction()
    data class LtiButtonPressed(val url: String) : HomeroomAction()
    object ShowRefreshError : HomeroomAction()
    object AnnouncementViewsReady : HomeroomAction()
    data class OpenAnnouncementDetails(val course: Course, val announcement: DiscussionTopicHeader) : HomeroomAction()
    data class OpenCourse(val course: Course) : HomeroomAction()
    data class OpenAssignments(val course: Course) : HomeroomAction()
}