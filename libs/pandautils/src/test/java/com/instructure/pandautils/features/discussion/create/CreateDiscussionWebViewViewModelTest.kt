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
package com.instructure.pandautils.features.discussion.create

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

@ExperimentalCoroutinesApi
class CreateDiscussionWebViewViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val locale: String = "en"
    private val timezone: String = "GMT"

    private lateinit var oAuthManager: OAuthManager
    private lateinit var viewModel: CreateDiscussionWebViewViewModel

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.AwaitKt")
        val apiPrefs: ApiPrefs = mockk(relaxed = true)
        oAuthManager = mockk(relaxed = true)
        val resources: Resources = mockk(relaxed = true)

        val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
        val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
        val testDispatcher = UnconfinedTestDispatcher()
        val localeObject: Locale = mockk(relaxed = true)
        val timezoneObject: TimeZone = mockk(relaxed = true)

        every { resources.getString(R.string.errorOccurred) } returns "An unexpected error occurred."
        every { localeObject.language } returns locale
        every { timezoneObject.id } returns timezone
        every { apiPrefs.fullDomain } returns "https://domain.com"

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        viewModel = CreateDiscussionWebViewViewModel(apiPrefs, oAuthManager, resources, localeObject, timezoneObject)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test loading the discussion create screen`() {
        val course = Course(1)
        val isAnnouncement = false
        val edit = null
        val authenticatedSession = AuthenticatedSession("https://authenticatedSession.com")
        coEvery { oAuthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(authenticatedSession)
        }

        viewModel.loadData(course, isAnnouncement, edit)

        coVerify { oAuthManager.getAuthenticatedSessionAsync("https://domain.com/courses/1/discussion_topics/new") }
        assertEquals("https://authenticatedSession.com&embed=true&session_locale=$locale&session_timezone=$timezone", viewModel.data.value?.url)
    }

    @Test
    fun `Test loading the discussion edit screen`() {
        val course = Course(1)
        val isAnnouncement = false
        val edit = 2L
        val authenticatedSession = AuthenticatedSession("https://authenticatedSession.com")
        coEvery { oAuthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(authenticatedSession)
        }

        viewModel.loadData(course, isAnnouncement, edit)

        coVerify { oAuthManager.getAuthenticatedSessionAsync("https://domain.com/courses/1/discussion_topics/2/edit") }
        assertEquals("https://authenticatedSession.com&embed=true&session_locale=$locale&session_timezone=$timezone", viewModel.data.value?.url)
    }

    @Test
    fun `Test loading the announcement create screen`() {
        val course = Course(1)
        val isAnnouncement = true
        val edit = null
        val authenticatedSession = AuthenticatedSession("https://authenticatedSession.com")
        coEvery { oAuthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(authenticatedSession)
        }

        viewModel.loadData(course, isAnnouncement, edit)

        coVerify { oAuthManager.getAuthenticatedSessionAsync("https://domain.com/courses/1/discussion_topics/new?is_announcement=true") }
        assertEquals("https://authenticatedSession.com&embed=true&session_locale=$locale&session_timezone=$timezone", viewModel.data.value?.url)
    }

    @Test
    fun `Test loading the announcement edit screen`() {
        val course = Course(1)
        val isAnnouncement = true
        val edit = 2L
        val authenticatedSession = AuthenticatedSession("https://authenticatedSession.com")
        coEvery { oAuthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(authenticatedSession)
        }

        viewModel.loadData(course, isAnnouncement, edit)

        coVerify { oAuthManager.getAuthenticatedSessionAsync("https://domain.com/courses/1/discussion_topics/2/edit") }
        assertEquals("https://authenticatedSession.com&embed=true&session_locale=$locale&session_timezone=$timezone", viewModel.data.value?.url)
    }

    @Test
    fun `Test failed loading the screen`() {
        val course = Course(1)
        val isAnnouncement = false
        val edit = null
        coEvery { oAuthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        viewModel.loadData(course, isAnnouncement, edit)

        coVerify { oAuthManager.getAuthenticatedSessionAsync("https://domain.com/courses/1/discussion_topics/new") }

        assertEquals(ViewState.Error("An unexpected error occurred."), viewModel.state.value)
    }
}

