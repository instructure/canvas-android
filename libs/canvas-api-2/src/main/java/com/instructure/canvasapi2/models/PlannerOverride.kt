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
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlannerOverride(
        @SerializedName("id")
        val id: Long? = null,
        @SerializedName("plannable_type")
        val plannableType: PlannableType,
        @SerializedName("plannable_id")
        val plannableId: Long,
        @SerializedName("dismissed")
        val dismissed: Boolean = false,
        @SerializedName("marked_complete")
        val markedComplete: Boolean = false
) : Parcelable