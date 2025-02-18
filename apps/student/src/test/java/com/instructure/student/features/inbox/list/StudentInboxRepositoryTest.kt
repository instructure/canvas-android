package com.instructure.student.features.inbox.list/*
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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StudentInboxRepositoryTest {

    private val inboxApi: InboxApi.InboxInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupsApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val progressApi: ProgressAPI.ProgressInterface = mockk(relaxed = true)
    private val inboxSettingsManager: InboxSettingsManager = mockk(relaxed = true)

    private val inboxRepository =
        StudentInboxRepository(inboxApi, coursesApi, groupsApi, progressApi, inboxSettingsManager)

    @Test
    fun `Get contexts returns only valid courses`() = runTest {
        val courses = listOf(
            Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),
            Course(11) // no active enrollment
        )
        val groups = listOf(Group(id = 63, courseId = 44, name = "First group"),)
        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val canvasContextsResults = inboxRepository.getCanvasContexts()

        assertEquals(2, canvasContextsResults.dataOrNull!!.size)
        assertEquals(courses[0].id, canvasContextsResults.dataOrNull!![0].id)
        assertEquals(groups[0].id, canvasContextsResults.dataOrNull!![1].id)
        assertEquals(groups[0].name, canvasContextsResults.dataOrNull!![1].name)
    }

}