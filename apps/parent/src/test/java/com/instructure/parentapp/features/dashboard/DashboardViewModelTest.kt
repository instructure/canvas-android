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

package com.instructure.parentapp.features.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController.Companion.KEY_DEEP_LINK_INTENT
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.Placement
import com.instructure.canvasapi2.models.Placements
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.parentapp.R
import com.instructure.parentapp.features.alerts.list.AlertsRepository
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import com.instructure.testutils.collectForTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val context: Context = mockk(relaxed = true)
    private val repository: DashboardRepository = mockk(relaxed = true)
    private val alertsRepository: AlertsRepository = mockk(relaxed = true)
    private val previousUsersUtils: PreviousUsersUtils = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)
    private val selectedStudentHolder: SelectedStudentHolder = mockk(relaxed = true)
    private val inboxCountUpdaterFlow = MutableSharedFlow<Boolean>()
    private val increaseInboxCountFlow = MutableSharedFlow<Int>()
    private val inboxCountUpdater: InboxCountUpdater = TestInboxCountUpdater(inboxCountUpdaterFlow, increaseInboxCountFlow)
    private val alertCountUpdaterFlow = MutableSharedFlow<Boolean>()
    private val alertCountUpdater: AlertCountUpdater = TestAlertCountUpdater(alertCountUpdaterFlow)
    private val analytics: Analytics = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        every { savedStateHandle.get<Intent>(KEY_DEEP_LINK_INTENT) } returns null
        ContextKeeper.appContext = context
        every { context.getString(R.string.a11y_studentSelectorExpand) } returns "expand"
        every { context.getString(R.string.a11y_studentSelectorCollapse) } returns "collapse"
        every { context.getString(R.string.a11y_studentSelectorContentDescription, any(), any()) } answers {
            "Tap to ${secondArg<Array<Any>>()[0]} student selector, selected student is: ${secondArg<Array<Any>>()[1]}"
        }
    }

    @Test
    fun `Load user info correctly`() {
        val user = User(
            id = 1L,
            name = "Test Name",
            shortName = "Short Name",
            pronouns = "Pronouns",
            primaryEmail = "primary@email.com",
            avatarUrl = "avatar"
        )

        val expected = UserViewData(
            user.name,
            user.pronouns,
            user.shortName,
            user.avatarUrl,
            user.primaryEmail
        )
        coEvery { apiPrefs.user } returns user

        createViewModel()

        assertEquals(expected, viewModel.data.value.userViewData)
    }

    @Test
    fun `Students map correctly`() {
        val students = listOf(
            User(id = 1L, shortName = "Student One", avatarUrl = "avatar1"),
            User(id = 2L, shortName = "Student Two", avatarUrl = "avatar2"),
        )

        coEvery { repository.getStudents(any()) } returns students

        createViewModel()

        val expected = listOf(
            StudentItemViewModel(studentItemViewData = StudentItemViewData(1L, "Student One", "avatar1")) {},
            StudentItemViewModel(studentItemViewData = StudentItemViewData(2L, "Student Two", "avatar2")) {},
            AddStudentItemViewModel(color = 0) {}
        )

        val items = viewModel.data.value.studentItems
        assert(items[0] is StudentItemViewModel)
        assertEquals((expected[0] as StudentItemViewModel).studentItemViewData, (items[0] as StudentItemViewModel).studentItemViewData)
        assert(items[1] is StudentItemViewModel)
        assertEquals((expected[1] as StudentItemViewModel).studentItemViewData, (items[1] as StudentItemViewModel).studentItemViewData)
        assert(items[2] is AddStudentItemViewModel)
    }

    @Test
    fun `Add student click`() = runTest {
        coEvery { repository.getStudents(any()) } returns listOf(User(id = 1L))

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        (viewModel.data.value.studentItems.last() as AddStudentItemViewModel).onAddStudentClicked()

        assertEquals(DashboardViewModelAction.AddStudent, events.first())
        verify { analytics.logEvent(AnalyticsEventConstants.ADD_STUDENT_DASHBOARD) }
    }

    @Test
    fun `Empty student list`() {
        coEvery { repository.getStudents(any()) } returns emptyList()

        createViewModel()

        val expected = ViewState.Empty(
            R.string.noStudentsError,
            R.string.noStudentsErrorDescription,
            R.drawable.panda_manage_students
        )

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `Selected student set up correctly when it was selected before`() {
        val students = listOf(User(id = 1L), User(id = 2L))
        val expected = students[1]
        coEvery { repository.getStudents(any()) } returns students
        coEvery { previousUsersUtils.getSignedInUser(any(), any(), any()) } returns SignedInUser(
            user = User(),
            domain = "",
            protocol = "",
            token = "",
            accessToken = null,
            refreshToken = "",
            clientId = null,
            clientSecret = null,
            calendarFilterPrefs = null,
            lastLogoutDate = null,
            canvasForElementary = false,
            selectedStudentId = 2L
        )

        createViewModel()

        assertEquals(expected, viewModel.data.value.selectedStudent)
    }

    @Test
    fun `Change selected student`() {
        val students = listOf(
            User(id = 1L, name = "Student One", avatarUrl = "avatar1"),
            User(id = 2L, name = "Student Two", avatarUrl = "avatar2"),
        )

        coEvery { repository.getStudents(any()) } returns students

        createViewModel()

        assertEquals(students.first(), viewModel.data.value.selectedStudent)

        (viewModel.data.value.studentItems.last { it is StudentItemViewModel } as StudentItemViewModel).onStudentClick()

        assertEquals(students.last(), viewModel.data.value.selectedStudent)
        assertFalse(viewModel.data.value.studentSelectorExpanded)
        coVerify { selectedStudentHolder.updateSelectedStudent(students.last()) }
    }

    @Test
    fun `Toggle student selector`() {
        coEvery { repository.getStudents(any()) } returns listOf(User(shortName = "Short Name"))

        createViewModel()

        viewModel.toggleStudentSelector()
        assertTrue(viewModel.data.value.studentSelectorExpanded)
        assertEquals(
            "Tap to collapse student selector, selected student is: Short Name",
            viewModel.data.value.studentSelectorContentDescription
        )

        viewModel.toggleStudentSelector()
        assertFalse(viewModel.data.value.studentSelectorExpanded)
        assertEquals(
            "Tap to expand student selector, selected student is: Short Name",
            viewModel.data.value.studentSelectorContentDescription
        )
    }

    @Test
    fun `Update unread count when the update unread count flow triggers an update`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents(any()) } returns students
        coEvery { repository.getUnreadCounts() } returns 0

        createViewModel()

        assertEquals(0, viewModel.data.value.unreadCount)

        coEvery { repository.getUnreadCounts() } returns 1
        inboxCountUpdaterFlow.emit(true)

        assertEquals(1, viewModel.data.value.unreadCount)
    }

    @Test
    fun `Update unread count when the increase unread count flow triggers an update`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents(any()) } returns students
        coEvery { repository.getUnreadCounts() } returns 0

        createViewModel()

        assertEquals(0, viewModel.data.value.unreadCount)

        increaseInboxCountFlow.emit(1)

        assertEquals(1, viewModel.data.value.unreadCount)
    }

    @Test
    fun `Update alert count when the update alert count flow triggers`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents(any()) } returns students
        coEvery { alertsRepository.getUnreadAlertCount(1L) } returns 0

        createViewModel()

        assertEquals(0, viewModel.data.value.alertCount)

        coEvery { alertsRepository.getUnreadAlertCount(1L) } returns 1
        alertCountUpdaterFlow.emit(true)

        assertEquals(1, viewModel.data.value.alertCount)
    }

    @Test
    fun `Initializing viewModel with a deeplink sends navigate deep link event`() = runTest {
        val uri = mockk<Uri>()
        val deepLinkIntent = mockk<Intent>(relaxed = true).also {
            every { it.data } returns uri
        }
        every { savedStateHandle.get<Intent>(KEY_DEEP_LINK_INTENT) } returns deepLinkIntent

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        assertEquals(DashboardViewModelAction.NavigateDeepLink(uri), events.first())
    }

    @Test
    fun `Update color updates add student item color`() {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents(any()) } returns students

        createViewModel()

        val items = viewModel.data.value.studentItems
        viewModel.updateColor(123)

        assertEquals(123, (items[2] as AddStudentItemViewModel).color)
    }

    @Test
    fun `Update date with launch definitions when launch definitions are received`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents(any()) } returns students
        coEvery { repository.getLaunchDefinitions() } returns listOf(
            LaunchDefinition(
                "type", 1, "name", null, "domain",
                Placements(Placement("", "global.url", "")), null
            )
        )

        createViewModel()

        assertEquals(1, viewModel.data.value.launchDefinitionViewData.size)
        val launchDefinition = viewModel.data.value.launchDefinitionViewData.first()
        assertEquals("name", launchDefinition.name)
        assertEquals("domain", launchDefinition.domain)
        assertEquals("global.url", launchDefinition.url)
    }

    @Test
    fun `Do not update launch definitions when url or domain is null`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents(any()) } returns students
        coEvery { repository.getLaunchDefinitions() } returns listOf(
            LaunchDefinition(
                "type", 1, "name", null, "domain",
                Placements(null), null
            ),
            LaunchDefinition(
                "type", 1, "name", null, null,
                Placements(Placement("", "global.url", "")), null
            )
        )

        createViewModel()

        assertEquals(0, viewModel.data.value.launchDefinitionViewData.size)
    }

    @Test
    fun `Open Mastery sends correct open LTI event`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents(any()) } returns students
        coEvery { repository.getLaunchDefinitions() } returns listOf(
            LaunchDefinition(
                "type", 1, "name", null, LaunchDefinition.MASTERY_DOMAIN,
                Placements(Placement("", "global.url", "")), null
            )
        )

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.openMastery()

        assertEquals(DashboardViewModelAction.OpenLtiTool("global.url", "name"), events.first())
    }

    @Test
    fun `Open Studio sends correct open LTI event`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents(any()) } returns students
        coEvery { repository.getLaunchDefinitions() } returns listOf(
            LaunchDefinition(
                "type", 1, "name", null, LaunchDefinition.STUDIO_DOMAIN,
                Placements(Placement("", "global.url", "")), null
            )
        )

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.openStudio()

        assertEquals(DashboardViewModelAction.OpenLtiTool("global.url", "name"), events.first())
    }

    private fun createViewModel() {
        viewModel = DashboardViewModel(
            context = context,
            repository = repository,
            alertsRepository = alertsRepository,
            previousUsersUtils = previousUsersUtils,
            apiPrefs = apiPrefs,
            parentPrefs = parentPrefs,
            selectedStudentHolder = selectedStudentHolder,
            inboxCountUpdater = inboxCountUpdater,
            alertCountUpdater = alertCountUpdater,
            analytics = analytics,
            savedStateHandle = savedStateHandle
        )
    }
}
