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
//package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.file
//
//import androidx.work.WorkManager
//import androidx.work.WorkRequest
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//import com.instructure.horizon.features.moduleitemsequence.content.assignment.FileItem
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
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class FileSubmissionContentViewModelTest {
//    private val workManager: WorkManager = mockk(relaxed = true)
//    private val fileDownloadProgressDao: FileDownloadProgressDao = mockk(relaxed = true)
//    private val fileCache: FileCache = mockk(relaxed = true)
//    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
//    private val testDispatcher = UnconfinedTestDispatcher()
//
//    private val testFiles = listOf(
//        FileItem(
//            fileId = 1L,
//            fileName = "file1.pdf",
//            fileUrl = "https://example.com/file1.pdf",
//            fileType = "application/pdf",
//            thumbnailUrl = "https://example.com/thumb1.jpg"
//        ),
//        FileItem(
//            fileId = 2L,
//            fileName = "file2.pdf",
//            fileUrl = "https://example.com/file2.pdf",
//            fileType = "application/pdf",
//            thumbnailUrl = "https://example.com/thumb2.jpg"
//        )
//    )
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(null)
//        coEvery { fileDownloadProgressDao.deleteByWorkerId(any()) } returns Unit
//        coEvery { fileCache.awaitFileDownload(any()) } returns mockk(relaxed = true)
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
//    fun `Test ViewModel initializes with empty state`() = runTest {
//        val viewModel = getViewModel()
//
//        assertTrue(viewModel.uiState.value.files.isEmpty())
//    }
//
////    @Test
////    fun `Test set initial data creates file items`() = runTest {
////        val viewModel = getViewModel()
////
////        viewModel.setInitialData(testFiles)
////
////        assertEquals(2, viewModel.uiState.value.files.size)
////        assertEquals("file1.pdf", viewModel.uiState.value.files.first().fileName)
////        assertTrue(viewModel.uiState.value.files.first().selected)
////        assertFalse(viewModel.uiState.value.files[1].selected)
////    }
//
//    @Test
//    fun `Test file click changes selection`() = runTest {
//        val viewModel = getViewModel()
//        viewModel.setInitialData(testFiles)
//
//        viewModel.uiState.value.files[1].onClick()
//
//        assertFalse(viewModel.uiState.value.files.first().selected)
//        assertTrue(viewModel.uiState.value.files[1].selected)
//    }
//
//    @Test
//    fun `Test download file starts download`() = runTest {
//        val viewModel = getViewModel()
//        viewModel.setInitialData(testFiles)
//
//        val fileItem = viewModel.uiState.value.files.first()
//        fileItem.onDownloadClick(fileItem)
//
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
//            fileName = "fileName"
//        )
//        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(progressEntity)
//
//        val viewModel = getViewModel()
//        viewModel.setInitialData(testFiles)
//
//        val fileItem = viewModel.uiState.value.files.first()
//        fileItem.onDownloadClick(fileItem)
//
//        assertEquals(FileDownloadProgressState.IN_PROGRESS, viewModel.uiState.value.files.first().downloadState)
//        assertEquals(0.5f, viewModel.uiState.value.files.first().downloadProgress)
//    }
//
////    @Test
////    fun `Test download completion sets file path to open`() = runTest {
////        val completedEntity = FileDownloadProgressEntity(
////            workerId = "worker-id",
////            progressState = FileDownloadProgressState.COMPLETED,
////            progress = 100,
////            filePath = "/path/to/file",
////            fileName = "fileName"
////        )
////        coEvery { fileDownloadProgressDao.findByWorkerIdFlow(any()) } returns flowOf(completedEntity)
////
////        val viewModel = getViewModel()
////        viewModel.setInitialData(testFiles)
////
////        val fileItem = viewModel.uiState.value.files.first()
////        fileItem.onDownloadClick(fileItem)
////
////        assertEquals("/path/to/file", viewModel.uiState.value.filePathToOpen)
////        assertEquals("application/pdf", viewModel.uiState.value.mimeTypeToOpen)
////    }
//
//    @Test
//    fun `Test cancel download cancels work`() = runTest {
//        val viewModel = getViewModel()
//        viewModel.setInitialData(testFiles)
//
//        val fileItem = viewModel.uiState.value.files.first()
//        fileItem.onDownloadClick(fileItem)
//        fileItem.onCancelDownloadClick(fileItem.fileId)
//
//        coVerify { workManager.cancelWorkById(any()) }
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
//        viewModel.setInitialData(testFiles)
//
//        val fileItem = viewModel.uiState.value.files.first()
//        fileItem.onDownloadClick(fileItem)
//
//        viewModel.uiState.value.onFileOpened()
//
//        assertNull(viewModel.uiState.value.filePathToOpen)
//        assertNull(viewModel.uiState.value.mimeTypeToOpen)
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
//        viewModel.setInitialData(testFiles)
//
//        val fileItem = viewModel.uiState.value.files.first()
//        fileItem.onDownloadClick(fileItem)
//
//        coVerify { fileDownloadProgressDao.deleteByWorkerId("worker-id") }
//    }
//
//    @Test
//    fun `Test file items maintain correct IDs`() = runTest {
//        val viewModel = getViewModel()
//        viewModel.setInitialData(testFiles)
//
//        assertEquals(1L, viewModel.uiState.value.files.first().fileId)
//        assertEquals(2L, viewModel.uiState.value.files[1].fileId)
//    }
//
//    @Test
//    fun `Test empty file list`() = runTest {
//        val viewModel = getViewModel()
//
//        viewModel.setInitialData(emptyList())
//
//        assertTrue(viewModel.uiState.value.files.isEmpty())
//    }
//
//    private fun getViewModel(): FileSubmissionContentViewModel {
//        return FileSubmissionContentViewModel(
//            workManager,
//            fileDownloadProgressDao,
//            fileCache,
//            crashlytics
//        )
//    }
//}
