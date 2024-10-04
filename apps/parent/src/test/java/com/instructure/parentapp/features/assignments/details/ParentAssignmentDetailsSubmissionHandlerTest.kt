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
        assertEquals(false, submissionHandler.lastSubmissionIsDraft)
        assertEquals(null, submissionHandler.lastSubmissionEntry)
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