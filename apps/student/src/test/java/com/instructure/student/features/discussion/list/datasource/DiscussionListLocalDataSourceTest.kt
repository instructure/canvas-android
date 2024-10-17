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

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class DiscussionListLocalDataSourceTest {

    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade = mockk(relaxed = true)

    private val dataSource = DiscussionListLocalDataSource(discussionTopicHeaderFacade)

    @Test
    fun `Return null for course permission`() = runTest {
        Assert.assertNull(dataSource.getPermissionsForCourse(Course(1)))
    }

    @Test
    fun `Return null for groups permission`() = runTest {
        Assert.assertNull(dataSource.getPermissionsForGroup(Group(1)))
    }

    @Test
    fun `Datasource returns correct discussions for course`() = runTest {
        val discussions = listOf(DiscussionTopicHeader(id = 1, "Discuss"), DiscussionTopicHeader(id = 2, "Discuss 2"))
        coEvery { discussionTopicHeaderFacade.getDiscussionsForCourse(any()) } returns discussions

        val result = dataSource.getDiscussions(Course(1), false)

        Assert.assertEquals(discussions, result)
    }

    @Test
    fun `Datasource returns correct announcements for course`() = runTest {
        val announcements = listOf(DiscussionTopicHeader(id = 1, "announce"), DiscussionTopicHeader(id = 2, "announce 2"))
        coEvery { discussionTopicHeaderFacade.getAnnouncementsForCourse(any()) } returns announcements

        val result = dataSource.getAnnouncements(Course(1), false)

        Assert.assertEquals(announcements, result)
    }
}