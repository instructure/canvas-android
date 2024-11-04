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

package com.instructure.parentapp.features.courses.details

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.navigation.Navigation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class CourseDetailsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: CourseDetailsRepository = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var viewModel: CourseDetailsViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1, 1)
        coEvery { savedStateHandle.get<Long>(Navigation.COURSE_ID) } returns 1
        coEvery { repository.getCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        every { parentPrefs.currentStudent } returns User(shortName = "User 1")
        every { apiPrefs.fullDomain } returns "https://domain.com"
        every { context.getString(R.string.regardingHiddenMessage, any(), any()) } returns "https://domain.com/courses/1"
        every { context.getString(R.string.regardingHiddenMessage, any(), "") } returns "Regarding: User 1"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Load course details with front page tab`() = runTest {
        coEvery { repository.getCourse(1, any()) } returns Course(id = 1, name = "Course 1", homePage = Course.HomePage.HOME_WIKI)
        coEvery { repository.getCourseTabs(1, any()) } returns listOf(Tab("tab1"))

        createViewModel()

        val expected = CourseDetailsUiState(
            courseName = "Course 1",
            studentColor = 1,
            isLoading = false,
            isError = false,
            tabs = listOf(TabType.GRADES, TabType.FRONT_PAGE)
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Load course details with syllabus tab`() = runTest {
        coEvery { repository.getCourse(1, any()) } returns Course(
            id = 1,
            name = "Course 1",
            homePage = Course.HomePage.HOME_SYLLABUS,
            syllabusBody = "Syllabus body"
        )
        coEvery { repository.getCourseTabs(1, any()) } returns listOf(Tab(Tab.SYLLABUS_ID))

        createViewModel()

        val expected = CourseDetailsUiState(
            courseName = "Course 1",
            studentColor = 1,
            isLoading = false,
            isError = false,
            tabs = listOf(TabType.GRADES, TabType.SYLLABUS)
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Load course details with summary tab`() = runTest {
        coEvery { repository.getCourse(1, any()) } returns Course(
            id = 1,
            name = "Course 1",
            homePage = Course.HomePage.HOME_SYLLABUS,
            syllabusBody = "Syllabus body",
            settings = CourseSettings(courseSummary = true)
        )
        coEvery { repository.getCourseTabs(1, any()) } returns listOf(Tab(Tab.SYLLABUS_ID))

        createViewModel()

        val expected = CourseDetailsUiState(
            courseName = "Course 1",
            studentColor = 1,
            isLoading = false,
            isError = false,
            tabs = listOf(TabType.GRADES, TabType.SYLLABUS, TabType.SUMMARY)
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Error loading course details`() = runTest {
        coEvery { repository.getCourse(1, any()) } throws Exception()

        createViewModel()

        val expected = CourseDetailsUiState(
            studentColor = 1,
            isLoading = false,
            isError = true
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Refresh course details`() = runTest {
        coEvery { repository.getCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { repository.getCourseTabs(1, any()) } returns listOf(Tab("tab1"))

        createViewModel()

        val expected = CourseDetailsUiState(
            courseName = "Course 1",
            studentColor = 1,
            isLoading = false,
            isError = false,
            tabs = listOf(TabType.GRADES)
        )

        Assert.assertEquals(expected, viewModel.uiState.value)

        coEvery { repository.getCourse(1, any()) } returns Course(
            id = 1,
            name = "Course 2",
            homePage = Course.HomePage.HOME_SYLLABUS,
            syllabusBody = "Syllabus body",
            settings = CourseSettings(courseSummary = true)
        )
        coEvery { repository.getCourseTabs(1, any()) } returns listOf(Tab(Tab.SYLLABUS_ID))

        viewModel.handleAction(CourseDetailsAction.Refresh)

        val expectedAfterRefresh = expected.copy(
            courseName = "Course 2",
            tabs = listOf(TabType.GRADES, TabType.SYLLABUS, TabType.SUMMARY)
        )

        Assert.assertEquals(expectedAfterRefresh, viewModel.uiState.value)
    }

    @Test
    fun `Navigate to assignment details`() = runTest {
        createViewModel()

        val events = mutableListOf<CourseDetailsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CourseDetailsAction.NavigateToAssignmentDetails(1, 1))

        val expected = CourseDetailsViewModelAction.NavigateToAssignmentDetails(1, 1)
        Assert.assertEquals(expected, events.last())
    }

    @Test
    fun `Navigate to compose message`() = runTest {
        createViewModel()

        val events = mutableListOf<CourseDetailsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CourseDetailsAction.SendAMessage)

        val expected = CourseDetailsViewModelAction.NavigateToComposeMessageScreen(getInboxComposeOptions())
        Assert.assertEquals(expected, events.last())
    }

    private fun createViewModel() {
        viewModel = CourseDetailsViewModel(context, savedStateHandle, repository, parentPrefs, apiPrefs)
    }

    private fun getInboxComposeOptions(): InboxComposeOptions {
        val courseContextId = Course(1).contextId
        var options = InboxComposeOptions.buildNewMessage()
        options = options.copy(
            defaultValues = options.defaultValues.copy(
                contextCode = courseContextId,
                contextName = "Course 1",
                subject = "Regarding: User 1"
            ),
            disabledFields = options.disabledFields.copy(
                isContextDisabled = true
            ),
            autoSelectRecipientsFromRoles = listOf(EnrollmentType.TEACHERENROLLMENT),
            hiddenBodyMessage = "https://domain.com/courses/1"
        )

        return options
    }
}
