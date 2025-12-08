/*
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
package com.instructure.parentapp.features.assignments.details

import com.instructure.canvasapi2.models.Assignment
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsSubmissionHandler
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ParentAssignmentDetailsSubmissionHandlerTest {
    private val submissionHandler = ParentAssignmentDetailsSubmissionHandler()

    @Test
    fun testUploadDefaultValues() {
        assertEquals(false, submissionHandler.isUploading)
        assertEquals(false, submissionHandler.isFailed)
        assertEquals(false, submissionHandler.lastSubmissionIsDraft)
        assertEquals(null, submissionHandler.lastSubmissionEntry)
        assertEquals(null, submissionHandler.lastSubmissionId)
        assertEquals(null, submissionHandler.lastSubmissionAssignmentId)
        assertEquals(null, submissionHandler.lastSubmissionSubmissionType)
    }

    @Test
    fun testUploadDefaultFunctions() = runTest {
        assertEquals(null, submissionHandler.getVideoUri(mockk(relaxed = true)))
        val ltiTool = submissionHandler.getStudioLTITool(Assignment(), 0L)
        assertEquals(null, ltiTool)
    }
}