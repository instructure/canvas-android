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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.appointmentgroups.domain.usecase.CancelAppointmentReservationUseCase
import com.instructure.student.features.appointmentgroups.domain.usecase.GetAppointmentGroupsUseCase
import com.instructure.student.features.appointmentgroups.domain.usecase.ReserveAppointmentSlotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentGroupsListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAppointmentGroupsUseCase: GetAppointmentGroupsUseCase,
    private val reserveAppointmentSlotUseCase: ReserveAppointmentSlotUseCase,
    private val cancelAppointmentReservationUseCase: CancelAppointmentReservationUseCase,
    private val uiMapper: AppointmentGroupUiMapper
) : ViewModel() {

    private val courseId = savedStateHandle.get<Long>(COURSE_ID) ?: throw IllegalStateException("courseId is required")

    private val _uiState = MutableStateFlow(
        AppointmentGroupsListUiState(
            snackbarShown = ::snackbarShown,
            onReserveSlot = ::reserveSlot,
            onCancelReservation = ::cancelReservation,
            onToggleGroupExpansion = ::toggleGroupExpansion
        )
    )
    val uiState: StateFlow<AppointmentGroupsListUiState> = _uiState.asStateFlow()

    init {
        loadAppointmentGroups()
    }

    private fun snackbarShown() {
        _uiState.update {
            it.copy(
                showReservationSuccessSnackbar = false,
                showReservationErrorSnackbar = false,
                showCancellationSuccessSnackbar = false,
                showCancellationErrorSnackbar = false
            )
        }
    }

    fun loadAppointmentGroups(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, isError = false) }
            } else {
                _uiState.update { it.copy(isLoading = true, isError = false) }
            }

            when (val result = getAppointmentGroupsUseCase(GetAppointmentGroupsUseCase.Params(courseId, forceNetwork = isRefresh))) {
                is DataResult.Success -> {
                    val currentExpandedStates = _uiState.value.groups.associate { it.id to it.isExpanded }
                    val groups = uiMapper.mapToUiState(result.data).map { group ->
                        group.copy(isExpanded = currentExpandedStates[group.id] ?: true)
                    }
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, groups = groups, isError = false) }
                }
                is DataResult.Fail -> {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, isError = true) }
                }
            }
        }
    }

    private fun toggleGroupExpansion(groupId: Long) {
        _uiState.update { state ->
            state.copy(
                groups = state.groups.map { group ->
                    if (group.id == groupId) {
                        group.copy(isExpanded = !group.isExpanded)
                    } else {
                        group
                    }
                }
            )
        }
    }

    private fun reserveSlot(appointmentId: Long, comments: String?) {
        viewModelScope.launch {
            val previousState = _uiState.value.groups

            val updatedGroups = _uiState.value.groups.map { group ->
                val updatedSlots = group.slots.map { slot ->
                    if (slot.id == appointmentId) {
                        slot.copy(
                            isReservedByMe = true,
                            isAvailable = false,
                            myReservationId = -1L,
                            availableSlots = (slot.availableSlots - 1).coerceAtLeast(0)
                        )
                    } else {
                        slot
                    }
                }
                val newReservationCount = updatedSlots.count { it.isReservedByMe }
                val canReserveMore = group.maxAppointmentsPerParticipant == null ||
                                     newReservationCount < group.maxAppointmentsPerParticipant
                group.copy(
                    slots = updatedSlots,
                    currentReservationCount = newReservationCount,
                    canReserveMore = canReserveMore
                )
            }
            _uiState.update { it.copy(groups = updatedGroups) }

            val params = ReserveAppointmentSlotUseCase.Params(appointmentId, comments)
            when (val result = reserveAppointmentSlotUseCase(params)) {
                is DataResult.Success -> {
                    val actualReservationId = result.data.id
                    val finalGroups = _uiState.value.groups.map { group ->
                        group.copy(
                            slots = group.slots.map { slot ->
                                if (slot.id == appointmentId) {
                                    slot.copy(myReservationId = actualReservationId)
                                } else {
                                    slot
                                }
                            }
                        )
                    }
                    _uiState.update {
                        it.copy(
                            groups = finalGroups,
                            showReservationSuccessSnackbar = true
                        )
                    }
                }
                is DataResult.Fail -> {
                    _uiState.update {
                        it.copy(
                            groups = previousState,
                            showReservationErrorSnackbar = true
                        )
                    }
                }
            }
        }
    }

    private fun cancelReservation(reservationId: Long) {
        viewModelScope.launch {
            val previousState = _uiState.value.groups

            val updatedGroups = _uiState.value.groups.map { group ->
                val updatedSlots = group.slots.map { slot ->
                    if (slot.myReservationId == reservationId) {
                        slot.copy(
                            isReservedByMe = false,
                            isAvailable = true,
                            myReservationId = null,
                            availableSlots = slot.availableSlots + 1
                        )
                    } else {
                        slot
                    }
                }
                val newReservationCount = updatedSlots.count { it.isReservedByMe }
                val canReserveMore = group.maxAppointmentsPerParticipant == null ||
                                     newReservationCount < group.maxAppointmentsPerParticipant
                group.copy(
                    slots = updatedSlots,
                    currentReservationCount = newReservationCount,
                    canReserveMore = canReserveMore
                )
            }
            _uiState.update { it.copy(groups = updatedGroups) }

            val params = CancelAppointmentReservationUseCase.Params(reservationId)
            when (cancelAppointmentReservationUseCase(params)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(showCancellationSuccessSnackbar = true) }
                }
                is DataResult.Fail -> {
                    _uiState.update {
                        it.copy(
                            groups = previousState,
                            showCancellationErrorSnackbar = true
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val COURSE_ID = "courseId"
    }
}