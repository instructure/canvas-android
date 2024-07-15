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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val repository: DashboardRepository = mockk(relaxed = true)
    private val previousUsersUtils: PreviousUsersUtils = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)
    private val selectedStudentHolder: SelectedStudentHolder = mockk(relaxed = true)
    private val inboxCountUpdaterFlow = MutableSharedFlow<Boolean>()
    private val inboxCountUpdater: InboxCountUpdater = TestInboxCountUpdater(inboxCountUpdaterFlow)
    private val alertCountUpdaterFlow = MutableSharedFlow<Boolean>()
    private val alertCountUpdater: AlertCountUpdater = TestAlertCountUpdater(alertCountUpdaterFlow)

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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

        coEvery { repository.getStudents() } returns students

        createViewModel()

        val expected = listOf(
            StudentItemViewData(1L, "Student One", "avatar1"),
            StudentItemViewData(2L, "Student Two", "avatar2")
        )

        assertEquals(expected, viewModel.data.value.studentItems.map { it.studentItemViewData })
    }

    @Test
    fun `Empty student list`() {
        coEvery { repository.getStudents() } returns emptyList()

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
        coEvery { repository.getStudents() } returns students
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

        coEvery { repository.getStudents() } returns students

        createViewModel()

        assertEquals(students.first(), viewModel.data.value.selectedStudent)

        viewModel.data.value.studentItems.last().onStudentClick()

        assertEquals(students.last(), viewModel.data.value.selectedStudent)
        assertFalse(viewModel.data.value.studentSelectorExpanded)
        coVerify { selectedStudentHolder.updateSelectedStudent(students.last()) }
    }

    @Test
    fun `Toggle student selector`() {
        createViewModel()

        viewModel.toggleStudentSelector()
        assertTrue(viewModel.data.value.studentSelectorExpanded)

        viewModel.toggleStudentSelector()
        assertFalse(viewModel.data.value.studentSelectorExpanded)
    }

    @Test
    fun `Update unread count when the update unread count flow triggers an update`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents() } returns students
        coEvery { repository.getUnreadCounts() } returns 0

        createViewModel()

        assertEquals(0, viewModel.data.value.unreadCount)

        coEvery { repository.getUnreadCounts() } returns 1
        inboxCountUpdaterFlow.emit(true)

        assertEquals(1, viewModel.data.value.unreadCount)
    }

    @Test
    fun `Update alert count when the update alert count flow triggers`() = runTest {
        val students = listOf(User(id = 1L), User(id = 2L))
        coEvery { repository.getStudents() } returns students
        coEvery { repository.getAlertCount(1L) } returns 0

        createViewModel()

        assertEquals(0, viewModel.data.value.alertCount)

        coEvery { repository.getAlertCount(1L) } returns 1
        alertCountUpdaterFlow.emit(true)

        assertEquals(1, viewModel.data.value.alertCount)
    }

    private fun createViewModel() {
        viewModel = DashboardViewModel(
            context = context,
            repository = repository,
            previousUsersUtils = previousUsersUtils,
            apiPrefs = apiPrefs,
            parentPrefs = parentPrefs,
            selectedStudentHolder = selectedStudentHolder,
            inboxCountUpdater = inboxCountUpdater,
            alertCountUpdater = alertCountUpdater
        )
    }
}
