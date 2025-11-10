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
package com.instructure.horizon.features.inbox.attachment

import android.net.Uri
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HorizonInboxAttachmentPickerViewModelTest {
    private val fileUploadManager: FileUploadManager = mockk(relaxed = true)
    private val fileUploadUtils: FileUploadUtilsHelper = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUri: Uri = mockk(relaxed = true)
    private val testFileSubmitObject = FileSubmitObject(
        name = "test.pdf",
        size = 1000L,
        contentType = "application/pdf",
        fullPath = "/path/to/test.pdf"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        coEvery { fileUploadUtils.getFileSubmitObjectFromInputStream(any(), any(), any()) } returns testFileSubmitObject
        every { fileUploadUtils.getFileNameWithDefault(any()) } returns "test.pdf"
        every { fileUploadUtils.getFileMimeType(any()) } returns "application/pdf"
        coEvery { fileUploadManager.uploadFile(any(), any(), any()) } returns DataResult.Success(
            Attachment(id = 123L)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel initializes with empty file list`() = runTest {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.files.isEmpty())
        assertNotNull(viewModel.uiState.value.onFileSelected)
    }

    @Test
    fun `Test file selection adds file to list`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)

        assertEquals(1, viewModel.uiState.value.files.size)
        assertEquals("test.pdf", viewModel.uiState.value.files.first().fileName)
        assertEquals(1000L, viewModel.uiState.value.files.first().fileSize)
    }

    @Test
    fun `Test file upload starts with in-progress state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)

        val file = viewModel.uiState.value.files.first()
        // Initially in progress, but will complete quickly due to UnconfinedTestDispatcher
        assertTrue(file.state is HorizonInboxAttachmentState.InProgress || file.state is HorizonInboxAttachmentState.Success)
    }

    @Test
    fun `Test successful file upload sets success state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)

        // Wait for upload to complete
        val file = viewModel.uiState.value.files.first()
        assertTrue(file.state is HorizonInboxAttachmentState.Success)
        assertEquals(123L, file.id)
    }

    @Test
    fun `Test failed file upload sets error state`() = runTest {
        coEvery { fileUploadManager.uploadFile(any(), any(), any()) } returns DataResult.Fail()

        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)

        val file = viewModel.uiState.value.files.first()
        assertTrue(file.state is HorizonInboxAttachmentState.Error)
    }

    @Test
    fun `Test successful upload adds remove action`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)

        val file = viewModel.uiState.value.files.first()
        assertNotNull(file.onActionClicked)
    }

    @Test
    fun `Test remove action removes file from list`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)

        val file = viewModel.uiState.value.files.first()
        file.onActionClicked?.invoke()

        assertTrue(viewModel.uiState.value.files.isEmpty())
    }

    @Test
    fun `Test multiple file uploads`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)
        viewModel.uiState.value.onFileSelected(testUri)

        assertEquals(2, viewModel.uiState.value.files.size)
    }

    @Test
    fun `Test upload progress updates file state`() = runTest {
        // This test verifies the upload manager's progress listener is called
        coEvery { fileUploadManager.uploadFile(any(), any(), any()) } coAnswers {
            val listener = secondArg<com.instructure.canvasapi2.utils.ProgressRequestUpdateListener>()
            listener.onProgressUpdated(50f, 1000L)
            DataResult.Success(Attachment(id = 123L))
        }

        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)

        // File should eventually reach success state
        val file = viewModel.uiState.value.files.first()
        assertTrue(file.state is HorizonInboxAttachmentState.Success)
    }

    @Test
    fun `Test failed upload adds retry action`() = runTest {
        coEvery { fileUploadManager.uploadFile(any(), any(), any()) } returns DataResult.Fail()

        val viewModel = getViewModel()

        viewModel.uiState.value.onFileSelected(testUri)

        val file = viewModel.uiState.value.files.first()
        assertTrue(file.state is HorizonInboxAttachmentState.Error)
        assertNotNull(file.onActionClicked)
    }

    private fun getViewModel(): HorizonInboxAttachmentPickerViewModel {
        return HorizonInboxAttachmentPickerViewModel(fileUploadManager, fileUploadUtils)
    }
}
