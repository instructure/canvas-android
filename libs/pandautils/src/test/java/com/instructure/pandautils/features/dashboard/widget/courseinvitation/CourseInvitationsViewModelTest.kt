/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.dashboard.widget.courseinvitation

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.models.enrollment.CourseInvitation
import com.instructure.pandautils.domain.usecase.enrollment.HandleCourseInvitationParams
import com.instructure.pandautils.domain.usecase.enrollment.HandleCourseInvitationUseCase
import com.instructure.pandautils.domain.usecase.enrollment.LoadCourseInvitationsParams
import com.instructure.pandautils.domain.usecase.enrollment.LoadCourseInvitationsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CourseInvitationsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()
    private val loadCourseInvitationsUseCase: LoadCourseInvitationsUseCase = mockk(relaxed = true)
    private val handleCourseInvitationUseCase: HandleCourseInvitationUseCase = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    private lateinit var viewModel: CourseInvitationsViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        setupStrings()
    }

    private fun setupStrings() {
        every { resources.getString(R.string.courseInvitationAccepted, any()) } returns "Invitation accepted"
        every { resources.getString(R.string.courseInvitationDeclined, any()) } returns "Invitation declined"
        every { resources.getString(R.string.errorOccurred) } returns "An error occurred"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init loads invitations with force refresh`() = runTest {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L),
            CourseInvitation(2L, 200L, "Course 2", 10L)
        )
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } returns invitations

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertFalse(state.error)
        assertEquals(2, state.invitations.size)
        assertEquals("Course 1", state.invitations[0].courseName)
        assertEquals("Course 2", state.invitations[1].courseName)
    }

    @Test
    fun `init shows error state when loading fails`() = runTest {
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } throws Exception("Network error")

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertTrue(state.error)
        assertTrue(state.invitations.isEmpty())
    }

    @Test
    fun `onRefresh reloads invitations`() = runTest {
        val initialInvitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L)
        )
        val refreshedInvitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L),
            CourseInvitation(2L, 200L, "Course 2", 10L)
        )
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } returns initialInvitations andThen refreshedInvitations

        viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.invitations.size)

        viewModel.uiState.value.onRefresh()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.invitations.size)
        coVerify(exactly = 2) { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) }
    }

    @Test
    fun `onAcceptInvitation removes invitation optimistically and shows success message`() = runTest {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L),
            CourseInvitation(2L, 200L, "Course 2", 10L)
        )
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } returns invitations
        coEvery { handleCourseInvitationUseCase(any()) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.invitations.size)

        val onAccept = viewModel.uiState.value.onAcceptInvitation
        onAccept(invitations[0])
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.invitations.size)
        assertEquals("Course 2", state.invitations[0].courseName)
        assertNotNull(state.snackbarMessage)
        assertEquals("Invitation accepted", state.snackbarMessage)
        assertNull(state.snackbarAction)
        coVerify {
            handleCourseInvitationUseCase(
                HandleCourseInvitationParams(
                    courseId = 100L,
                    enrollmentId = 1L,
                    accept = true
                )
            )
        }
    }

    @Test
    fun `onDeclineInvitation removes invitation optimistically and shows success message`() = runTest {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L),
            CourseInvitation(2L, 200L, "Course 2", 10L)
        )
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } returns invitations
        coEvery { handleCourseInvitationUseCase.invoke(any()) } returns Unit

        viewModel = createViewModel()
        assertEquals(2, viewModel.uiState.value.invitations.size)
        viewModel.uiState.value.onDeclineInvitation(invitations[1])

        val state = viewModel.uiState.value
        assertEquals(1, state.invitations.size)
        assertEquals("Course 1", state.invitations[0].courseName)
        assertNotNull(state.snackbarMessage)
        assertEquals("Invitation declined", state.snackbarMessage)
        assertNull(state.snackbarAction)
        coVerify {
            handleCourseInvitationUseCase(
                HandleCourseInvitationParams(
                    courseId = 200L,
                    enrollmentId = 2L,
                    accept = false
                )
            )
        }
    }

    @Test
    fun `onAcceptInvitation restores invitation and shows error with retry action on failure`() = runTest {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L),
            CourseInvitation(2L, 200L, "Course 2", 10L)
        )
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } returns invitations
        coEvery { handleCourseInvitationUseCase(any()) } throws Exception("Network error")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onAcceptInvitation(invitations[0])
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.invitations.size)
        assertEquals("Course 1", state.invitations[0].courseName)
        assertEquals("Course 2", state.invitations[1].courseName)
        assertNotNull(state.snackbarMessage)
        assertNotNull(state.snackbarAction)
    }

    @Test
    fun `onDeclineInvitation restores invitation and shows error with retry action on failure`() = runTest {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L),
            CourseInvitation(2L, 200L, "Course 2", 10L)
        )
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } returns invitations
        coEvery { handleCourseInvitationUseCase(any()) } throws Exception("Network error")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onDeclineInvitation(invitations[1])
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.invitations.size)
        assertNotNull(state.snackbarMessage)
        assertNotNull(state.snackbarAction)
    }

    @Test
    fun `retry action retries the failed invitation action`() = runTest {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L)
        )
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } returns invitations
        coEvery { handleCourseInvitationUseCase(any()) } throws Exception("Network error") andThen Unit

        viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.invitations.size)

        viewModel.uiState.value.onAcceptInvitation(invitations[0])
        advanceUntilIdle()

        val retryAction = viewModel.uiState.value.snackbarAction
        assertNotNull(retryAction)

        retryAction!!.invoke()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.invitations.size)
        assertNotNull(state.snackbarMessage)
        assertNull(state.snackbarAction)
        coVerify(exactly = 2) {
            handleCourseInvitationUseCase(
                HandleCourseInvitationParams(
                    courseId = 100L,
                    enrollmentId = 1L,
                    accept = true
                )
            )
        }
    }

    @Test
    fun `onClearSnackbar clears snackbar message and action`() = runTest {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L)
        )
        coEvery { loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true)) } returns invitations
        coEvery { handleCourseInvitationUseCase(any()) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onAcceptInvitation(invitations[0])
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.snackbarMessage)

        viewModel.uiState.value.onClearSnackbar()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.snackbarMessage)
        assertNull(state.snackbarAction)
    }

    private fun createViewModel(): CourseInvitationsViewModel {
        return CourseInvitationsViewModel(loadCourseInvitationsUseCase, handleCourseInvitationUseCase, resources)
    }
}