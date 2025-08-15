/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.speedgrader.details.studentnotes

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.ColumnDatum
import com.instructure.canvasapi2.models.CustomColumn
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class StudentNotesRepositoryTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private val repository = StudentNotesRepository(courseApi)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when get custom grade book columns fails`() = runTest {
        coEvery { courseApi.getCustomGradeBookColumns(any(), any()) } returns DataResult.Fail()

        repository.getCustomGradeBookColumns(1, forceRefresh = true)
    }

    @Test
    fun `Get custom grade book columns successful`() = runTest {
        val expected = listOf(
            CustomColumn(
                id = 1,
                title = "Test Column 1",
                position = 1,
                hidden = false,
                teacherNotes = true
            ),
            CustomColumn(
                id = 2,
                title = "Test Column 2",
                position = 2,
                hidden = true,
                teacherNotes = false
            )
        )

        coEvery { courseApi.getCustomGradeBookColumns(any(), any()) } returns DataResult.Success(expected)

        val result = repository.getCustomGradeBookColumns(1, forceRefresh = true)

        assertEquals(result, expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when get custom grade book columns entries fails`() = runTest {
        coEvery { courseApi.getCustomGradeBookColumnsEntries(any(), any(), any()) } returns DataResult.Fail()

        repository.getCustomGradeBookColumnsEntries(1, 1, forceRefresh = true)
    }

    @Test
    fun `Get custom grade book columns entries successful`() = runTest {
        val expected = listOf(
            ColumnDatum(
                userId = 1,
                content = "Note for student 1"
            ),
            ColumnDatum(
                userId = 2,
                content = "Note for student 2"
            )
        )

        coEvery { courseApi.getCustomGradeBookColumnsEntries(any(), any(), any()) } returns DataResult.Success(expected)

        val result = repository.getCustomGradeBookColumnsEntries(1, 1, forceRefresh = true)

        assertEquals(result, expected)
    }
}
