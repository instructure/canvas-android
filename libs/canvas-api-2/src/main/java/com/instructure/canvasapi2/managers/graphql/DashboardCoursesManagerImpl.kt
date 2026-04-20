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

package com.instructure.canvasapi2.managers.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.CourseAnnouncementsQuery
import com.instructure.canvasapi2.DashboardCoursesQuery
import com.instructure.canvasapi2.DashboardSingleCourseQuery
import com.instructure.canvasapi2.enqueueQuery

class DashboardCoursesManagerImpl(
    private val apolloClient: ApolloClient
) : DashboardCoursesManager {

    override suspend fun getDashboardCourses(
        forceNetwork: Boolean
    ): DashboardCoursesQuery.Data {
        return apolloClient.enqueueQuery(DashboardCoursesQuery(), forceNetwork = forceNetwork).dataAssertNoErrors
    }

    override suspend fun getSingleCourse(
        courseId: Long,
        forceNetwork: Boolean
    ): DashboardSingleCourseQuery.Data {
        val query = DashboardSingleCourseQuery(courseId = courseId.toString())
        return apolloClient.enqueueQuery(query, forceNetwork = forceNetwork).dataAssertNoErrors
    }

    override suspend fun getCourseAnnouncements(
        courseId: Long,
        forceNetwork: Boolean
    ): List<CourseAnnouncementsQuery.Node1?> {
        val allNodes = mutableListOf<CourseAnnouncementsQuery.Node1?>()
        var hasNextPage = true
        var cursor: String? = null

        while (hasNextPage) {
            val query = CourseAnnouncementsQuery(
                courseId = courseId.toString(),
                cursor = if (cursor != null) Optional.present(cursor) else Optional.absent()
            )
            val data = apolloClient.enqueueQuery(query, forceNetwork = forceNetwork).dataAssertNoErrors
            val announcements = (data.course as? CourseAnnouncementsQuery.CourseOnCourse)?.announcements

            announcements?.nodes?.let { allNodes.addAll(it) }

            hasNextPage = announcements?.pageInfo?.hasNextPage == true
            cursor = announcements?.pageInfo?.endCursor
        }

        return allNodes
    }
}