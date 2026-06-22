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
import com.apollographql.apollo.api.ApolloResponse
import com.instructure.canvasapi2.CourseAnnouncementsQuery
import com.instructure.canvasapi2.enqueueQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

private const val QL_CLIENT_CONFIG_CLASS = "com.instructure.canvasapi2.QLClientConfigKt"

class DashboardCoursesManagerImplTest {

    private val apolloClient: ApolloClient = mockk()
    private lateinit var manager: DashboardCoursesManagerImpl

    @Before
    fun setup() {
        mockkStatic(QL_CLIENT_CONFIG_CLASS)
        manager = DashboardCoursesManagerImpl(apolloClient)
    }

    @After
    fun tearDown() {
        unmockkStatic(QL_CLIENT_CONFIG_CLASS)
    }

    @Test
    fun `getCourseAnnouncements stops paging once the fetch cap is reached`() = runTest {
        // 10 pages of 20 are available (200 total), every page reports hasNextPage = true.
        val pages = (0 until 10).map { pageIndex ->
            announcementResponse(nodeCount = 20, startId = pageIndex * 20, hasNextPage = true)
        }
        coEvery { apolloClient.enqueueQuery(any<CourseAnnouncementsQuery>(), any(), any()) } returnsMany pages

        val result = manager.getCourseAnnouncements(courseId = 1L, forceNetwork = true)

        // Page size is 20 and the cap is 100, so it must stop after exactly 5 queries with 100 nodes,
        // never fetching the remaining pages even though hasNextPage stays true.
        coVerify(exactly = 5) { apolloClient.enqueueQuery(any<CourseAnnouncementsQuery>(), any(), any()) }
        assertEquals(100, result.course?.onCourse?.announcements?.nodes?.size)
    }

    @Test
    fun `getCourseAnnouncements fetches every page when total is below the cap`() = runTest {
        val pages = listOf(
            announcementResponse(nodeCount = 20, startId = 0, hasNextPage = true),
            announcementResponse(nodeCount = 15, startId = 20, hasNextPage = false)
        )
        coEvery { apolloClient.enqueueQuery(any<CourseAnnouncementsQuery>(), any(), any()) } returnsMany pages

        val result = manager.getCourseAnnouncements(courseId = 1L, forceNetwork = true)

        coVerify(exactly = 2) { apolloClient.enqueueQuery(any<CourseAnnouncementsQuery>(), any(), any()) }
        assertEquals(35, result.course?.onCourse?.announcements?.nodes?.size)
    }

    @Test
    fun `getCourseAnnouncements trims overshoot so the result never exceeds the fetch cap`() = runTest {
        // Pages larger than the per-loop bound check (30 each) accumulate past 100 (to 120),
        // so the result must be trimmed back down to exactly the cap.
        val pages = (0 until 10).map { pageIndex ->
            announcementResponse(nodeCount = 30, startId = pageIndex * 30, hasNextPage = true)
        }
        coEvery { apolloClient.enqueueQuery(any<CourseAnnouncementsQuery>(), any(), any()) } returnsMany pages

        val result = manager.getCourseAnnouncements(courseId = 1L, forceNetwork = true)

        assertEquals(100, result.course?.onCourse?.announcements?.nodes?.size)
    }

    @Test
    fun `getCourseAnnouncements does not page when the first page has no next page`() = runTest {
        val pages = listOf(
            announcementResponse(nodeCount = 5, startId = 0, hasNextPage = false)
        )
        coEvery { apolloClient.enqueueQuery(any<CourseAnnouncementsQuery>(), any(), any()) } returnsMany pages

        val result = manager.getCourseAnnouncements(courseId = 1L, forceNetwork = true)

        coVerify(exactly = 1) { apolloClient.enqueueQuery(any<CourseAnnouncementsQuery>(), any(), any()) }
        assertEquals(5, result.course?.onCourse?.announcements?.nodes?.size)
    }

    private fun announcementResponse(
        nodeCount: Int,
        startId: Int,
        hasNextPage: Boolean
    ): ApolloResponse<CourseAnnouncementsQuery.Data> {
        val nodes = (0 until nodeCount).map { i ->
            CourseAnnouncementsQuery.Node(
                _id = "${startId + i}",
                title = "Announcement ${startId + i}",
                message = null,
                postedAt = null,
                participant = CourseAnnouncementsQuery.Participant(read = false),
                entryCounts = CourseAnnouncementsQuery.EntryCounts(unreadCount = 0)
            )
        }
        val data = CourseAnnouncementsQuery.Data(
            course = CourseAnnouncementsQuery.Course(
                __typename = "Course",
                onCourse = CourseAnnouncementsQuery.OnCourse(
                    _id = "1",
                    announcements = CourseAnnouncementsQuery.Announcements(
                        pageInfo = CourseAnnouncementsQuery.PageInfo(
                            hasNextPage = hasNextPage,
                            endCursor = if (hasNextPage) "cursor_$startId" else null
                        ),
                        nodes = nodes
                    )
                )
            )
        )
        return ApolloResponse.Builder(
            operation = CourseAnnouncementsQuery(courseId = "1", pageSize = 20),
            requestUuid = UUID.randomUUID()
        ).data(data).build()
    }
}
