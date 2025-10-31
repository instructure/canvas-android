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
 */
package com.instructure.student.features.appointmentgroups

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.appointmentgroups.domain.model.AppointmentGroupDomain
import com.instructure.student.features.appointmentgroups.domain.model.AppointmentSlotDomain
import com.instructure.student.features.appointmentgroups.domain.model.ConflictInfo
import com.instructure.student.features.appointmentgroups.domain.usecase.CancelAppointmentReservationUseCase
import com.instructure.student.features.appointmentgroups.domain.usecase.GetAppointmentGroupsUseCase
import com.instructure.student.features.appointmentgroups.domain.usecase.ReserveAppointmentSlotUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import java.util.Date

@ExperimentalCoroutinesApi
class AppointmentGroupsListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getAppointmentGroupsUseCase: GetAppointmentGroupsUseCase = mockk(relaxed = true)
    private val reserveAppointmentSlotUseCase: ReserveAppointmentSlotUseCase = mockk(relaxed = true)
    private val cancelAppointmentReservationUseCase: CancelAppointmentReservationUseCase = mockk(relaxed = true)
    private val uiMapper: AppointmentGroupUiMapper = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    private lateinit var viewModel: AppointmentGroupsListViewModel

    private val courseId = 123L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<Long>(AppointmentGroupsListViewModel.COURSE_ID) } returns courseId
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAppointmentGroups success updates state with groups`() = runTest {
        val domainGroups = listOf(
            createAppointmentGroupDomain(1L, "Office Hours")
        )
        val uiGroups = listOf(
            createAppointmentGroupUiState(1L, "Office Hours")
        )

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups

        viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals(1, state.groups.size)
        assertEquals("Office Hours", state.groups[0].title)
    }

    @Test
    fun `loadAppointmentGroups failure sets error state`() = runTest {
        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Fail()

        viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
        assertTrue(state.isError)
        assertTrue(state.groups.isEmpty())
    }

    @Test
    fun `loadAppointmentGroups with isRefresh sets isRefreshing and forceNetwork`() = runTest {
        val domainGroups = listOf(createAppointmentGroupDomain(1L, "Office Hours"))
        val uiGroups = listOf(createAppointmentGroupUiState(1L, "Office Hours"))

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups

        viewModel = createViewModel()

        viewModel.loadAppointmentGroups(isRefresh = true)

        coVerify { getAppointmentGroupsUseCase(GetAppointmentGroupsUseCase.Params(courseId, forceNetwork = true)) }
    }

    @Test
    fun `loadAppointmentGroups preserves expansion state after refresh`() = runTest {
        val domainGroups = listOf(
            createAppointmentGroupDomain(1L, "Office Hours"),
            createAppointmentGroupDomain(2L, "Lab Sessions")
        )
        val uiGroups = listOf(
            createAppointmentGroupUiState(1L, "Office Hours", isExpanded = true),
            createAppointmentGroupUiState(2L, "Lab Sessions", isExpanded = true)
        )

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups

        viewModel = createViewModel()

        viewModel.handleAction(AppointmentGroupsListAction.ToggleGroupExpansion(1L))

        val stateBeforeRefresh = viewModel.uiState.first()
        assertFalse(stateBeforeRefresh.groups[0].isExpanded)
        assertTrue(stateBeforeRefresh.groups[1].isExpanded)

        viewModel.loadAppointmentGroups(isRefresh = true)

        val stateAfterRefresh = viewModel.uiState.first()
        assertFalse(stateAfterRefresh.groups[0].isExpanded)
        assertTrue(stateAfterRefresh.groups[1].isExpanded)
    }

    @Test
    fun `toggleGroupExpansion toggles isExpanded for correct group`() = runTest {
        val domainGroups = listOf(
            createAppointmentGroupDomain(1L, "Office Hours"),
            createAppointmentGroupDomain(2L, "Lab Sessions")
        )
        val uiGroups = listOf(
            createAppointmentGroupUiState(1L, "Office Hours"),
            createAppointmentGroupUiState(2L, "Lab Sessions")
        )

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups

        viewModel = createViewModel()

        viewModel.handleAction(AppointmentGroupsListAction.ToggleGroupExpansion(1L))

        val state = viewModel.uiState.first()
        assertFalse(state.groups[0].isExpanded)
        assertTrue(state.groups[1].isExpanded)
    }

    @Test
    fun `reserveSlot optimistically updates UI then shows success on API success`() = runTest {
        val domainGroups = listOf(
            createAppointmentGroupDomain(1L, "Office Hours", slots = listOf(
                createAppointmentSlotDomain(100L, isReservedByMe = false, availableSlots = 5)
            ))
        )
        val uiGroups = listOf(
            createAppointmentGroupUiState(1L, "Office Hours", slots = listOf(
                createAppointmentSlotUiState(100L, isReservedByMe = false, availableSlots = 5)
            ))
        )

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups
        coEvery { reserveAppointmentSlotUseCase(any()) } returns DataResult.Success(mockk {
            every { id } returns 999L
        })

        viewModel = createViewModel()

        viewModel.handleAction(AppointmentGroupsListAction.ReserveSlot(100L, null))

        val state = viewModel.uiState.first()
        assertTrue(state.groups[0].slots[0].isReservedByMe)
        assertEquals(999L, state.groups[0].slots[0].myReservationId)
        assertEquals(4, state.groups[0].slots[0].availableSlots)
        assertTrue(state.showReservationSuccessSnackbar)
    }

    @Test
    fun `reserveSlot reverts UI on API failure`() = runTest {
        val domainGroups = listOf(
            createAppointmentGroupDomain(1L, "Office Hours", slots = listOf(
                createAppointmentSlotDomain(100L, isReservedByMe = false, availableSlots = 5)
            ))
        )
        val uiGroups = listOf(
            createAppointmentGroupUiState(1L, "Office Hours", slots = listOf(
                createAppointmentSlotUiState(100L, isReservedByMe = false, availableSlots = 5)
            ))
        )

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups
        coEvery { reserveAppointmentSlotUseCase(any()) } returns DataResult.Fail()

        viewModel = createViewModel()

        viewModel.handleAction(AppointmentGroupsListAction.ReserveSlot(100L, null))

        val state = viewModel.uiState.first()
        assertFalse(state.groups[0].slots[0].isReservedByMe)
        assertEquals(5, state.groups[0].slots[0].availableSlots)
        assertTrue(state.showReservationErrorSnackbar)
    }

    @Test
    fun `cancelReservation optimistically updates UI then shows success on API success`() = runTest {
        val domainGroups = listOf(
            createAppointmentGroupDomain(1L, "Office Hours", slots = listOf(
                createAppointmentSlotDomain(100L, isReservedByMe = true, myReservationId = 999L, availableSlots = 4)
            ))
        )
        val uiGroups = listOf(
            createAppointmentGroupUiState(1L, "Office Hours", slots = listOf(
                createAppointmentSlotUiState(100L, isReservedByMe = true, myReservationId = 999L, availableSlots = 4)
            ))
        )

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups
        coEvery { cancelAppointmentReservationUseCase(any()) } returns DataResult.Success(Unit)

        viewModel = createViewModel()

        viewModel.handleAction(AppointmentGroupsListAction.CancelReservation(999L))

        val state = viewModel.uiState.first()
        assertFalse(state.groups[0].slots[0].isReservedByMe)
        assertEquals(5, state.groups[0].slots[0].availableSlots)
        assertTrue(state.showCancellationSuccessSnackbar)
    }

    @Test
    fun `cancelReservation reverts UI on API failure`() = runTest {
        val domainGroups = listOf(
            createAppointmentGroupDomain(1L, "Office Hours", slots = listOf(
                createAppointmentSlotDomain(100L, isReservedByMe = true, myReservationId = 999L, availableSlots = 4)
            ))
        )
        val uiGroups = listOf(
            createAppointmentGroupUiState(1L, "Office Hours", slots = listOf(
                createAppointmentSlotUiState(100L, isReservedByMe = true, myReservationId = 999L, availableSlots = 4)
            ))
        )

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups
        coEvery { cancelAppointmentReservationUseCase(any()) } returns DataResult.Fail()

        viewModel = createViewModel()

        viewModel.handleAction(AppointmentGroupsListAction.CancelReservation(999L))

        val state = viewModel.uiState.first()
        assertTrue(state.groups[0].slots[0].isReservedByMe)
        assertEquals(4, state.groups[0].slots[0].availableSlots)
        assertTrue(state.showCancellationErrorSnackbar)
    }

    @Test
    fun `snackbarShown resets all snackbar flags`() = runTest {
        val domainGroups = listOf(createAppointmentGroupDomain(1L, "Office Hours"))
        val uiGroups = listOf(createAppointmentGroupUiState(1L, "Office Hours"))

        coEvery { getAppointmentGroupsUseCase(any()) } returns DataResult.Success(domainGroups)
        every { uiMapper.mapToUiState(domainGroups) } returns uiGroups

        viewModel = createViewModel()

        val state = viewModel.uiState.first()
        state.snackbarShown()

        val updatedState = viewModel.uiState.first()
        assertFalse(updatedState.showReservationSuccessSnackbar)
        assertFalse(updatedState.showReservationErrorSnackbar)
        assertFalse(updatedState.showCancellationSuccessSnackbar)
        assertFalse(updatedState.showCancellationErrorSnackbar)
    }

    private fun createViewModel() = AppointmentGroupsListViewModel(
        savedStateHandle,
        getAppointmentGroupsUseCase,
        reserveAppointmentSlotUseCase,
        cancelAppointmentReservationUseCase,
        uiMapper
    )

    private fun createAppointmentGroupDomain(
        id: Long,
        title: String,
        slots: List<AppointmentSlotDomain> = emptyList()
    ) = AppointmentGroupDomain(
        id = id,
        title = title,
        description = null,
        locationName = null,
        locationAddress = null,
        participantCount = 0,
        slots = slots
    )

    private fun createAppointmentSlotDomain(
        id: Long,
        isReservedByMe: Boolean = false,
        myReservationId: Long? = null,
        availableSlots: Int = 0
    ) = AppointmentSlotDomain(
        id = id,
        appointmentGroupId = 1L,
        startDate = Date(),
        endDate = Date(),
        availableSlots = availableSlots,
        isReservedByMe = isReservedByMe,
        myReservationId = myReservationId,
        conflictInfo = ConflictInfo(false, null)
    )

    private fun createAppointmentGroupUiState(
        id: Long,
        title: String,
        isExpanded: Boolean = true,
        slots: List<AppointmentSlotUiState> = emptyList()
    ) = AppointmentGroupUiState(
        id = id,
        title = title,
        description = null,
        locationName = null,
        locationAddress = null,
        participantCount = 0,
        slots = slots,
        isExpanded = isExpanded
    )

    private fun createAppointmentSlotUiState(
        id: Long,
        isReservedByMe: Boolean = false,
        myReservationId: Long? = null,
        availableSlots: Int = 0
    ) = AppointmentSlotUiState(
        id = id,
        timeRange = "Nov 6, 4:00 PM - 4:30 PM",
        availableSlots = availableSlots,
        isAvailable = availableSlots > 0 && !isReservedByMe,
        isReservedByMe = isReservedByMe,
        myReservationId = myReservationId,
        hasConflict = false,
        conflictEventTitle = null
    )
}
