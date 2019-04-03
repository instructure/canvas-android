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
import java.util.*


abstract class CanvasComparable<T : CanvasComparable<T>> : Comparable<T>, Parcelable {
    @SerializedName("Don't use me for serialization")
    open val id: Long = -1
    open val comparisonDate: Date? get() = null // Return null if there is no date
    open val comparisonString: String? = null

    override fun compareTo(other: T): Int {
        if (id == other.id && id > 0) return 0

        val dateResult = CanvasComparable.compare(comparisonDate, other.comparisonDate)
        if (dateResult != 0) return dateResult

        val stringResult = CanvasComparable.compare(comparisonString, other.comparisonString)
        return if (stringResult != 0) stringResult
        // Even if they have the same date and string just compare ids
        else id.compareTo(other.id)
    }

    override fun describeContents(): Int = 0

    companion object {
        fun <C: Comparable<C>> compare(a: C?, b: C?): Int = when {
            a == null && b == null -> 0
            a == null -> 1
            b == null -> -1
            else -> a.compareTo(b)
        }

        fun <C : Comparable<C>> equals(a: C?, b: C?): Boolean = when {
            a == null && b == null -> true
            a == null -> false
            b == null -> false
            else -> a == b
        }
    }
}
