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
package com.instructure.horizon.features.notification

import com.instructure.canvasapi2.models.StreamItem
import com.instructure.horizon.data.datasource.NotificationStreamLocalDataSource
import com.instructure.horizon.data.datasource.NotificationStreamNetworkDataSource
import com.instructure.horizon.data.repository.NotificationStreamRepository
import com.instructure.horizon.model.NotificationStreamItem
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotificationRepositoryTest {
    private val networkDataSource: NotificationStreamNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: NotificationStreamLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    @Before
    fun setup() {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { localDataSource.courseNameFor(any()) } returns "Course 1"
    }

    @Test
    fun `Filters stream items by enrolled course and category when online`() = runTest {
        val validCourseId = 1L
        val notValidCourseId = 2L

        val items = listOf(
            StreamItem(id = 1, type = "Conversation", course_id = validCourseId),
            StreamItem(id = 2, type = "Message", course_id = validCourseId),
            StreamItem(id = 3, type = "DiscussionTopic", course_id = validCourseId),
            StreamItem(id = 4, type = "Announcement", course_id = validCourseId),
            StreamItem(id = 5, type = "Announcement", course_id = notValidCourseId),
            StreamItem(id = 6, type = "Message", notificationCategory = "Due Date", course_id = validCourseId),
            StreamItem(id = 7, type = "Message", grade = "A", course_id = validCourseId),
            StreamItem(id = 8, type = "Message", score = 95.0, course_id = validCourseId),
            StreamItem(id = 9, type = "Message", notificationCategory = "Grading Policies", course_id = validCourseId),
        )

        coEvery { networkDataSource.getCoursesInProgressIds(any()) } returns listOf(validCourseId)
        coEvery { networkDataSource.getStream(any()) } returns items

        val result = getRepository().getNotifications(forceRefresh = true)

        assertEquals(4, result.size)
        assertFalse(result.any { it.id == 4L })
        assertTrue(result.any { it.id == 6L })
        assertTrue(result.any { it.id == 7L })
        assertTrue(result.any { it.id == 8L })
        assertTrue(result.any { it.id == 9L })
    }

    @Test(expected = IllegalStateException::class)
    fun `Throws when network stream call fails`() = runTest {
        coEvery { networkDataSource.getCoursesInProgressIds(any()) } returns emptyList()
        coEvery { networkDataSource.getStream(any()) } throws IllegalStateException("boom")
        getRepository().getNotifications(true)
    }

    @Test
    fun `Reads from local data source when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        val cached = listOf(
            NotificationStreamItem(
                id = 10,
                type = "Message",
                notificationCategory = "Due Date",
                title = "Cached",
                courseId = 1L,
                courseName = "Course 1",
                isRead = true,
                score = -1.0,
                grade = null,
                updatedAt = null,
                htmlUrl = "url",
                assignmentHtmlUrl = null,
            )
        )
        coEvery { localDataSource.getStream() } returns cached

        val result = getRepository().getNotifications(forceRefresh = false)

        assertEquals(cached, result)
    }

    private fun getRepository(): NotificationStreamRepository {
        return NotificationStreamRepository(
            networkDataSource = networkDataSource,
            localDataSource = localDataSource,
            networkStateProvider = networkStateProvider,
            featureFlagProvider = featureFlagProvider,
        )
    }
}
