/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.domain.usecase.announcements

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class LoadCourseAnnouncementsParams(val courseId: Long, val forceNetwork: Boolean = false)

class LoadCourseAnnouncementsUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) : BaseUseCase<LoadCourseAnnouncementsParams, List<DiscussionTopicHeader>>() {

    override suspend fun execute(params: LoadCourseAnnouncementsParams): List<DiscussionTopicHeader> {
        val announcements = courseRepository.getCourseAnnouncements(
            courseId = params.courseId,
            forceRefresh = params.forceNetwork
        ).dataOrThrow

        // Filter for unread announcements only
        return announcements.filter { announcement ->
            announcement.unreadCount > 0 || announcement.readState == DiscussionTopicHeader.ReadState.UNREAD.name
        }
    }
}