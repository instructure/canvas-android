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
package com.instructure.horizon.features.dashboard

import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DashboardRepositoryTest {
    private val unreadCountApi: UnreadCountAPI.UnreadCountsInterface = mockk(relaxed = true)

    private val notificationCounts = listOf(
        UnreadNotificationCount(
            type = "Message",
            count = 5,
            unreadCount = 10,
        ),
        UnreadNotificationCount(
            type = "Conversation",
            count = 2,
            unreadCount = 5,
        ),
        UnreadNotificationCount(
            type = "Announcement",
            count = 1,
            unreadCount = 3,
        ),
    )

    @Before
    fun setup() {
        coEvery { unreadCountApi.getNotificationsCount(any()) } returns DataResult.Success(notificationCounts)
    }

    @Test
    fun `Test successful UnreadCount call`() = runTest {
        val repository = getRepository()
        val result = repository.getUnreadCounts(true)
        assertEquals(3, result.size)
        assertEquals(notificationCounts, result)
    }

    private fun getRepository(): DashboardRepository {
        return DashboardRepository(unreadCountApi)
    }
}