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
package com.instructure.student.features.assignments.details

import android.util.Log
import android.widget.Toast
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsViewData
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.student.room.StudentDb
import com.instructure.student.room.entities.CreateSubmissionEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StudentAssignmentDetailsSubmissionHandlerTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val submissionHelper: SubmissionHelper = mockk(relaxed = true)
    private val studentDb: StudentDb = mockk(relaxed = true)

    private lateinit var submissionHandler: StudentAssignmentDetailsSubmissionHandler

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        ContextKeeper.appContext = mockk(relaxed = true)
        mockkStatic(Toast::class)
        every { Toast.makeText(any(), any<Int>(), any()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Test initial values`() {
        submissionHandler = StudentAssignmentDetailsSubmissionHandler(submissionHelper, studentDb)
        assertEquals(false, submissionHandler.isUploading)
        assertEquals(false, submissionHandler.lastSubmissionIsDraft)
        assertEquals(null, submissionHandler.lastSubmissionEntry)
        assertEquals(null, submissionHandler.lastSubmissionAssignmentId)
        assertEquals(null, submissionHandler.lastSubmissionSubmissionType)
    }

    @Test
    fun `Upload fail`() {
        submissionHandler = StudentAssignmentDetailsSubmissionHandler(submissionHelper, studentDb)

        val data = MutableLiveData<AssignmentDetailsViewData>(AssignmentDetailsViewData(
            courseColor = ColorKeeper.getOrGenerateColor(Course()),
            submissionAndRubricLabelColor = ThemePrefs.textButtonColor,
            assignmentName = "Assignment",
            points = "0",
            submissionStatusText = "Status",
            submissionStatusIcon = 1,
            submissionStatusTint = 1,
            submissionStatusVisible = true,
            fullLocked = true,
            lockedMessage = ""
        ))

        val liveData = MutableLiveData<List<CreateSubmissionEntity>>(listOf())
        every {
            studentDb.submissionDao().findSubmissionsByAssignmentIdLiveData(any(), any())
        } returns liveData

        submissionHandler.addAssignmentSubmissionObserver(mockk(relaxed = true), 0, 0, mockk(relaxed = true), data, {})

        liveData.postValue(listOf(getDbSubmission()))

        assertTrue(submissionHandler.isUploading)

        liveData.postValue(listOf(getDbSubmission().copy(errorFlag = true)))
        assertTrue(data.value?.attempts?.first()?.data?.isFailed!!)
    }

    @Test
    fun `Upload success`() {
        submissionHandler = StudentAssignmentDetailsSubmissionHandler(submissionHelper, studentDb)

        val data = MutableLiveData<AssignmentDetailsViewData>(AssignmentDetailsViewData(
            courseColor = ColorKeeper.getOrGenerateColor(Course()),
            submissionAndRubricLabelColor = ThemePrefs.textButtonColor,
            assignmentName = "Assignment",
            points = "0",
            submissionStatusText = "Status",
            submissionStatusIcon = 1,
            submissionStatusTint = 1,
            submissionStatusVisible = true,
            fullLocked = true,
            lockedMessage = ""
        ))

        val liveData = MutableLiveData<List<CreateSubmissionEntity>>(listOf())
        every {
            studentDb.submissionDao().findSubmissionsByAssignmentIdLiveData(any(), any())
        } returns liveData

        submissionHandler.addAssignmentSubmissionObserver(
            mockk(relaxed = true), 0, 0, mockk(relaxed = true), data, {})

        liveData.postValue(listOf(getDbSubmission()))
        assertTrue(submissionHandler.isUploading)

        liveData.postValue(emptyList())
        assertFalse(submissionHandler.isUploading)
    }

    private fun getDbSubmission() = CreateSubmissionEntity(
        id = 0,
        submissionEntry = "",
        lastActivityDate = null,
        assignmentName = null,
        assignmentId = 0,
        canvasContext = CanvasContext.emptyCourseContext(0),
        submissionType = "",
        errorFlag = false,
        assignmentGroupCategoryId = null,
        userId = 0,
        currentFile = 0,
        fileCount = 0,
        progress = null,
        annotatableAttachmentId = null,
        isDraft = false
    )
}