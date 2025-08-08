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

package com.instructure.pandautils.features.speedgrader.details.submissiondetails

import com.instructure.canvasapi2.SubmissionDetailsQuery
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class SubmissionDetailsRepositoryTest {

    private val submissionDetailsManager = mockk<SubmissionDetailsManager>()

    private val repository = SubmissionDetailsRepository(submissionDetailsManager)

    @Test
    fun `getSubmission calls submissionDetailsManager and returns data`() = runTest {
        val assignmentId = 123L
        val studentId = 456L
        val expectedData = mockk<SubmissionDetailsQuery.Data>()

        coEvery { submissionDetailsManager.getSubmissionDetails(studentId, assignmentId) } returns expectedData

        val result = repository.getSubmission(assignmentId, studentId)

        Assert.assertEquals(expectedData, result)
    }

    @Test(expected = Exception::class)
    fun `getSubmission throws exception when submissionDetailsManager fails`() = runTest {
        val assignmentId = 123L
        val studentId = 456L

        coEvery { submissionDetailsManager.getSubmissionDetails(studentId, assignmentId) } throws Exception("Error")

        repository.getSubmission(assignmentId, studentId)
    }
}
