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

package com.instructure.pandautils.data.repository.announcement

import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class AnnouncementNetworkDataSource(
    private val announcementApi: AnnouncementAPI.AnnouncementInterface
) : AnnouncementDataSource {

    override suspend fun getCourseAnnouncements(courseId: Long, forceRefresh: Boolean): DataResult<List<DiscussionTopicHeader>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return announcementApi.getFirstPageAnnouncementsList(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            params
        ).depaginate { nextUrl ->
            announcementApi.getNextPageAnnouncementsList(nextUrl, params)
        }
    }
}