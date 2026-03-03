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

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementLocalDataSourceTest {

    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade = mockk(relaxed = true)

    private val dataSource = AnnouncementLocalDataSource(discussionTopicHeaderFacade)

    @Test
    fun `getCourseAnnouncements returns announcements from facade`() = runTest {
        val announcements = listOf(
            DiscussionTopicHeader(id = 1, title = "Announcement 1"),
            DiscussionTopicHeader(id = 2, title = "Announcement 2")
        )
        coEvery { discussionTopicHeaderFacade.getAnnouncementsForCourse(1) } returns announcements

        val result = dataSource.getCourseAnnouncements(1, false)

        assertTrue(result is DataResult.Success)
        assertEquals(announcements, (result as DataResult.Success).data)
    }

    @Test
    fun `getCourseAnnouncements returns empty list when no announcements synced`() = runTest {
        coEvery { discussionTopicHeaderFacade.getAnnouncementsForCourse(1) } returns emptyList()

        val result = dataSource.getCourseAnnouncements(1, false)

        assertTrue(result is DataResult.Success)
        assertEquals(emptyList<DiscussionTopicHeader>(), (result as DataResult.Success).data)
    }
}