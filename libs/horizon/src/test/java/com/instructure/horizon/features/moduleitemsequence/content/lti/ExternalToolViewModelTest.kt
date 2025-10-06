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
//package com.instructure.horizon.features.moduleitemsequence.content.lti
//
//import android.net.Uri
//import androidx.lifecycle.SavedStateHandle
//import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
//import com.instructure.pandautils.utils.Const
//import io.mockk.coEvery
//import io.mockk.coVerify
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.mockkStatic
//import io.mockk.unmockkAll
//import junit.framework.TestCase.assertEquals
//import junit.framework.TestCase.assertFalse
//import junit.framework.TestCase.assertNotNull
//import junit.framework.TestCase.assertNull
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.UnconfinedTestDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class ExternalToolViewModelTest {
//    private val repository: ExternalToolRepository = mockk(relaxed = true)
//    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
//    private val testDispatcher = UnconfinedTestDispatcher()
//
//    private val courseId = 1L
//    private val url = "https://example.com/lti/tool"
//    private val externalUrl = "https://external.com/preview"
//    private val encodedUrl = Uri.encode(url)
//    private val encodedExternalUrl = Uri.encode(externalUrl)
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//        mockkStatic(Uri::class)
//        every { Uri.decode(encodedUrl) } returns url
//        every { Uri.decode(encodedExternalUrl) } returns externalUrl
//        every { Uri.decode("") } returns ""
//        every { savedStateHandle.get<Long>(Const.COURSE_ID) } returns courseId
//        every { savedStateHandle.get<String>(ModuleItemContent.ExternalTool.URL) } returns encodedUrl
//        every { savedStateHandle.get<String>(ModuleItemContent.ExternalTool.EXTERNAL_URL) } returns encodedExternalUrl
//        coEvery { repository.authenticateUrl(any()) } returns "https://authenticated.url"
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//        unmockkAll()
//    }
//
//    @Test
//    fun `Test ViewModel initializes with preview and URL`() = runTest {
//        val viewModel = getViewModel()
//
//        assertEquals(externalUrl, viewModel.uiState.value.previewUrl)
//        assertEquals(url, viewModel.uiState.value.urlToOpen)
//        assertEquals(PreviewState.LOADING, viewModel.uiState.value.previewState)
//    }
//
//    @Test
//    fun `Test open externally authenticates URL`() = runTest {
//        val viewModel = getViewModel()
//
//        viewModel.uiState.value.onOpenExternallyClicked()
//
//        assertEquals("https://authenticated.url", viewModel.uiState.value.authenticatedUrl)
//        assertFalse(viewModel.uiState.value.openExternallyLoading)
//        coVerify { repository.authenticateUrl(url) }
//    }
//
//    @Test
//    fun `Test open externally handles authentication error`() = runTest {
//        coEvery { repository.authenticateUrl(any()) } throws Exception("Auth error")
//
//        val viewModel = getViewModel()
//
//        viewModel.uiState.value.onOpenExternallyClicked()
//
//        assertNull(viewModel.uiState.value.authenticatedUrl)
//        assertFalse(viewModel.uiState.value.openExternallyLoading)
//    }
//
//    @Test
//    fun `Test preview error sets error state`() = runTest {
//        val viewModel = getViewModel()
//
//        viewModel.uiState.value.onPreviewError()
//
//        assertEquals(PreviewState.ERROR, viewModel.uiState.value.previewState)
//    }
//
//    @Test
//    fun `Test page finished sets success state`() = runTest {
//        val viewModel = getViewModel()
//
//        viewModel.uiState.value.onPageFinished()
//
//        assertEquals(PreviewState.SUCCESS, viewModel.uiState.value.previewState)
//    }
//
//    @Test
//    fun `Test page finished after error does not change state`() = runTest {
//        val viewModel = getViewModel()
//
//        viewModel.uiState.value.onPreviewError()
//        viewModel.uiState.value.onPageFinished()
//
//        assertEquals(PreviewState.ERROR, viewModel.uiState.value.previewState)
//    }
//
//    @Test
//    fun `Test link opened clears authenticated URL`() = runTest {
//        val viewModel = getViewModel()
//        viewModel.uiState.value.onOpenExternallyClicked()
//
//        viewModel.uiState.value.onLinkOpened()
//
//        assertNull(viewModel.uiState.value.authenticatedUrl)
//    }
//
//    @Test
//    fun `Test open externally loading state`() = runTest {
//        coEvery { repository.authenticateUrl(any()) } coAnswers {
//            kotlinx.coroutines.delay(100)
//            "https://authenticated.url"
//        }
//
//        val viewModel = getViewModel()
//
//        viewModel.uiState.value.onOpenExternallyClicked()
//        // Due to UnconfinedTestDispatcher, loading completes immediately
//        assertFalse(viewModel.uiState.value.openExternallyLoading)
//    }
//
//    @Test
//    fun `Test UI state contains all callbacks`() = runTest {
//        val viewModel = getViewModel()
//
//        assertNotNull(viewModel.uiState.value.onOpenExternallyClicked)
//        assertNotNull(viewModel.uiState.value.onPreviewError)
//        assertNotNull(viewModel.uiState.value.onPageFinished)
//        assertNotNull(viewModel.uiState.value.onLinkOpened)
//    }
//
//    @Test
//    fun `Test empty URL parameters default to empty string`() = runTest {
//        every { savedStateHandle.get<String>(ModuleItemContent.ExternalTool.URL) } returns null
//        every { savedStateHandle.get<String>(ModuleItemContent.ExternalTool.EXTERNAL_URL) } returns null
//
//        val viewModel = getViewModel()
//
//        assertEquals("", viewModel.uiState.value.urlToOpen)
//        assertEquals("", viewModel.uiState.value.previewUrl)
//    }
//
//    private fun getViewModel(): ExternalToolViewModel {
//        return ExternalToolViewModel(repository, savedStateHandle)
//    }
//}
