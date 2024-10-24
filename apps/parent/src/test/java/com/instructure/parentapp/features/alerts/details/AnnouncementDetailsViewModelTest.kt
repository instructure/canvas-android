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
 *
 */

package com.instructure.parentapp.features.alerts.details

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class AnnouncementDetailsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val fileDownloader: FileDownloader = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)

    private val repository: AnnouncementDetailsRepository = mockk(relaxed = true)

    private lateinit var viewModel: AnnouncementDetailsViewModel

    private val courseTestResponse = Course(
        id = 10,
        name = "Course Name"
    )

    private val courseAnnouncementTestResponse = DiscussionTopicHeader(
        id = 1,
        title = "Alert Title",
        message = "Alert Message",
        postedDate = Date.from(
            Instant.parse("2024-01-03T00:00:00Z")
        ),
        attachments = mutableListOf(
            RemoteFile(
                id = 1,
                fileName = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )
    )

    private val globalAnnouncementTestResponse = AccountNotification(
        id = 2,
        subject = "Alert Title",
        message = "Alert Message",
        startAt = "2024-01-03T00:00:00Z"
    )

    private fun createViewModel() {
        viewModel = AnnouncementDetailsViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            repository = repository,
            fileDownloader = fileDownloader,
            parentPrefs = parentPrefs
        )
    }

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        mockkStatic(User::studentColor)

        coEvery { savedStateHandle.get<Long>(AnnouncementDetailsFragment.ANNOUNCEMENT_ID) } returns 1
        coEvery { savedStateHandle.get<Long>(AnnouncementDetailsFragment.COURSE_ID) } returns 10
        coEvery { repository.getCourseAnnouncement(any(), any(), any()) } returns courseAnnouncementTestResponse
        coEvery { repository.getCourse(any(), any()) } returns courseTestResponse
        coEvery { repository.getGlobalAnnouncement(any(), any()) } returns globalAnnouncementTestResponse
        val student = User(id = 55)
        coEvery { parentPrefs.currentStudent } returns student
        coEvery { student.studentColor } returns 1
        coEvery { context.getString(any()) } returns "Global Announcement"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Having Course id gets course announcement`() = runTest {
        val expectedUiState = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Course Name",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            ),
            attachment = Attachment(
                id = 1,
                filename = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )

        createViewModel()
        coVerify { repository.getCourse(10, false) }
        coVerify { repository.getCourseAnnouncement(10, 1, false) }
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Not having Course id gets global announcement`() = runTest {
        val expectedUiState = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Global Announcement",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            )
        )

        coEvery { savedStateHandle.get<Long>(AnnouncementDetailsFragment.COURSE_ID) } returns -1

        createViewModel()
        coVerify(exactly = 0) { repository.getCourse(10, false) }
        coVerify { repository.getGlobalAnnouncement(1, false) }
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Success state if getting course returns null`() = runTest {
        coEvery { repository.getCourse(10, false) } returns null
        createViewModel()
        val expectedUiState = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = null,
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            ),
            attachment = Attachment(
                id = 1,
                filename = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Error state if getting course failed`() = runTest {
        coEvery { repository.getCourse(10, false) } throws Exception()
        createViewModel()
        val expectedUiState = AnnouncementDetailsUiState(
            isError = true,
            studentColor = 1
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Error state if getting course announcement failed`() = runTest {
        coEvery { repository.getCourseAnnouncement(10, 1, false) } throws Exception()
        createViewModel()
        val expectedUiState = AnnouncementDetailsUiState(
            isError = true,
            studentColor = 1
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Error state if getting global announcement failed`() = runTest {
        coEvery { savedStateHandle.get<Long>(AnnouncementDetailsFragment.COURSE_ID) } returns -1
        coEvery { repository.getGlobalAnnouncement(1, false) } throws Exception()
        createViewModel()
        val expectedUiState = AnnouncementDetailsUiState(
            isError = true,
            studentColor = 1
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Refresh after get course failed`() = runTest {
        coEvery { repository.getCourse(10, false) } throws Exception()
        createViewModel()

        coEvery { repository.getCourse(any(), any()) } returns courseTestResponse
        viewModel.handleAction(AnnouncementDetailsAction.Refresh)

        val expectedUiStateRefreshed = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Course Name",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            ),
            attachment = Attachment(
                id = 1,
                filename = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )

        assertEquals(expectedUiStateRefreshed, viewModel.uiState.value)
    }

    @Test
    fun `Refresh after get course announcement failed`() = runTest {
        coEvery { repository.getCourseAnnouncement(10, 1, false) } throws Exception()
        createViewModel()

        coEvery {
            repository.getCourseAnnouncement(
                10,
                1,
                false
            )
        } returns courseAnnouncementTestResponse
        viewModel.handleAction(AnnouncementDetailsAction.Refresh)

        val expectedUiStateRefreshed = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Course Name",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            ),
            attachment = Attachment(
                id = 1,
                filename = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )

        assertEquals(expectedUiStateRefreshed, viewModel.uiState.value)
    }

    @Test
    fun `Refresh after get global announcement failed`() = runTest {
        coEvery { savedStateHandle.get<Long>(AnnouncementDetailsFragment.COURSE_ID) } returns -1
        coEvery { repository.getGlobalAnnouncement(1, false) } throws Exception()
        createViewModel()

        coEvery {
            repository.getGlobalAnnouncement(
                1,
                false
            )
        } returns globalAnnouncementTestResponse
        viewModel.handleAction(AnnouncementDetailsAction.Refresh)

        val expectedUiStateRefreshed = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Global Announcement",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            )
        )

        assertEquals(expectedUiStateRefreshed, viewModel.uiState.value)
    }

    @Test
    fun `When refresh fails while having data, snackbar is shown`() = runTest {
        createViewModel()
        coEvery { repository.getCourseAnnouncement(10, 1, true) } throws Exception()

        viewModel.handleAction(AnnouncementDetailsAction.Refresh)

        val expectedUiStateRefreshed = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Course Name",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            ),
            attachment = Attachment(
                id = 1,
                filename = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            ),
            showErrorSnack = true
        )
        assertEquals(expectedUiStateRefreshed, viewModel.uiState.value)
    }

    @Test
    fun `Dismiss snackbar`() = runTest {
        val expectedUiState = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Course Name",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            ),
            attachment = Attachment(
                id = 1,
                filename = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )

        createViewModel()
        coEvery { repository.getCourseAnnouncement(10, 1, true) } throws Exception()

        viewModel.handleAction(AnnouncementDetailsAction.Refresh)

        viewModel.handleAction(AnnouncementDetailsAction.SnackbarDismissed)
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `File download`() = runTest {
        val expectedUiState = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Course Name",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            ),
            attachment = Attachment(
                id = 1,
                filename = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )

        createViewModel()
        expectedUiState.attachment?.let {
            viewModel.handleAction(AnnouncementDetailsAction.OpenAttachment(it))
            coVerify { fileDownloader.downloadFileToDevice(any()) }
        }
    }
}
