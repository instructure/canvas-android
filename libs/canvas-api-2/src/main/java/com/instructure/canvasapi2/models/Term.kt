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

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize

@Parcelize
data class Term(
        override val id: Long = 0,
        val name: String? = null,
        @SerializedName("start_at")
        val startAt: String? = null,
        @SerializedName("end_at")
        val endAt: String? = null,

        // Helper variables - here so they get parcelized
        val isGroupTerm: Boolean = false

) : CanvasModel<Term>() {
    override val comparisonDate get() = startDate
    override val comparisonString get() = name

    val endDate get() = endAt.toDate()
    val startDate get() = startAt.toDate()

    override fun compareTo(other: Term): Int {
        return when {
            isGroupTerm && other.isGroupTerm -> 0
            isGroupTerm -> 1
            other.isGroupTerm -> -1
            else -> (this as CanvasComparable<Term>).compareTo(other)
        }

    }
}