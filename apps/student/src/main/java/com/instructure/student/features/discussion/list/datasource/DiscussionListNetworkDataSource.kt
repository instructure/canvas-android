/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.discussion.list.datasource

import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.depaginate

class DiscussionListNetworkDataSource(
    private val courseApi: CourseAPI.CoursesInterface,
    private val groupApi: GroupAPI.GroupInterface,
    private val discussionApi: DiscussionAPI.DiscussionInterface,
    private val announcementApi: AnnouncementAPI.AnnouncementInterface
) : DiscussionListDataSource {
    override suspend fun getPermissionsForCourse(course: Course): CanvasContextPermission? {
        val params = RestParams(isForceReadFromNetwork = true)
        return courseApi.getCourse(course.id, params).dataOrNull?.permissions
    }

    override suspend fun getPermissionsForGroup(group: Group): CanvasContextPermission? {
        val params = RestParams(isForceReadFromNetwork = true)
        return groupApi.getDetailedGroup(group.id, params).dataOrNull?.permissions
    }

    override suspend fun getDiscussions(canvasContext: CanvasContext, forceNetwork: Boolean): List<DiscussionTopicHeader> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return discussionApi.getFirstPageDiscussionTopicHeaders(canvasContext.apiContext(), canvasContext.id, params).depaginate { nextPage ->
            discussionApi.getNextPage(nextPage, params)
        }.dataOrThrow
    }

    override suspend fun getAnnouncements(canvasContext: CanvasContext, forceNetwork: Boolean): List<DiscussionTopicHeader> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return announcementApi.getFirstPageAnnouncementsList(canvasContext.apiContext(), canvasContext.id, params).depaginate { nextPage ->
            announcementApi.getNextPageAnnouncementsList(nextPage, params)
        }.dataOrThrow
    }
}