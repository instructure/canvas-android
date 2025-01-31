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
package com.instructure.pandautils.features.discussion.details

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.Assert.assertEquals
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

@OptIn(ExperimentalCoroutinesApi::class)
class DiscussionDetailsWebViewViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val discussionManager: DiscussionManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val oAuthManager: OAuthManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val locale: String = "en"
    private val timezone: String = "GMT"

    private lateinit var viewModel: DiscussionDetailsWebViewViewModel

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.AwaitKt")
        val localeObject: Locale = mockk(relaxed = true)
        val timezoneObject: TimeZone = mockk(relaxed = true)

        every { resources.getString(R.string.errorOccurred) } returns "An unexpected error occurred."
        every { localeObject.language } returns locale
        every { timezoneObject.id } returns timezone
        every { apiPrefs.fullDomain } returns "https://domain.com"

        val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
        val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        viewModel = DiscussionDetailsWebViewViewModel(oAuthManager, apiPrefs, discussionManager, resources, localeObject, timezoneObject)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test Discussion details successful state`() {
        val course = Course(3)
        val discussionTopicHeader = DiscussionTopicHeader(6, title = "Discussion Title")
        val authenticatedSession = AuthenticatedSession("https://authenticatedSession.com")

        coEvery { discussionManager.getDiscussionTopicHeaderAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(discussionTopicHeader)
        }
        coEvery { oAuthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(authenticatedSession)
        }

        viewModel.loadData(course, null, discussionTopicHeader.id)

        verify(exactly = 1) { oAuthManager.getAuthenticatedSessionAsync("https://domain.com/courses/3/discussion_topics/6") }

        assertEquals(discussionTopicHeader.title, viewModel.data.value?.title)
        assertEquals("https://authenticatedSession.com&embed=true&session_locale=en&session_timezone=GMT", viewModel.data.value?.url)
    }

    @Test
    fun `Test Discussion details failed state`() {
        val course = Course(3)
        val discussionTopicHeader = DiscussionTopicHeader(6, title = "Discussion Title")

        coEvery { discussionManager.getDiscussionTopicHeaderAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(discussionTopicHeader)
        }
        coEvery { oAuthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        viewModel.loadData(course, null, discussionTopicHeader.id)

        verify(exactly = 1) { oAuthManager.getAuthenticatedSessionAsync("https://domain.com/courses/3/discussion_topics/6") }

        assertEquals(ViewState.Error("An unexpected error occurred."), viewModel.state.value)
    }

    @Test
    fun `TestDiscussion details setLoading function`() {
        viewModel.setLoading(true)
        assertEquals(ViewState.Loading, viewModel.state.value)
        viewModel.setLoading(false)
        assertEquals(ViewState.Success, viewModel.state.value)
    }
}