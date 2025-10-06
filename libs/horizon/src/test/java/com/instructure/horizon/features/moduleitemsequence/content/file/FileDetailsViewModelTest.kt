///*
// * Copyright (C) 2025 - present Instructure, Inc.
// *
// *     This program is free software: you can redistribute it and/or modify
// *     it under the terms of the GNU General Public License as published by
// *     the Free Software Foundation, version 3 of the License.
// *
// *     This program is distributed in the hope that it will be useful,
// *     but WITHOUT ANY WARRANTY; without even the implied warranty of
// *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *     GNU General Public License for more details.
// *
// *     You should have received a copy of the GNU General Public License
// *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//package com.instructure.horizon.features.moduleitemsequence.content.file
//
//import androidx.lifecycle.SavedStateHandle
//import androidx.work.WorkManager
//import androidx.work.WorkRequest
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//import com.instructure.canvasapi2.models.FileFolder
//import com.instructure.horizon.features.account.filepreview.FilePreviewUiState
//import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
//import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
//import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
//import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
//import com.instructure.pandautils.utils.filecache.FileCache
//import com.instructure.pandautils.utils.filecache.awaitFileDownload
//import io.mockk.coEvery
//import io.mockk.coVerify
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.unmockkAll
//import junit.framework.TestCase.assertEquals
//import junit.framework.TestCase.assertFalse
//import junit.framework.TestCase.assertNull
//import junit.framework.TestCase.assertTrue
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.UnconfinedTestDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import java.io.File
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class FileDetailsViewModelTest {
//    private val repository: FileDetailsRepository = mockk(relaxed = true)
//    private val workManager: WorkManager = mockk(relaxed = true)
//    private val fileDownloadProgressDao: FileDownloadProgressDao = mockk(relaxed = true)
//    private val fileCache: FileCache = mockk(relaxed = true)
//    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
//    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
//    private val testDispatcher = UnconfinedTestDispatcher()
//
//    private val fileUrl = "https://example.com/files/123"
//    private val testFile = FileFolder(
//        id = 123L,
//        displayName = "test.pdf",
//        url = fileUrl,
//        contentType = "application/pdf",
//        thumbnailUrl = "https://example.com/thumb.jpg"
//    )
//
//    private val mockFile: File = mockk(relaxed = true)
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//        every { savedStateHandle.get<String>(ModuleItemContent.File.FILE_URL) } returns fileUrl
//        coEvery { repository.getFileFolderFromURL(any()) } returns testFile
//        coEvery { repository.getAuthenticatedFileUrl(any()) } returns "https://authenticated.url"
//        coEvery { fileCache.awaitFileDownload(any(), any()) } returns mockFile
//        every { mockFile.absolutePath } returns "/path/to/file"
//        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(null)
//        coEvery { fileDownloadProgressDao.deleteByWorkerId(any()) } returns Unit
//        every { workManager.enqueue(any<WorkRequest>()) } returns mockk {
//            every { result } returns mockk()
//        }
//        every { crashlytics.recordException(any()) } returns Unit
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//        unmockkAll()
//    }
//
//    @Test
//    fun `Test ViewModel loads file details`() = runTest {
//        val viewModel = getViewModel()
//
//        assertFalse(viewModel.uiState.value.loadingState.isLoading)
//        assertEquals("test.pdf", viewModel.uiState.value.fileName)
//        assertEquals("application/pdf", viewModel.uiState.value.mimeType)
//        coVerify { repository.getFileFolderFromURL(fileUrl) }
//    }
//
//    @Test
//    fun `Test PDF file generates PDF preview`() = runTest {
//        val viewModel = getViewModel()
//
//        assertTrue(viewModel.uiState.value.filePreview is FilePreviewUiState.Pdf)
//        coVerify { fileCache.awaitFileDownload(fileUrl) }
//    }
//
//    @Test
//    fun `Test video file generates media preview`() = runTest {
//        val videoFile = testFile.copy(contentType = "video/mp4")
//        coEvery { repository.getFileFolderFromURL(any()) } returns videoFile
//
//        val viewModel = getViewModel()
//
//        assertTrue(viewModel.uiState.value.filePreview is FilePreviewUiState.Media)
//    }
//
//    @Test
//    fun `Test image file generates image preview`() = runTest {
//        val imageFile = testFile.copy(contentType = "image/png")
//        coEvery { repository.getFileFolderFromURL(any()) } returns imageFile
//
//        val viewModel = getViewModel()
//
//        assertTrue(viewModel.uiState.value.filePreview is FilePreviewUiState.Image)
//    }
//
//    @Test
//    fun `Test unknown file type generates webview preview`() = runTest {
//        val unknownFile = testFile.copy(contentType = "application/unknown")
//        coEvery { repository.getFileFolderFromURL(any()) } returns unknownFile
//
//        val viewModel = getViewModel()
//
//        assertTrue(viewModel.uiState.value.filePreview is FilePreviewUiState.WebView)
//    }
//
//    @Test
//    fun `Test download button starts download`() = runTest {
//        val viewModel = getViewModel()
//
//        viewModel.uiState.value.onDownloadClicked()
//
//        assertEquals(FileDownloadProgressState.STARTING, viewModel.uiState.value.downloadState)
//        coVerify { workManager.enqueue(any<WorkRequest>()) }
//    }
//
//    @Test
//    fun `Test download progress updates state`() = runTest {
//        val progressEntity = FileDownloadProgressEntity(
//            workerId = "worker-id",
//            progressState = FileDownloadProgressState.IN_PROGRESS,
//            progress = 50,
//            filePath = "/path/to/file",
//            fileName = "filename"
//        )
//        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(progressEntity)
//
//        val viewModel = getViewModel()
//        viewModel.uiState.value.onDownloadClicked()
//
//        assertEquals(0.5f, viewModel.uiState.value.downloadProgress)
//        assertEquals(FileDownloadProgressState.IN_PROGRESS, viewModel.uiState.value.downloadState)
//    }
//
//    @Test
//    fun `Test download completion sets file path to open`() = runTest {
//        val completedEntity = FileDownloadProgressEntity(
//            workerId = "worker-id",
//            progressState = FileDownloadProgressState.COMPLETED,
//            progress = 100,
//            filePath = "/path/to/downloaded/file",
//            fileName = "filename"
//        )
//        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(completedEntity)
//
//        val viewModel = getViewModel()
//        viewModel.uiState.value.onDownloadClicked()
//
//        assertEquals("/path/to/downloaded/file", viewModel.uiState.value.filePathToOpen)
//    }
//
//    @Test
//    fun `Test file opened clears file path`() = runTest {
//        val completedEntity = FileDownloadProgressEntity(
//            workerId = "worker-id",
//            progressState = FileDownloadProgressState.COMPLETED,
//            progress = 100,
//            filePath = "/path/to/file",
//            fileName = "fileName"
//        )
//        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(completedEntity)
//
//        val viewModel = getViewModel()
//        viewModel.uiState.value.onDownloadClicked()
//
//        viewModel.uiState.value.onFileOpened()
//
//        assertNull(viewModel.uiState.value.filePathToOpen)
//    }
//
//    @Test
//    fun `Test cancel download cancels work`() = runTest {
//        val viewModel = getViewModel()
//        viewModel.uiState.value.onDownloadClicked()
//
//        viewModel.uiState.value.onCancelDownloadClicked()
//
//        coVerify { workManager.cancelWorkById(any()) }
//    }
//
//    @Test
//    fun `Test download progress cleanup on completion`() = runTest {
//        val completedEntity = FileDownloadProgressEntity(
//            workerId = "worker-id",
//            progressState = FileDownloadProgressState.COMPLETED,
//            progress = 100,
//            filePath = "/path/to/file",
//            fileName = "fileName"
//        )
//        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(completedEntity)
//
//        val viewModel = getViewModel()
//        viewModel.uiState.value.onDownloadClicked()
//
//        coVerify { fileDownloadProgressDao.deleteByWorkerId("worker-id") }
//    }
//
//    @Test
//    fun `Test file load error sets error state`() = runTest {
//        coEvery { repository.getFileFolderFromURL(any()) } throws Exception("Error")
//
//        val viewModel = getViewModel()
//
//        assertFalse(viewModel.uiState.value.loadingState.isLoading)
//        assertTrue(viewModel.uiState.value.loadingState.isError)
//    }
//
//    @Test
//    fun `Test preview generation error shows no preview`() = runTest {
//        coEvery { fileCache.awaitFileDownload(any()) } throws Exception("Download error")
//
//        val viewModel = getViewModel()
//
//        assertTrue(viewModel.uiState.value.filePreview is FilePreviewUiState.NoPreview)
//        coVerify { crashlytics.recordException(any()) }
//    }
//
//    @Test
//    fun `Test download error sets error state`() = runTest {
//        val errorEntity = FileDownloadProgressEntity(
//            workerId = "worker-id",
//            progressState = FileDownloadProgressState.ERROR,
//            progress = 0,
//            filePath = "",
//            fileName = ""
//        )
//        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(errorEntity)
//
//        val viewModel = getViewModel()
//        viewModel.uiState.value.onDownloadClicked()
//
//        assertEquals(FileDownloadProgressState.ERROR, viewModel.uiState.value.downloadState)
//    }
//
//    @Test
//    fun `Test authenticated URL is requested`() = runTest {
//        val viewModel = getViewModel()
//
//        coVerify { repository.getAuthenticatedFileUrl(any()) }
//    }
//
//    @Test
//    fun `Test file cache download is null shows no preview`() = runTest {
//        coEvery { fileCache.awaitFileDownload(any()) } returns null
//
//        val viewModel = getViewModel()
//
//        assertTrue(viewModel.uiState.value.filePreview is FilePreviewUiState.NoPreview)
//    }
//
//    private fun getViewModel(): FileDetailsViewModel {
//        return FileDetailsViewModel(
//            repository,
//            workManager,
//            fileDownloadProgressDao,
//            fileCache,
//            crashlytics,
//            savedStateHandle
//        )
//    }
//}
