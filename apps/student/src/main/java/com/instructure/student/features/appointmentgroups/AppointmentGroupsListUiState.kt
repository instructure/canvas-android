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

data class AppointmentGroupsListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val groups: List<AppointmentGroupUiState> = emptyList(),
    val isError: Boolean = false,
    val showReservationSuccessSnackbar: Boolean = false,
    val showReservationErrorSnackbar: Boolean = false,
    val showCancellationSuccessSnackbar: Boolean = false,
    val showCancellationErrorSnackbar: Boolean = false,
    val snackbarShown: () -> Unit = {}
)

data class AppointmentGroupUiState(
    val id: Long,
    val title: String,
    val description: String?,
    val locationName: String?,
    val locationAddress: String?,
    val participantCount: Int,
    val maxAppointmentsPerParticipant: Int?,
    val currentReservationCount: Int,
    val canReserveMore: Boolean,
    val slots: List<AppointmentSlotUiState>,
    val isExpanded: Boolean = false
)

data class AppointmentSlotUiState(
    val id: Long,
    val timeRange: String,
    val availableSlots: Int,
    val isAvailable: Boolean,
    val isReservedByMe: Boolean,
    val myReservationId: Long?,
    val hasConflict: Boolean,
    val conflictEventTitle: String?
)

sealed class AppointmentGroupsListAction {
    data class ReserveSlot(
        val appointmentId: Long,
        val comments: String?
    ) : AppointmentGroupsListAction()

    data class CancelReservation(
        val reservationId: Long
    ) : AppointmentGroupsListAction()

    data class ToggleGroupExpansion(
        val groupId: Long
    ) : AppointmentGroupsListAction()
}