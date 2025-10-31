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
package com.instructure.student.features.appointmentgroups.domain.usecase

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.appointmentgroups.AppointmentGroupRepository
import javax.inject.Inject

class CancelAppointmentReservationUseCase @Inject constructor(
    private val repository: AppointmentGroupRepository
) : UseCase<CancelAppointmentReservationUseCase.Params, Unit>() {

    override suspend fun execute(params: Params): DataResult<Unit> {
        return repository.cancelReservation(
            reservationId = params.reservationId
        )
    }

    data class Params(
        val reservationId: Long
    )
}