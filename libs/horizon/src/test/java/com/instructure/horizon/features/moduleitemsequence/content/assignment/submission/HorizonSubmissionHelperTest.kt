/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.SubmissionState
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class HorizonSubmissionHelperTest {
    private val studentDb: StudentDb = mockk(relaxed = true)
    private val submissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)

    private lateinit var helper: HorizonSubmissionHelper

    @Before
    fun setup() {
        every { studentDb.submissionDao() } returns submissionDao
        helper = HorizonSubmissionHelper(studentDb, apiPrefs, workManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun entity(type: Assignment.SubmissionType) = CreateSubmissionEntity(
        id = 42L,
        assignmentId = 7L,
        canvasContext = CanvasContext.emptyCourseContext(1L),
        userId = 1L,
        submissionType = type.apiString,
        submissionState = SubmissionState.FAILED,
        errorFlag = true,
        isDraft = true,
    )

    @Test
    fun `retrySubmission resets flags and re-enqueues a text submission`() = runTest {
        helper.retrySubmission(entity(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        coVerify { submissionDao.setSubmissionError(false, 42L) }
        coVerify { submissionDao.setDraft(42L, false) }
        coVerify { submissionDao.updateSubmissionState(42L, SubmissionState.QUEUED, any()) }
        verify { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `retrySubmission re-enqueues a file submission`() = runTest {
        helper.retrySubmission(entity(Assignment.SubmissionType.ONLINE_UPLOAD))

        coVerify { submissionDao.updateSubmissionState(42L, SubmissionState.QUEUED, any()) }
        verify { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `retrySubmission ignores unsupported submission types`() = runTest {
        helper.retrySubmission(entity(Assignment.SubmissionType.ONLINE_URL))

        coVerify(exactly = 0) { submissionDao.updateSubmissionState(any(), any(), any()) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `cancelSubmission cancels the work and reverts to a draft`() = runTest {
        helper.cancelSubmission(entity(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        verify { workManager.cancelUniqueWork(HorizonSubmissionHelper.workName(42L)) }
        coVerify { submissionDao.setDraft(42L, true) }
        coVerify { submissionDao.updateSubmissionState(42L, SubmissionState.QUEUED, any()) }
    }

    @Test
    fun `submitScheduledSubmission re-enqueues the submission`() = runTest {
        helper.submitScheduledSubmission(entity(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        coVerify { submissionDao.setDraft(42L, false) }
        coVerify { submissionDao.updateSubmissionState(42L, SubmissionState.QUEUED, any()) }
        verify { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }
}
