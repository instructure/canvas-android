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

package com.instructure.parentapp.features.webview

import android.webkit.CookieManager
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.toJson
import com.instructure.parentapp.util.navigation.Navigation
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.collectForTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimpleWebViewViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val repository: SimpleWebViewRepository = mockk(relaxed = true)
    private val cookieManager: CookieManager = mockk(relaxed = true)
    private val fileDownloader: FileDownloader = mockk(relaxed = true)

    private lateinit var viewModel: SimpleWebViewViewModel

    @Before
    fun setup() {
        every { savedStateHandle.get<String>(Const.URL) } returns "test-url"
        every { savedStateHandle.get<String>(Navigation.INITIAL_COOKIES) } returns null
    }

    @Test
    fun `Load WebView with cookies when cookies are provided`() = runTest {
        every { savedStateHandle.get<String>(Navigation.INITIAL_COOKIES) } returns mapOf("key" to "value").toJson()
        every { apiPrefs.user?.limitParentAppWebAccess() } returns false
        coEvery { repository.getAuthenticatedSession("test-url") } returns "session-url"

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        verify { cookieManager.setCookie("session-url", "key=value") }
        assertEquals(SimpleWebViewAction.LoadWebView("session-url", false), events.last())
        assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Load WebView when web access is limited`() = runTest {
        every { apiPrefs.user?.limitParentAppWebAccess() } returns true
        coEvery { repository.getAuthenticatedSession("test-url") } returns "session-url"

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        assertEquals(SimpleWebViewAction.LoadWebView("session-url", true), events.last())
        assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Launch custom tab when no cookies are provided and web access not limited`() = runTest {
        every { apiPrefs.user?.limitParentAppWebAccess() } returns false
        coEvery { repository.getAuthenticatedSession("test-url") } returns "session-url"

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        assertEquals(SimpleWebViewAction.LaunchCustomTab("session-url"), events.last())
        assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Show error when repository call fails`() = runTest {
        coEvery { repository.getAuthenticatedSession(any()) } throws IllegalStateException()

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        assertEquals(SimpleWebViewAction.ShowError, events.last())
    }

    @Test
    fun `Download file using fileDownloader`() = runTest {
        val mimeType = "application/pdf"
        val fileUrl = "https://example.com/file.pdf"
        val fileName = "example.pdf"

        createViewModel()

        viewModel.downloadFile(mimeType, fileUrl, fileName)

        verify { fileDownloader.downloadFileToDevice(fileUrl, fileName, mimeType) }
    }

    private fun createViewModel() {
        viewModel = SimpleWebViewViewModel(savedStateHandle, apiPrefs, repository, cookieManager, fileDownloader)
    }
}
