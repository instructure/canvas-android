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
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class DiscussionListNetworkDataSourceTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val discussionApi: DiscussionAPI.DiscussionInterface = mockk(relaxed = true)
    private val announcementApi: AnnouncementAPI.AnnouncementInterface = mockk(relaxed = true)

    private val discussionListNetworkDataSource = DiscussionListNetworkDataSource(
        courseApi = courseApi,
        groupApi = groupApi,
        discussionApi = discussionApi,
        announcementApi = announcementApi
    )

    @Test
    fun `Return null permissions for course when the request fails`() = runTest {
        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Fail()

        val result = discussionListNetworkDataSource.getPermissionsForCourse(Course(1))

        Assert.assertNull(result)
    }

    @Test
    fun `Return correct course permissions when the request is successful`() = runTest {
        val permissions = CanvasContextPermission(canCreateAnnouncement = true)
        val course = Course(1).apply { this.permissions = permissions }
        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Success(course)

        val result = discussionListNetworkDataSource.getPermissionsForCourse(Course(1))

        Assert.assertEquals(permissions, result)
    }

    @Test
    fun `Return null permissions for group when the request fails`() = runTest {
        coEvery { groupApi.getDetailedGroup(any(), any()) } returns DataResult.Fail()

        val result = discussionListNetworkDataSource.getPermissionsForGroup(Group(1))

        Assert.assertNull(result)
    }

    @Test
    fun `Return correct group permissions when the request is successful`() = runTest {
        val permissions = CanvasContextPermission(canCreateAnnouncement = true)
        val group = Group(1).apply { this.permissions = permissions }
        coEvery { groupApi.getDetailedGroup(any(), any()) } returns DataResult.Success(group)

        val result = discussionListNetworkDataSource.getPermissionsForGroup(Group(1))

        Assert.assertEquals(permissions, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw error when discussion request fails`() = runTest {
        coEvery { discussionApi.getFirstPageDiscussionTopicHeaders(any(), any(), any()) } returns DataResult.Fail()

        discussionListNetworkDataSource.getDiscussions(Course(1), false)
    }

    @Test
    fun `Return empty list when discussion request is successful but no discussions are returned`() = runTest {
        coEvery { discussionApi.getFirstPageDiscussionTopicHeaders(any(), any(), any()) } returns DataResult.Success(emptyList())

        val result = discussionListNetworkDataSource.getDiscussions(Course(1), false)

        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun `Return correct list of discussions when discussion request is successful`() = runTest {
        val discussions = listOf(DiscussionTopicHeader(id = 1, title = "Discussion 1"))
        coEvery { discussionApi.getFirstPageDiscussionTopicHeaders(any(), any(), any()) } returns DataResult.Success(discussions)

        val result = discussionListNetworkDataSource.getDiscussions(Course(1), false)

        Assert.assertEquals(discussions, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw error when announcement request fails`() = runTest {
        coEvery { announcementApi.getFirstPageAnnouncementsList(any(), any(), any()) } returns DataResult.Fail()

        discussionListNetworkDataSource.getAnnouncements(Course(1), false)
    }

    @Test
    fun `Return empty list when announcement request is successful but no discussions are returned`() = runTest {
        coEvery { announcementApi.getFirstPageAnnouncementsList(any(), any(), any()) } returns DataResult.Success(emptyList())

        val result = discussionListNetworkDataSource.getAnnouncements(Course(1), false)

        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun `Return correct list of announcements when discussion request is successful`() = runTest {
        val announcements = listOf(DiscussionTopicHeader(id = 1, title = "Announce 1"))
        coEvery { announcementApi.getFirstPageAnnouncementsList(any(), any(), any()) } returns DataResult.Success(announcements)

        val result = discussionListNetworkDataSource.getAnnouncements(Course(1), false)

        Assert.assertEquals(announcements, result)
    }
}