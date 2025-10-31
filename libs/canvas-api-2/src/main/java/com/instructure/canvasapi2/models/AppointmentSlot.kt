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
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class AppointmentSlot(
    val id: Long,
    @SerializedName("start_at") val startAt: String? = null,
    @SerializedName("end_at") val endAt: String? = null,
    @SerializedName("appointment_group_id") val appointmentGroupId: Long = 0,
    @SerializedName("child_events") val childEvents: List<AppointmentReservation> = emptyList(),
    @SerializedName("available_slots") val availableSlots: Int = 0
) : Parcelable {
    @IgnoredOnParcel
    val startDate: Date? get() = startAt?.toDate()

    @IgnoredOnParcel
    val endDate: Date? get() = endAt?.toDate()

    @IgnoredOnParcel
    val isAvailable: Boolean get() = availableSlots > 0

    @IgnoredOnParcel
    val myReservation: AppointmentReservation?
        get() = childEvents.firstOrNull { it.user?.id == ApiPrefs.user?.id }

    @IgnoredOnParcel
    val isReservedByMe: Boolean get() = myReservation != null
}