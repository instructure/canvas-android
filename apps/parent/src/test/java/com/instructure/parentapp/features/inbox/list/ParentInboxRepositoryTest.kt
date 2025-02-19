package com.instructure.parentapp.features.inbox.list/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ParentInboxRepositoryTest {

    private val inboxApi: InboxApi.InboxInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupsApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val progressApi: ProgressAPI.ProgressInterface = mockk(relaxed = true)
    private val inboxSettingsManager: InboxSettingsManager = mockk(relaxed = true)

    private val inboxRepository =
        ParentInboxRepository(inboxApi, coursesApi, groupsApi, progressApi, inboxSettingsManager)

    @Test
    fun `Get contexts returns only valid courses`() = runTest {
        val courses = listOf(
            Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),
            Course(11) // no active enrollment
        )
        coEvery { coursesApi.getCoursesByEnrollmentType(any(), any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(emptyList())

        val canvasContextsResults = inboxRepository.getCanvasContexts()

        assertEquals(1, canvasContextsResults.dataOrNull!!.size)
        assertEquals(courses[0].id, canvasContextsResults.dataOrNull!![0].id)
    }

    @Test
    fun `Get contexts returns failed results when request fails`() = runTest {
        coEvery { coursesApi.getCoursesByEnrollmentType(any(), any()) } returns DataResult.Fail()

        val canvasContextsResults = inboxRepository.getCanvasContexts()

        assertEquals(DataResult.Fail(), canvasContextsResults)
    }
}