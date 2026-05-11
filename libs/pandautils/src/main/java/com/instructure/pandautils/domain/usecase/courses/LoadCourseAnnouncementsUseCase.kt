/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class LoadCourseAnnouncementsUseCase @Inject constructor(
    private val dashboardCoursesManager: DashboardCoursesManager
) : BaseUseCase<LoadCourseAnnouncementsUseCase.Params, List<DiscussionTopicHeader>>() {

    override suspend fun execute(params: Params): List<DiscussionTopicHeader> {
        val data = dashboardCoursesManager.getCourseAnnouncements(params.courseId, cursor = null, forceNetwork = params.forceNetwork)
        val nodes = data.course?.onCourse?.announcements?.nodes ?: return emptyList()
        return nodes.mapNotNull { node ->
            node ?: return@mapNotNull null
            val isUnread = node.participant?.read != true
            val hasUnreadEntries = (node.entryCounts?.unreadCount ?: 0) > 0
            if (!isUnread && !hasUnreadEntries) return@mapNotNull null
            DiscussionTopicHeader(
                id = node._id.toLongOrNull() ?: return@mapNotNull null,
                title = node.title,
                message = node.message,
                postedDate = node.postedAt,
                unreadCount = node.entryCounts?.unreadCount ?: 0,
                announcement = true
            )
        }
    }

    data class Params(val courseId: Long, val forceNetwork: Boolean = true)
}
