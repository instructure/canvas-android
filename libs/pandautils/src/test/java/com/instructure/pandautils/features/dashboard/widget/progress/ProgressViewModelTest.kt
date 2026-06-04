/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.progress

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.features.dashboard.DashboardNavigationEvent
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    private val observeUploadsUseCase: ObserveUploadsUseCase = mockk(relaxed = true)
    private val observeSyncProgressUseCase: ObserveSyncProgressUseCase = mockk(relaxed = true)
    private val dismissUploadUseCase: DismissUploadUseCase = mockk(relaxed = true)
    private val dismissSyncProgressUseCase: DismissSyncProgressUseCase = mockk(relaxed = true)
    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val dashboardFileUploadDao: DashboardFileUploadDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeUploadsUseCase() } returns flowOf(emptyList())
        every { observeSyncProgressUseCase() } returns flowOf(null)
        every { apiPrefs.user } returns User(id = 123L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state shows loading`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `Observes uploads on init`() = runTest {
        val uploads = listOf(createUploadItem())
        every { observeUploadsUseCase() } returns flowOf(uploads)

        val viewModel = getViewModel()

        assertEquals(uploads, viewModel.uiState.value.uploadItems)
    }

    @Test
    fun `Observes sync progress on init`() = runTest {
        val syncProgress = createSyncProgressItem()
        every { observeSyncProgressUseCase() } returns flowOf(syncProgress)

        val viewModel = getViewModel()

        assertEquals(syncProgress, viewModel.uiState.value.syncProgress)
    }

    @Test
    fun `Upload click for succeeded assignment upload emits navigation event`() = runTest {
        val course = Course(id = 456L)
        coEvery { courseRepository.getCourse(456L, false) } returns DataResult.Success(course)

        val viewModel = getViewModel()
        val item = createUploadItem(
            state = UploadState.SUCCEEDED,
            courseId = 456L,
            assignmentId = 789L,
            attemptId = 1L
        )

        val navigationEvents = mutableListOf<DashboardNavigationEvent.Progress>()
        val job = launch(testDispatcher) {
            viewModel.navigationEvents.collect { navigationEvents.add(it) }
        }

        viewModel.uiState.value.onUploadClick(item)
        advanceUntilIdle()

        coVerify { dashboardFileUploadDao.deleteByWorkerId(item.workerId.toString()) }
        assertEquals(1, navigationEvents.size)
        val event = navigationEvents[0] as DashboardNavigationEvent.Progress.NavigateToSubmissionDetails
        assertEquals(course, event.course)
        assertEquals(789L, event.assignmentId)
        assertEquals(1L, event.attemptId)

        job.cancel()
    }

    @Test
    fun `Upload click for succeeded file upload emits navigation event`() = runTest {
        val user = User(id = 123L)
        every { apiPrefs.user } returns user

        val viewModel = getViewModel()
        val item = createUploadItem(
            state = UploadState.SUCCEEDED,
            courseId = null,
            assignmentId = null,
            attemptId = null,
            folderId = 999L
        )

        val navigationEvents = mutableListOf<DashboardNavigationEvent.Progress>()
        val job = launch(testDispatcher) {
            viewModel.navigationEvents.collect { navigationEvents.add(it) }
        }

        viewModel.uiState.value.onUploadClick(item)
        advanceUntilIdle()

        coVerify { dashboardFileUploadDao.deleteByWorkerId(item.workerId.toString()) }
        assertEquals(1, navigationEvents.size)
        val event = navigationEvents[0] as DashboardNavigationEvent.Progress.NavigateToMyFiles
        assertEquals(user, event.user)
        assertEquals(999L, event.folderId)

        job.cancel()
    }

    @Test
    fun `Upload click for succeeded upload with failed course fetch emits open progress dialog event`() = runTest {
        coEvery { courseRepository.getCourse(456L, false) } returns DataResult.Fail()

        val viewModel = getViewModel()
        val item = createUploadItem(
            state = UploadState.SUCCEEDED,
            courseId = 456L,
            assignmentId = 789L,
            attemptId = 1L
        )

        val navigationEvents = mutableListOf<DashboardNavigationEvent.Progress>()
        val job = launch(testDispatcher) {
            viewModel.navigationEvents.collect { navigationEvents.add(it) }
        }

        viewModel.uiState.value.onUploadClick(item)
        advanceUntilIdle()

        assertEquals(1, navigationEvents.size)
        val event = navigationEvents[0] as DashboardNavigationEvent.Progress.OpenProgressDialog
        assertEquals(item.workerId, event.workerId)

        job.cancel()
    }

    @Test
    fun `Upload click for succeeded upload with no destination emits open progress dialog event`() = runTest {
        val viewModel = getViewModel()
        val item = createUploadItem(
            state = UploadState.SUCCEEDED,
            courseId = null,
            assignmentId = null,
            attemptId = null,
            folderId = null
        )

        val navigationEvents = mutableListOf<DashboardNavigationEvent.Progress>()
        val job = launch(testDispatcher) {
            viewModel.navigationEvents.collect { navigationEvents.add(it) }
        }

        viewModel.uiState.value.onUploadClick(item)
        advanceUntilIdle()

        coVerify { dashboardFileUploadDao.deleteByWorkerId(item.workerId.toString()) }
        assertEquals(1, navigationEvents.size)
        val event = navigationEvents[0] as DashboardNavigationEvent.Progress.OpenProgressDialog
        assertEquals(item.workerId, event.workerId)

        job.cancel()
    }

    @Test
    fun `Upload click for uploading state emits open progress dialog event`() = runTest {
        val viewModel = getViewModel()
        val item = createUploadItem(state = UploadState.UPLOADING)

        val navigationEvents = mutableListOf<DashboardNavigationEvent.Progress>()
        val job = launch(testDispatcher) {
            viewModel.navigationEvents.collect { navigationEvents.add(it) }
        }

        viewModel.uiState.value.onUploadClick(item)
        advanceUntilIdle()

        assertEquals(1, navigationEvents.size)
        val event = navigationEvents[0] as DashboardNavigationEvent.Progress.OpenProgressDialog
        assertEquals(item.workerId, event.workerId)

        job.cancel()
    }

    @Test
    fun `Upload click for failed state emits open progress dialog event`() = runTest {
        val viewModel = getViewModel()
        val item = createUploadItem(state = UploadState.FAILED)

        val navigationEvents = mutableListOf<DashboardNavigationEvent.Progress>()
        val job = launch(testDispatcher) {
            viewModel.navigationEvents.collect { navigationEvents.add(it) }
        }

        viewModel.uiState.value.onUploadClick(item)
        advanceUntilIdle()

        assertEquals(1, navigationEvents.size)
        val event = navigationEvents[0] as DashboardNavigationEvent.Progress.OpenProgressDialog
        assertEquals(item.workerId, event.workerId)

        job.cancel()
    }

    @Test
    fun `Upload dismiss calls dismiss use case`() = runTest {
        val viewModel = getViewModel()
        val item = createUploadItem()

        viewModel.uiState.value.onUploadDismiss(item)
        advanceUntilIdle()

        coVerify { dismissUploadUseCase(item.workerId) }
    }

    @Test
    fun `Sync click emits open sync progress event`() = runTest {
        val viewModel = getViewModel()

        val navigationEvents = mutableListOf<DashboardNavigationEvent.Progress>()
        val job = launch(testDispatcher) {
            viewModel.navigationEvents.collect { navigationEvents.add(it) }
        }

        viewModel.uiState.value.onSyncClick()
        advanceUntilIdle()

        assertEquals(1, navigationEvents.size)
        assertEquals(DashboardNavigationEvent.Progress.OpenSyncProgress, navigationEvents[0])

        job.cancel()
    }

    @Test
    fun `Sync dismiss calls dismiss use case`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSyncDismiss()
        advanceUntilIdle()

        coVerify { dismissSyncProgressUseCase() }
    }

    @Test
    fun `Clear snackbar sets snackbar message to null`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onClearSnackbar()

        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `Updates UI state when uploads flow emits new data`() = runTest {
        val uploadsFlow = kotlinx.coroutines.flow.MutableStateFlow<List<UploadProgressItem>>(emptyList())
        every { observeUploadsUseCase() } returns uploadsFlow

        val viewModel = getViewModel()
        assertEquals(emptyList<UploadProgressItem>(), viewModel.uiState.value.uploadItems)

        val newUploads = listOf(createUploadItem())
        uploadsFlow.value = newUploads

        assertEquals(newUploads, viewModel.uiState.value.uploadItems)
    }

    @Test
    fun `Updates UI state when sync progress flow emits new data`() = runTest {
        val syncFlow = kotlinx.coroutines.flow.MutableStateFlow<SyncProgressItem?>(null)
        every { observeSyncProgressUseCase() } returns syncFlow

        val viewModel = getViewModel()
        assertNull(viewModel.uiState.value.syncProgress)

        val newSyncProgress = createSyncProgressItem()
        syncFlow.value = newSyncProgress

        assertEquals(newSyncProgress, viewModel.uiState.value.syncProgress)
    }

    private fun getViewModel() = ProgressViewModel(
        observeUploadsUseCase,
        observeSyncProgressUseCase,
        dismissUploadUseCase,
        dismissSyncProgressUseCase,
        courseRepository,
        dashboardFileUploadDao,
        apiPrefs
    )

    private fun createUploadItem(
        state: UploadState = UploadState.UPLOADING,
        courseId: Long? = null,
        assignmentId: Long? = null,
        attemptId: Long? = null,
        folderId: Long? = null
    ) = UploadProgressItem(
        workerId = UUID.randomUUID(),
        title = "Test Upload",
        subtitle = "Uploading...",
        progress = 50,
        state = state,
        icon = R.drawable.ic_upload,
        iconBackground = R.color.backgroundInfo,
        courseId = courseId,
        assignmentId = assignmentId,
        attemptId = attemptId,
        folderId = folderId
    )

    private fun createSyncProgressItem() = SyncProgressItem(
        title = "Syncing offline content",
        subtitle = "3 courses",
        progress = 67,
        state = ProgressState.IN_PROGRESS,
        itemCount = 3
    )
}