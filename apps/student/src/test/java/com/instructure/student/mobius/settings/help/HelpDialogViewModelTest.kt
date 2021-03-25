/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.student.mobius.settings.help

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.HelpLinksManager
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.models.HelpLinks
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.PackageInfoProvider
import com.instructure.student.R
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HelpDialogViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val helpLinksManager: HelpLinksManager = mockk(relaxed = true)
    private val courseManager: CourseManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val packageInfoProvider: PackageInfoProvider = mockk(relaxed = true)

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private lateinit var viewModel: HelpDialogViewModel

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `Show error state if help links failed`() {
        // Given
        every { helpLinksManager.getHelpLinksAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        viewModel = HelpDialogViewModel(helpLinksManager, courseManager, context, apiPrefs, packageInfoProvider)
        viewModel.state.observe(lifecycleOwner, Observer {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Error)
    }

    @Test
    fun `Create links from custom links if it's not empty`() {
        // Given
        val customLinks = listOf(createHelpLink(listOf("student"), "Custom link"))
        val defaultLinks = listOf(createHelpLink(listOf("student"), "Default link"))
        val helpLinks = HelpLinks(customLinks, defaultLinks)

        every { helpLinksManager.getHelpLinksAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(helpLinks)
        }

        every { courseManager.getAllFavoriteCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        // When
        viewModel = HelpDialogViewModel(helpLinksManager, courseManager, context, apiPrefs, packageInfoProvider)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val linksViewData = viewModel.data.value?.helpLinks ?: emptyList()
        assertEquals(2, linksViewData.size) // We will have 2 links because we also add the store rating link manually
        assertEquals("Custom link", linksViewData[0].helpLinkViewData.title)
    }

    @Test
    fun `Create links from default links if custom links is not empty`() {
        // Given
        val defaultLinks = listOf(createHelpLink(listOf("student"), "Default link"))
        val helpLinks = HelpLinks(emptyList(), defaultLinks)

        every { helpLinksManager.getHelpLinksAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(helpLinks)
        }

        every { courseManager.getAllFavoriteCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        // When
        viewModel = HelpDialogViewModel(helpLinksManager, courseManager, context, apiPrefs, packageInfoProvider)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val linksViewData = viewModel.data.value?.helpLinks ?: emptyList()
        assertEquals(2, linksViewData.size) // We will have 2 links because we also add the store rating link manually
        assertEquals("Default link", linksViewData[0].helpLinkViewData.title)
    }

    @Test
    fun `Don't include links that are only available for teachers`() {
        // Given
        val defaultLinks = listOf(
            createHelpLink(listOf("student"), "Student link"),
            createHelpLink(listOf("user"), "User link"),
            createHelpLink(listOf("student", "teacher"), "Student and Teacher link"),
            createHelpLink(listOf("teacher"), "Teacher link")
        )
        val helpLinks = HelpLinks(emptyList(), defaultLinks)

        every { helpLinksManager.getHelpLinksAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(helpLinks)
        }

        every { courseManager.getAllFavoriteCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { context.getString(R.string.shareYourLove) } returns "Share your love title"

        // When
        viewModel = HelpDialogViewModel(helpLinksManager, courseManager, context, apiPrefs, packageInfoProvider)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val linksViewData = viewModel.data.value?.helpLinks ?: emptyList()
        assertEquals(4, linksViewData.size) // We will have 4 links because we also add the store rating link manually
        assertEquals("Student link", linksViewData[0].helpLinkViewData.title)
        assertEquals("User link", linksViewData[1].helpLinkViewData.title)
        assertEquals("Student and Teacher link", linksViewData[2].helpLinkViewData.title)
        assertEquals("Share your love title", linksViewData[3].helpLinkViewData.title)
    }

    @Test
    fun `Map links correctly with action`() {
        // Given
        val defaultLinks = listOf(
            createHelpLink(listOf("student"), "Create ticket", url = "#create_ticket"),
            createHelpLink(listOf("student"), "Tel 0123456", url = "tel:0123456"),
            createHelpLink(listOf("student"), "Send mail", url = "mailto:yoda@jedicouncil.com"),
            createHelpLink(listOf("student"), "External browser", url = "cases.canvaslms.com/liveagentchat"),
            createHelpLink(listOf("student"), "WebView", url = "google.com"),
        )
        val helpLinks = HelpLinks(emptyList(), defaultLinks)

        every { helpLinksManager.getHelpLinksAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(helpLinks)
        }

        every { courseManager.getAllFavoriteCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { context.getString(R.string.shareYourLove) } returns "Share your love title"

        // When
        viewModel = HelpDialogViewModel(helpLinksManager, courseManager, context, apiPrefs, packageInfoProvider)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val linksViewData = viewModel.data.value?.helpLinks ?: emptyList()
        assertEquals(6, linksViewData.size) // We will have 4 links because we also add the store rating link manually
        assertEquals(HelpLinkViewData("Create ticket", "", HelpDialogAction.ReportProblem), linksViewData[0].helpLinkViewData)
        assertEquals(HelpLinkViewData("Tel 0123456", "", HelpDialogAction.Phone("tel:0123456")), linksViewData[1].helpLinkViewData)
        assertEquals(HelpLinkViewData("Send mail", "", HelpDialogAction.SendMail("mailto:yoda@jedicouncil.com")), linksViewData[2].helpLinkViewData)
        assertEquals(HelpLinkViewData("External browser", "", HelpDialogAction.OpenExternalBrowser("cases.canvaslms.com/liveagentchat")), linksViewData[3].helpLinkViewData)
        assertEquals(HelpLinkViewData("WebView", "", HelpDialogAction.OpenWebView("google.com", "WebView")), linksViewData[4].helpLinkViewData)
        assertEquals(HelpLinkViewData("Share your love title", "", HelpDialogAction.RateTheApp), linksViewData[5].helpLinkViewData)
    }

    private fun createHelpLink(availableTo: List<String>, text: String, id: String = "", url: String = ""): HelpLink {
        return HelpLink(id, "", availableTo, url, text, "")
    }
}