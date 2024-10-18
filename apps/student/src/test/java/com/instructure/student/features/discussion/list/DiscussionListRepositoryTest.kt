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
package com.instructure.student.features.discussion.list

import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.list.datasource.DiscussionListLocalDataSource
import com.instructure.student.features.discussion.list.datasource.DiscussionListNetworkDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DiscussionListRepositoryTest {

    private val networkDataSource: DiscussionListNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: DiscussionListLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = DiscussionListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get announcement creation permission from network data source for course when device is online`() = runTest {
        val course = Course(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getPermissionsForCourse(course) } returns CanvasContextPermission(canCreateAnnouncement = true)
        coEvery { localDataSource.getPermissionsForCourse(course) } returns null

        val result = repository.getCreationPermission(course, true)

        Assert.assertTrue(result)
    }

    @Test
    fun `Get announcement creation permission from local data source for course when device is offline`() = runTest {
        val course = Course(1)
        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getPermissionsForCourse(course) } returns CanvasContextPermission(canCreateAnnouncement = true)
        coEvery { localDataSource.getPermissionsForCourse(course) } returns null

        val result = repository.getCreationPermission(course, true)

        Assert.assertFalse(result)
    }

    @Test
    fun `Get announcement creation permission from network data source for group when device is online`() = runTest {
        val group = Group(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getPermissionsForGroup(group) } returns CanvasContextPermission(canCreateAnnouncement = true)
        coEvery { localDataSource.getPermissionsForGroup(group) } returns null

        val result = repository.getCreationPermission(group, true)

        Assert.assertTrue(result)
    }

    @Test
    fun `Get discussion creation permission from network data source for course when device is online`() = runTest {
        val course = Course(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getPermissionsForCourse(course) } returns CanvasContextPermission(canCreateDiscussionTopic = true)
        coEvery { localDataSource.getPermissionsForCourse(course) } returns null

        val result = repository.getCreationPermission(course, false)

        Assert.assertTrue(result)
    }

    @Test
    fun `Get discussion creation permission from local data source for course when device is offline`() = runTest {
        val course = Course(1)
        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getPermissionsForCourse(course) } returns CanvasContextPermission(canCreateDiscussionTopic = true)
        coEvery { localDataSource.getPermissionsForCourse(course) } returns null

        val result = repository.getCreationPermission(course, false)

        Assert.assertFalse(result)
    }

    @Test
    fun `Get discussion creation permission from network data source for group when device is online`() = runTest {
        val group = Group(1)
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getPermissionsForGroup(group) } returns CanvasContextPermission(canCreateDiscussionTopic = true)
        coEvery { localDataSource.getPermissionsForGroup(group) } returns null

        val result = repository.getCreationPermission(group, false)

        Assert.assertTrue(result)
    }

    @Test
    fun `Get discussions from network when device is online`() = runTest {
        val networkDiscussions = listOf(DiscussionTopicHeader(id = 1, title = "Discuss"), DiscussionTopicHeader(id = 1, title = "Discuss 2"))
        val localDiscussions = listOf(DiscussionTopicHeader(id = 3, title = "Discuss 3"), DiscussionTopicHeader(id = 4, title = "Discuss 4"))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getDiscussions(any(), any()) } returns networkDiscussions
        coEvery { localDataSource.getDiscussions(any(), any()) } returns localDiscussions

        val result = repository.getDiscussionTopicHeaders(Course(1), false, true)

        Assert.assertEquals(networkDiscussions, result)
    }

    @Test
    fun `Get discussions from local store when device is offline`() = runTest {
        val networkDiscussions = listOf(DiscussionTopicHeader(id = 1, title = "Discuss"), DiscussionTopicHeader(id = 1, title = "Discuss 2"))
        val localDiscussions = listOf(DiscussionTopicHeader(id = 3, title = "Discuss 3"), DiscussionTopicHeader(id = 4, title = "Discuss 4"))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getDiscussions(any(), any()) } returns networkDiscussions
        coEvery { localDataSource.getDiscussions(any(), any()) } returns localDiscussions

        val result = repository.getDiscussionTopicHeaders(Course(1), false, true)

        Assert.assertEquals(localDiscussions, result)
    }

    @Test
    fun `Get announcements from network when device is online`() = runTest {
        val networkAnnouncements = listOf(DiscussionTopicHeader(id = 1, title = "Announce"), DiscussionTopicHeader(id = 1, title = "Announce 2"))
        val localAnnouncements = listOf(DiscussionTopicHeader(id = 3, title = "Announce 3"), DiscussionTopicHeader(id = 4, title = "Announce 4"))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAnnouncements(any(), any()) } returns networkAnnouncements
        coEvery { localDataSource.getAnnouncements(any(), any()) } returns localAnnouncements

        val result = repository.getDiscussionTopicHeaders(Course(1), true, true)

        Assert.assertEquals(networkAnnouncements, result)
    }

    @Test
    fun `Get announcements from local store when device is offline`() = runTest {
        val networkAnnouncements = listOf(DiscussionTopicHeader(id = 1, title = "Announce"), DiscussionTopicHeader(id = 1, title = "Announce 2"))
        val localAnnouncements = listOf(DiscussionTopicHeader(id = 3, title = "Announce 3"), DiscussionTopicHeader(id = 4, title = "Announce 4"))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAnnouncements(any(), any()) } returns networkAnnouncements
        coEvery { localDataSource.getAnnouncements(any(), any()) } returns localAnnouncements

        val result = repository.getDiscussionTopicHeaders(Course(1), true, true)

        Assert.assertEquals(networkAnnouncements, result)
    }
}