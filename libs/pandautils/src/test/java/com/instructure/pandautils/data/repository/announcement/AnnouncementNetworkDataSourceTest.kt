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
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementNetworkDataSourceTest {

    private val announcementApi: AnnouncementAPI.AnnouncementInterface = mockk(relaxed = true)

    private val dataSource = AnnouncementNetworkDataSource(announcementApi)

    @Test
    fun `getCourseAnnouncements returns announcements from api`() = runTest {
        val announcements = listOf(
            DiscussionTopicHeader(id = 1, title = "Announcement 1"),
            DiscussionTopicHeader(id = 2, title = "Announcement 2")
        )
        coEvery { announcementApi.getFirstPageAnnouncementsList(any(), eq(1L), any()) } returns DataResult.Success(announcements)

        val result = dataSource.getCourseAnnouncements(1, false)

        assertTrue(result is DataResult.Success)
        assertEquals(announcements, (result as DataResult.Success).data)
    }

    @Test
    fun `getCourseAnnouncements returns Fail when api fails`() = runTest {
        coEvery { announcementApi.getFirstPageAnnouncementsList(any(), eq(1L), any()) } returns DataResult.Fail()

        val result = dataSource.getCourseAnnouncements(1, false)

        assertTrue(result is DataResult.Fail)
    }
}