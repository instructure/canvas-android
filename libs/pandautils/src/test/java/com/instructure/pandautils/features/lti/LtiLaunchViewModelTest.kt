/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.lti

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LtiLaunchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: LtiLaunchRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: LtiLaunchViewModel

    @Before
    fun setup() {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns null
        every { savedStateHandle.get<Tab>(LtiLaunchFragment.LTI_TAB) } returns null
        every { savedStateHandle.get<LTITool>(LtiLaunchFragment.LTI_TOOL) } returns null
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns false
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.IS_ASSIGNMENT_LTI) } returns false
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns null
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.OPEN_INTERNALLY) } returns false
        val urlCaptor = slot<String>()
        coEvery { repository.authenticateUrl(capture(urlCaptor)) } answers {
            urlCaptor.captured
        }
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Launch custom tab when lti tool url is successful`() = runTest {
        val ltiTool = LTITool(url = "url")
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery { repository.getLtiFromAuthenticationUrl(any(), any()) } returns ltiTool

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
        assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Load lti into WebView when lti tool url is successful and openInternally is true`() = runTest {
        val ltiTool = LTITool(url = "url")
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.OPEN_INTERNALLY) } returns true
        coEvery { repository.getLtiFromAuthenticationUrl(any(), any()) } returns ltiTool

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(LtiLaunchAction.LoadLtiWebView("url"), events[0])
        assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Load lti into WebView when lti tool is new quizzes`() = runTest {
        val ltiTool = LTITool(url = "quiz-lti")
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "quiz-lti"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery { repository.getLtiFromAuthenticationUrl(any(), any()) } returns ltiTool

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(LtiLaunchAction.LoadLtiWebView("quiz-lti"), events[0])
    }

    @Test
    fun `Show error when lti tool url is null`() = runTest {
        val ltiTool = LTITool(url = null)
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery { repository.getLtiFromAuthenticationUrl(any(), any()) } returns ltiTool

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(LtiLaunchAction.ShowError, events[0])
    }

    @Test
    fun `Show error when lti request fails`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery { repository.getLtiFromAuthenticationUrl(any(), any()) } throws Exception()

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(LtiLaunchAction.ShowError, events[0])
    }

    @Test
    fun `Load lti url from lti tab into WebView when lti tab is present`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        every { savedStateHandle.get<Tab>(LtiLaunchFragment.LTI_TAB) } returns Tab(ltiUrl = "lti-tab-url")
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.OPEN_INTERNALLY) } returns true
        coEvery { repository.getLtiFromAuthenticationUrl("lti-tab-url", any()) } returns LTITool(url = "lti-tab-url")
        coEvery { repository.getLtiFromAuthenticationUrl("url", any()) } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(LtiLaunchAction.LoadLtiWebView("lti-tab-url"), events[0])
    }

    @Test
    fun `Load authenticated lti url when its an assignment lti but not sessionless launch`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.IS_ASSIGNMENT_LTI) } returns true
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.OPEN_INTERNALLY) } returns true
        coEvery { repository.getLtiFromAuthenticationUrl("url", any()) } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify { repository.getLtiFromAuthenticationUrl("url", any()) }
        assertEquals(LtiLaunchAction.LoadLtiWebView("url"), events[0])
    }

    @Test
    fun `Load url but do not get authenticated lti url when its not an assignment lti and not a sessionless launch`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify(exactly = 0) { repository.getLtiFromAuthenticationUrl(any(), any()) }
        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
    }

    @Test
    fun `Display error if lti tab and lti url is null`() = runTest {
        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(LtiLaunchAction.ShowError, events[0])
    }

    @Test
    fun `Replace canvas-courses deeplink`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "canvas-courses://url"
        every { apiPrefs.protocol } returns "https"
        every { apiPrefs.fullDomain } returns "https://domain"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery {
            repository.getLtiFromAuthenticationUrl(
                "https://domain/api/v1/accounts/self/external_tools/sessionless_launch?url=https://url",
                any()
            )
        } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify {
            repository.getLtiFromAuthenticationUrl(
                eq("https://domain/api/v1/accounts/self/external_tools/sessionless_launch?url=https://url"),
                any()
            )
        }
        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
    }

    @Test
    fun `Replace canvas-student deeplink`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "canvas-student://url"
        every { apiPrefs.protocol } returns "https"
        every { apiPrefs.fullDomain } returns "https://domain"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery {
            repository.getLtiFromAuthenticationUrl(
                "https://domain/api/v1/accounts/self/external_tools/sessionless_launch?url=https://url",
                any()
            )
        } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify {
            repository.getLtiFromAuthenticationUrl(
                eq("https://domain/api/v1/accounts/self/external_tools/sessionless_launch?url=https://url"),
                any()
            )
        }
        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
    }

    @Test
    fun `Create correct url for course context if its a sessionless launch`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns CanvasContext.emptyCourseContext(id = 1)
        every { apiPrefs.protocol } returns "https"
        every { apiPrefs.fullDomain } returns "https://domain"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery {
            repository.getLtiFromAuthenticationUrl(
                "https://domain/api/v1/courses/1/external_tools/sessionless_launch?url=url",
                any()
            )
        } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify { repository.getLtiFromAuthenticationUrl(eq("https://domain/api/v1/courses/1/external_tools/sessionless_launch?url=url"), any()) }
        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
    }

    @Test
    fun `Create correct url for group context if its a sessionless launch`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns CanvasContext.emptyGroupContext(id = 1)
        every { apiPrefs.protocol } returns "https"
        every { apiPrefs.fullDomain } returns "https://domain"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery {
            repository.getLtiFromAuthenticationUrl(
                "https://domain/api/v1/groups/1/external_tools/sessionless_launch?url=url",
                any()
            )
        } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify { repository.getLtiFromAuthenticationUrl(eq("https://domain/api/v1/groups/1/external_tools/sessionless_launch?url=url"), any()) }
        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
    }

    @Test
    fun `Create correct url for course context if its a sessionless launch and url has an external tool id`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "https://a/external_tools/55"
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns CanvasContext.emptyCourseContext(id = 1)
        every { apiPrefs.protocol } returns "https"
        every { apiPrefs.fullDomain } returns "https://domain"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery {
            repository.getLtiFromAuthenticationUrl(
                "https://domain/api/v1/courses/1/external_tools/sessionless_launch?id=55",
                any()
            )
        } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify { repository.getLtiFromAuthenticationUrl(eq("https://domain/api/v1/courses/1/external_tools/sessionless_launch?id=55"), any()) }
        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
    }

    @Test
    fun `Create correct url for group context if its a sessionless launch and url has an external tool id`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "https://a/external_tools/55"
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns CanvasContext.emptyGroupContext(id = 1)
        every { apiPrefs.protocol } returns "https"
        every { apiPrefs.fullDomain } returns "https://domain"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery {
            repository.getLtiFromAuthenticationUrl(
                "https://domain/api/v1/groups/1/external_tools/sessionless_launch?id=55",
                any()
            )
        } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify { repository.getLtiFromAuthenticationUrl(eq("https://domain/api/v1/groups/1/external_tools/sessionless_launch?id=55"), any()) }
        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
    }

    @Test
    fun `Create correct url for user context if its a sessionless launch and url has an external tool id`() = runTest {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "https://a/external_tools/55"
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns CanvasContext.emptyUserContext()
        every { apiPrefs.protocol } returns "https"
        every { apiPrefs.fullDomain } returns "https://domain"
        every { savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) } returns true
        coEvery {
            repository.getLtiFromAuthenticationUrl(
                "https://domain/api/v1/accounts/self/external_tools/sessionless_launch?id=55",
                any()
            )
        } returns LTITool(url = "url")

        viewModel = createViewModel()

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coVerify { repository.getLtiFromAuthenticationUrl(eq("https://domain/api/v1/accounts/self/external_tools/sessionless_launch?id=55"), any()) }
        assertEquals(LtiLaunchAction.LaunchCustomTab("url"), events[0])
    }

    private fun createViewModel() = LtiLaunchViewModel(savedStateHandle, repository, apiPrefs)
}