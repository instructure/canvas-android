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
 *
 */
package com.instructure.pandautils.data.repository.announcement

import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AnnouncementRepositoryTest {

    private val announcementApi: AnnouncementAPI.AnnouncementInterface = mockk(relaxed = true)
    private lateinit var repository: AnnouncementRepository

    @Before
    fun setup() {
        repository = AnnouncementRepositoryImpl(announcementApi)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getCourseAnnouncements returns success with announcements list`() = runTest {
        val announcements = listOf(
            DiscussionTopicHeader(id = 1L, title = "Announcement 1"),
            DiscussionTopicHeader(id = 2L, title = "Announcement 2")
        )
        val expected = DataResult.Success(announcements)
        coEvery {
            announcementApi.getFirstPageAnnouncementsList(any(), any(), any())
        } returns expected
        coEvery {
            announcementApi.getNextPageAnnouncementsList(any(), any())
        } returns DataResult.Success(emptyList())

        val result = repository.getCourseAnnouncements(
            courseId = 100L,
            forceRefresh = false
        )

        assertEquals(expected, result)
        coVerify {
            announcementApi.getFirstPageAnnouncementsList(
                "courses",
                100L,
                match { !it.isForceReadFromNetwork && it.usePerPageQueryParam }
            )
        }
    }

    @Test
    fun `getCourseAnnouncements with forceRefresh passes correct params`() = runTest {
        val announcements = listOf(DiscussionTopicHeader(id = 1L, title = "Announcement 1"))
        val expected = DataResult.Success(announcements)
        coEvery {
            announcementApi.getFirstPageAnnouncementsList(any(), any(), any())
        } returns expected
        coEvery {
            announcementApi.getNextPageAnnouncementsList(any(), any())
        } returns DataResult.Success(emptyList())

        val result = repository.getCourseAnnouncements(
            courseId = 200L,
            forceRefresh = true
        )

        assertEquals(expected, result)
        coVerify {
            announcementApi.getFirstPageAnnouncementsList(
                "courses",
                200L,
                match { it.isForceReadFromNetwork && it.usePerPageQueryParam }
            )
        }
    }

    @Test
    fun `getCourseAnnouncements returns failure`() = runTest {
        val expected = DataResult.Fail()
        coEvery {
            announcementApi.getFirstPageAnnouncementsList(any(), any(), any())
        } returns expected

        val result = repository.getCourseAnnouncements(
            courseId = 100L,
            forceRefresh = false
        )

        assertEquals(expected, result)
    }
}