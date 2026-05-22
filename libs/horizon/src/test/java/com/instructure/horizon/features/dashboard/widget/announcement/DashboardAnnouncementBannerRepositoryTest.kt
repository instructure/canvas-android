/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.dashboard.widget.announcement

import com.instructure.horizon.domain.usecase.GetUnreadAnnouncementsUseCase
import com.instructure.horizon.model.HorizonAnnouncement
import com.instructure.horizon.model.HorizonAnnouncementKind
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class DashboardAnnouncementBannerRepositoryTest {
    private val getUnreadAnnouncementsUseCase: GetUnreadAnnouncementsUseCase = mockk(relaxed = true)

    @Test
    fun `Maps course announcements to banner items`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns listOf(
            announcement(id = 1, kind = HorizonAnnouncementKind.COURSE, courseId = 100L, title = "Course A1", courseName = "Course 1"),
        )

        val result = getRepository().getUnreadAnnouncements(forceRefresh = false)

        assertEquals(1, result.size)
        assertEquals("Course A1", result[0].title)
        assertEquals("Course 1", result[0].source)
        assertEquals(AnnouncementType.COURSE, result[0].type)
    }

    @Test
    fun `Maps account announcements to banner items`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns listOf(
            announcement(id = 2, kind = HorizonAnnouncementKind.ACCOUNT, courseId = null, title = "Global", courseName = null),
        )

        val result = getRepository().getUnreadAnnouncements(forceRefresh = false)

        assertEquals(1, result.size)
        assertEquals("Global", result[0].title)
        assertEquals(null, result[0].source)
        assertEquals(AnnouncementType.GLOBAL, result[0].type)
    }

    @Test
    fun `Returns empty when use case returns empty`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns emptyList()
        assertEquals(0, getRepository().getUnreadAnnouncements(false).size)
    }

    private fun announcement(
        id: Long,
        kind: HorizonAnnouncementKind,
        courseId: Long?,
        title: String,
        courseName: String?,
    ) = HorizonAnnouncement(
        id = id,
        kind = kind,
        courseId = courseId,
        courseName = courseName,
        title = title,
        message = "",
        authorDisplayName = null,
        postedAt = Date(),
        isUnread = true,
        isClosed = false,
    )

    private fun getRepository(): DashboardAnnouncementBannerRepository {
        return DashboardAnnouncementBannerRepository(getUnreadAnnouncementsUseCase)
    }
}
