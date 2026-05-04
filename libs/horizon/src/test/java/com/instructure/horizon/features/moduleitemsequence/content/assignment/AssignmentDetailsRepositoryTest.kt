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
package com.instructure.horizon.features.moduleitemsequence.content.assignment

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.horizon.data.datasource.AssignmentDetailsLocalDataSource
import com.instructure.horizon.data.datasource.AssignmentDetailsNetworkDataSource
import com.instructure.horizon.data.datasource.SubmissionLocalDataSource
import com.instructure.horizon.data.repository.AssignmentDetailsRepository
import com.instructure.horizon.data.repository.HorizonFileSyncRepository
import com.instructure.horizon.database.dao.HorizonCourseModuleDao
import com.instructure.horizon.database.dao.HorizonEntitySyncMetadataDao
import com.instructure.pandautils.features.offline.sync.HtmlParser
import com.instructure.pandautils.features.offline.sync.HtmlParsingResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class AssignmentDetailsRepositoryTest {
    private val networkDataSource: AssignmentDetailsNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: AssignmentDetailsLocalDataSource = mockk(relaxed = true)
    private val submissionLocalDataSource: SubmissionLocalDataSource = mockk(relaxed = true)
    private val courseModuleDao: HorizonCourseModuleDao = mockk(relaxed = true)
    private val entitySyncMetadataDao: HorizonEntitySyncMetadataDao = mockk(relaxed = true)
    private val htmlParser: HtmlParser = mockk(relaxed = true)
    private val fileSyncRepository: HorizonFileSyncRepository = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val courseId = 1L
    private val assignmentId = 10L
    private val testAssignment = Assignment(id = assignmentId, name = "Test Assignment", pointsPossible = 100.0)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAssignment fetches from network when online`() = runTest {
        coEvery { networkDataSource.getAssignment(courseId, assignmentId, false) } returns testAssignment
        coEvery { htmlParser.createHtmlStringWithLocalFiles(any(), any()) } returns HtmlParsingResult("", emptySet(), emptySet(), emptySet())

        val result = getRepository().getAssignment(courseId, assignmentId, false)

        assertEquals(testAssignment, result)
        coVerify { networkDataSource.getAssignment(courseId, assignmentId, false) }
    }

    @Test(expected = IllegalStateException::class)
    fun `getAssignment throws when offline and no cached data`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getAssignment(assignmentId) } returns null

        getRepository().getAssignment(courseId, assignmentId, false)
    }

    @Test
    fun `getAssignment returns cached data when offline`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getAssignment(assignmentId) } returns testAssignment

        val result = getRepository().getAssignment(courseId, assignmentId, false)

        assertEquals(testAssignment, result)
        coVerify { localDataSource.getAssignment(assignmentId) }
    }

    @Test
    fun `getAssignment saves to local when online and sync enabled`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignment(courseId, assignmentId, false) } returns testAssignment
        coEvery { htmlParser.createHtmlStringWithLocalFiles(any(), any()) } returns HtmlParsingResult("parsed", emptySet(), emptySet(), emptySet())

        getRepository().getAssignment(courseId, assignmentId, false)

        coVerify { localDataSource.saveAssignment(testAssignment, courseId, "parsed") }
    }

    @Test
    fun `getAssignment saves submission history when online and sync enabled`() = runTest {
        val submission = Submission(id = 1L, attempt = 1L, workflowState = "submitted")
        val assignmentWithSubmission = testAssignment.copy(
            submission = Submission(submissionHistory = listOf(submission))
        )
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignment(courseId, assignmentId, false) } returns assignmentWithSubmission
        coEvery { htmlParser.createHtmlStringWithLocalFiles(any(), any()) } returns HtmlParsingResult("", emptySet(), emptySet(), emptySet())

        getRepository().getAssignment(courseId, assignmentId, false)

        coVerify { submissionLocalDataSource.saveSubmissions(assignmentId, listOf(submission)) }
    }

    @Test
    fun `getAssignment passes forceRefresh to network data source`() = runTest {
        coEvery { networkDataSource.getAssignment(courseId, assignmentId, true) } returns testAssignment
        coEvery { htmlParser.createHtmlStringWithLocalFiles(any(), any()) } returns HtmlParsingResult("", emptySet(), emptySet(), emptySet())

        getRepository().getAssignment(courseId, assignmentId, true)

        coVerify { networkDataSource.getAssignment(courseId, assignmentId, true) }
    }

    private fun getRepository() = AssignmentDetailsRepository(
        networkDataSource,
        localDataSource,
        submissionLocalDataSource,
        courseModuleDao,
        entitySyncMetadataDao,
        htmlParser,
        fileSyncRepository,
        networkStateProvider,
        featureFlagProvider,
    )
}
