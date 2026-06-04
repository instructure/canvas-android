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
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.enqueueQuery

class DashboardCoursesManagerImpl(
    private val apolloClient: ApolloClient
) : DashboardCoursesManager {

    override suspend fun getDashboardCourses(
        forceNetwork: Boolean
    ): DashboardCoursesQuery.Data {
        val query = DashboardCoursesQuery(pageSize = QLClientConfig.GRAPHQL_PAGE_SIZE)
        return apolloClient.enqueueQuery(query, forceNetwork = forceNetwork).dataAssertNoErrors
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
        cursor: String?,
        forceNetwork: Boolean
    ): CourseAnnouncementsQuery.Data {
        val pageSize = QLClientConfig.GRAPHQL_PAGE_SIZE
        val initialCursor = if (cursor != null) Optional.present(cursor) else Optional.absent()
        val query = CourseAnnouncementsQuery(
            courseId = courseId.toString(),
            pageSize = pageSize,
            cursor = initialCursor
        )
        val initialData = apolloClient.enqueueQuery(query, forceNetwork = forceNetwork).dataAssertNoErrors

        val course = initialData.course?.onCourse ?: return initialData
        val allNodes = course.announcements?.nodes?.toMutableList() ?: mutableListOf()
        var hasNextPage = course.announcements?.pageInfo?.hasNextPage == true
        var nextCursor = course.announcements?.pageInfo?.endCursor

        while (hasNextPage) {
            val paginatedQuery = CourseAnnouncementsQuery(
                courseId = courseId.toString(),
                pageSize = pageSize,
                cursor = Optional.present(nextCursor)
            )
            val paginatedData = apolloClient.enqueueQuery(paginatedQuery, forceNetwork = forceNetwork).dataAssertNoErrors
            val paginatedCourse = paginatedData.course?.onCourse

            paginatedCourse?.announcements?.nodes?.let { allNodes.addAll(it) }
            hasNextPage = paginatedCourse?.announcements?.pageInfo?.hasNextPage == true
            nextCursor = paginatedCourse?.announcements?.pageInfo?.endCursor
        }

        return CourseAnnouncementsQuery.Data(
            course = CourseAnnouncementsQuery.Course(
                __typename = "Course",
                onCourse = course.copy(
                    announcements = course.announcements?.copy(nodes = allNodes)
                )
            )
        )
    }
}