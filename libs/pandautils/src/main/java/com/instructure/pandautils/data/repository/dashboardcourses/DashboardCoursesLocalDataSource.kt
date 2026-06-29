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

package com.instructure.pandautils.data.repository.dashboardcourses

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade

class DashboardCoursesLocalDataSource(
    private val courseFacade: CourseFacade,
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade
) : DashboardCoursesDataSource {

    override suspend fun getCourses(forceRefresh: Boolean): DashboardCoursesData {
        val courses = courseFacade.getAllCourses()
        val announcementsMap = courses.associate { course ->
            course.id to discussionTopicHeaderFacade.getAnnouncementsForCourse(course.id)
                .filter { it.status == DiscussionTopicHeader.ReadState.UNREAD || it.unreadCount > 0 }
        }
        return DashboardCoursesData(courses = courses, announcementsMap = announcementsMap)
    }
}
