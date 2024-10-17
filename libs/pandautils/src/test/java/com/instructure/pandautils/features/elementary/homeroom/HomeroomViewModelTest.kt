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
package com.instructure.pandautils.features.elementary.homeroom

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.AnnouncementItemViewModel
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.CourseCardItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.HtmlContentFormatter
import io.mockk.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeroomViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val courseManager: CourseManager = mockk(relaxed = true)
    private val announcementManager: AnnouncementManager = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)
    private val oauthManager: OAuthManager = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val courseCardCreator: CourseCardCreator = mockk(relaxed = true)

    private lateinit var viewModel: HomeroomViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        coEvery { htmlContentFormatter.formatHtmlWithIframes(any()) } returnsArgument 0

        ContextKeeper.appContext = mockk(relaxed = true)
        every { courseManager.getDashboardCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        mockkStatic("kotlinx.coroutines.AwaitKt")

        coEvery { courseCardCreator.createCourseCards(any(), any(), any(), any()) } returns listOf(mockk<CourseCardItemViewModel>())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Show error state if fetching courses fails`() {
        // Given
        every { resources.getString(R.string.homeroomError) } returns "Error"
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Error)
        assertEquals("Error", (viewModel.state.value as ViewState.Error).errorMessage)
    }

    @Test
    fun `Create AnnouncementViewModels from the first announcements of each homeroom course`() {
        // Given
        val courses = listOf(Course(id = 1, name = "Course 1", homeroomCourse = true), Course(id = 2, name = "Course 2", homeroomCourse = false))
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        val announcements = listOf(
            DiscussionTopicHeader(title = "Course 1 first", message = "First message"),
            DiscussionTopicHeader(title = "Course 1 second", message = "Second message"))

        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(courses[0], any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(announcements))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        // Then
        // Verify that we only get the announcements for the homeroom course.
        verifyAll { announcementManager.getLatestAnnouncementAsync(courses[0], any()) }

        assertEquals(ViewState.Success, viewModel.state.value)

        assertEquals(1, viewModel.data.value!!.announcements.size)

        val announcementViewData = (viewModel.data.value!!.announcements[0] as AnnouncementItemViewModel).data
        assertEquals(AnnouncementViewData("Course 1", "Course 1 first", "First message"), announcementViewData)
    }

    @Test
    fun `Only create AnnouncementViewModel for courses that have at least one announcement`() {
        // Given
        val courses = listOf(Course(id = 1, name = "Course 1", homeroomCourse = true), Course(id = 2, name = "Course 2", homeroomCourse = true))
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        val announcements = listOf(
            DiscussionTopicHeader(title = "Course 1 first", message = "First message"),
            DiscussionTopicHeader(title = "Course 1 second", message = "Second message"))

        val announcementsDeferred1: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(courses[0], any()) } returns announcementsDeferred1

        val announcementsDeferred2: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(courses[1], any()) } returns announcementsDeferred2

        coEvery { listOf(announcementsDeferred1, announcementsDeferred2).awaitAll() } returns listOf(DataResult.Success(announcements), DataResult.Success(emptyList()))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        // Then
        verifyAll {
            announcementManager.getLatestAnnouncementAsync(courses[0], any());
            announcementManager.getLatestAnnouncementAsync(courses[1], any())
        }

        assertEquals(ViewState.Success, viewModel.state.value)

        assertEquals(1, viewModel.data.value!!.announcements.size)
        assertTrue(viewModel.shouldUpdateAnnouncements)

        val announcementViewData = (viewModel.data.value!!.announcements[0] as AnnouncementItemViewModel).data
        assertEquals(AnnouncementViewData("Course 1", "Course 1 first", "First message"), announcementViewData)
    }

    @Test
    fun `Error after refresh should trigger refresh error event if data is already available`() {
        // Given
        val courses = listOf(Course(id = 1, name = "Course 1", homeroomCourse = true))
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() }.returnsMany(DataResult.Success(courses), DataResult.Fail())
        }

        val announcements = listOf(DiscussionTopicHeader(title = "Course 1 first", message = "First message"))

        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(courses[0], any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(announcements))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        viewModel.refresh()

        // Then
        assertEquals(ViewState.Error(), viewModel.state.value)
        assertEquals(HomeroomAction.ShowRefreshError, viewModel.events.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Clicking on previous announcements should trigger Open Announcements event`() {
        // Given
        val courses = listOf(Course(id = 1, name = "Course 1", homeroomCourse = true))
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() }.returnsMany(DataResult.Success(courses), DataResult.Fail())
        }

        val announcements = listOf(DiscussionTopicHeader(title = "Course 1 first", message = "First message"))

        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(courses[0], any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(announcements))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        val announcementViewModel = viewModel.data.value!!.announcements[0] as AnnouncementItemViewModel
        announcementViewModel.onPreviousAnnouncementsClicked()

        // Then
        assertEquals(HomeroomAction.OpenAnnouncements(courses[0]), viewModel.events.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Clicking on LTI button should trigger LTI clicked event`() {
        // Given
        val courses = listOf(Course(id = 1, name = "Course 1", homeroomCourse = true))
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() }.returnsMany(DataResult.Success(courses), DataResult.Fail())
        }

        val announcements = listOf(DiscussionTopicHeader(title = "Course 1 first", message = "First message"))

        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(courses[0], any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(announcements))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        val announcementViewModel = viewModel.data.value!!.announcements[0] as AnnouncementItemViewModel
        announcementViewModel.onLtiButtonPressed("LTI")

        // Then
        assertEquals(HomeroomAction.LtiButtonPressed("LTI"), viewModel.events.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `OnAnnouncementViewsReady should send event`() {
        // When
        viewModel = createViewModel()
        viewModel.events.observe(lifecycleOwner, Observer {})
        viewModel.onAnnouncementViewsReady()

        // Then
        assertEquals(HomeroomAction.AnnouncementViewsReady, viewModel.events.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Create course cards from dashboard courses that are not homeroom`() {
        // Given
        val courses = listOf(
            Course(id = 1, name = "Course 1", homeroomCourse = true),
            Course(id = 2, name = "Course 2", homeroomCourse = false),
            Course(id = 3, name = "Course 3 not on dashboard", homeroomCourse = false)
        )
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        val dashboardCards = listOf(DashboardCard(id = 2))
        every { courseManager.getDashboardCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(dashboardCards)
        }

        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(any(), any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(emptyList()))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        // Then
        assertEquals(ViewState.Success, viewModel.state.value)

        // Verify that course card creation is called with the correct list of courses
        val dashboardCourses = slot<MutableList<Course>>()
        coVerify { courseCardCreator.createCourseCards(capture(dashboardCourses), any(), any(), any()) }
        assertEquals(1, dashboardCourses.captured.size)
    }

    @Test
    fun `Do not create course cards if there are only homeroom courses`() {
        // Given
        val courses = listOf(
            Course(id = 1, name = "Course 1", homeroomCourse = true)
        )
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        val dashboardCards = listOf(DashboardCard(id = 2))
        every { courseManager.getDashboardCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(dashboardCards)
        }

        val announcements = listOf(DiscussionTopicHeader(title = "Course 1 first", message = "First message"))

        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(any(), any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(announcements))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        // Then
        assertEquals(ViewState.Success, viewModel.state.value)

        // Verify that course card creation is not called, because we don't have dashboard courses
        coVerify(exactly = 0) { courseCardCreator.createCourseCards(any(), any(), any(), any()) }
    }

    @Test
    fun `Show empty state if there are no announcements and no courses`() {
        // Given
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        // Then
        assertEquals(ViewState.Empty(R.string.homeroomEmptyTitle, R.string.homeroomEmptyMessage, R.drawable.ic_panda_super), viewModel.state.value)
    }

    @Test
    fun `Create course cards from the saved courses when refreshing assignments data`() {
        // Given
        val courses = listOf(Course(id = 2, name = "Course 2", homeroomCourse = false))
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        val dashboardCards = listOf(DashboardCard(id = 2))
        every { courseManager.getDashboardCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(dashboardCards)
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, Observer {})

        // Capture the dashboard cards list from the first call
        val dashboardCourses = slot<MutableList<Course>>()
        coVerify { courseCardCreator.createCourseCards(capture(dashboardCourses), any(), any(), any()) }

        viewModel.refreshAssignmentsStatus()

        // Then
        // Verify that course card creation is called with the correct list of courses and announcements should not update
        assertEquals(ViewState.Success, viewModel.state.value)
        assertFalse(viewModel.shouldUpdateAnnouncements)
        coVerify { courseCardCreator.createCourseCards(eq(dashboardCourses.captured), any(), any(), any()) }
    }

    private fun createViewModel() = HomeroomViewModel(apiPrefs, resources, courseManager, announcementManager, htmlContentFormatter, oauthManager, colorKeeper, courseCardCreator)
}