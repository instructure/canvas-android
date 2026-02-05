/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class AssignmentGroup(
        override val id: Long = 0,
        val name: String? = null,
        val position: Int = 0,
        @SerializedName("group_weight")
        val groupWeight: Double = 0.0,
        val assignments: List<Assignment> = ArrayList(),
        val rules: GradingRule? = null
) : CanvasModel<AssignmentGroup>() {
    override val comparisonDate: Date? get() = null
    override val comparisonString: String? get() = position.toString()
}

@Parcelize
data class GradingRule(
    @SerializedName("drop_lowest")
    val dropLowest: Int = 0,
    @SerializedName("drop_highest")
    val dropHighest: Int = 0,
    @SerializedName("never_drop")
    val neverDrop: List<Long> = ArrayList()
) : Parcelable {
    fun hasValidRule() : Boolean {
        return dropLowest != 0 || dropHighest != 0 || neverDrop.isNotEmpty()
    }
}
